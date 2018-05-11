# Android进程保活

## 通用方法：
1. onDestroy()方法重新启动当前应用<br/>
2. 通过MediaPlayer播放一段无声音频，来提高进程优先级。<br/>
3. 利用Notification提升权限<br/>
  Android中Service的优先级为4，通过setForeground接口可以将后台Service设置为前台Service，使进程的优先级由4提升为2，从而使进程的优先级仅仅低于用户当前正在交互的进程，与可见进程优先级一致，使进程被杀死的概率大大降低。
4. 利用辅助功能拉活，将应用加入厂商或管理软件白名单（实用性最强）<br/>
5. 利用系统Service机制拉活<br/>
  将Service的onStartCommand方法返回值设置为START_STICKY,利用系统机制在Service挂掉后自动拉活。
  如下两种情况无法拉活：
    1.短时间内Service被杀死达到5次，系统不再拉起。Service第一次被异常杀死后会在5秒内重启，第二次被杀死会在10秒内重启，第三次会在20秒内重启。
    2.进程被取得Root权限的管理工具或者系统工具通过forestop停止掉，无法重启。

## Android 5.0以下

### 1.利用fork子进程保证双向守护
##### 适用范围<br/>
该方案不受 forcestop 影响，被强制停止的应用依然可以被拉活，在 Android5.0 以下版本拉活效果非常好。
对于 Android5.0 以上手机，系统虽然会将native进程内的所有进程都杀死，这里其实就是系统“依次”杀死进程时间与拉活逻辑执行时间赛跑的问题，如果可以跑的比系统逻辑快，依然可以有效拉起。记得网上有人做过实验，该结论是成立的，在某些 Android 5.0 以上机型有效。

##### 方案实现
一、子进程怎么拉起父进程<br/>
    ![C++socket流程图](https://upload-images.jianshu.io/upload_images/5361549-fc97905a4f824636.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
  
  1. socket长连接
  2. 通过am命令
   ```
   execlp("am","am","startservice","--user",g_userId,"-n",SERVICE_NAME,(char*)NULL);
   ```
   拉起父进程。
   
二、父进程怎么确保子进程存活<br/>
  子进程在挂掉的时候，会发出SIGCHLD信号；父进程通过sigaction监听这个信号，然后重新fork出来子进程。

##### 结论：
1.Fork出来的进程名字与父进程名字相同。
2.Fork函数调用的时候，会复制父进程的全部内存。因为父进程一定是我们需要保证常驻的java进程，它在初始化的时候是fork的一个zygote进程。即使咋应用刚初始化的时候fork，进程里面是有一个java虚拟机的内存在里面，最少一二十M是有的。Fork出来子进程多的内存，最后都会算到我们自己应用的内存中。


### 2.双管道互相监听

#### 3.1  Android 5.0以下
二进制文件存放在assets下面，程序第一次启动的时候会将他拷贝到手机项目/data/data/...下
1. 将对应的packagename，servicename以及二进制可执行文件的路径传进来
2. 清理僵尸进程，就像最开始讲的，低端手机会忽略c进程，如果我们恰巧运行在低端手机上，那么c进程得不到释放会越来越多，我们称他为僵尸进程，需要清理一下
3. 建立两条管道
4. 执行二进制文件，将上面的参数传递进去
5. 然后关掉自己管道1的写端和管道2的读端，然后阻塞读取管道1，如果读取到，则代表子进程挂掉


#### 3.2  Android 5.0及以上
##### 方案实现
1. 4个文件，a进程文件a1，a2，b进程b1，b2
2. a进程加锁文件a1，b进程同理
3. a进程创建a2文件，然后轮询查看b2文件是否存在（这里可以轮询，因为时间很短），不存在代表b进程还没创建，b进程同理
4. a进程轮询到b2文件存在了，代表b进程已经创建并可能在对b1文件加锁，此时删除文件b2，代表a进程已经加锁完毕，允许b进程读取a进程的锁，b进程同理
5. a进程监听文件a2，如果a2被删除，代表b进程进行到了步骤4已经对b1加锁完成，可以开始读取b1文件的锁（不能直接监听a2文件删除，也就是不能跳过34步，这也是最难想的一部分，如果那样可能此时b进程还没创建，和b进程创建完成并加锁完成的状态是一样的，就会让进程a误以为进程b加锁完成），b进程同理

##### Android 5.1
无法通过startService拉起守护进程，通过
```
mRemote.transact(34, mServiceData, null, 0);
```
##### Android 6.0
通过广播拉起守护进程
```
mRemote.transact(14, mServiceData, null, 0);
```
## Android 5.0
Android5.0 以后系统对 Native 进程等加强了管理，Native 拉活方式失效。系统在 Android5.0 以上版本提供了 JobScheduler 接口，系统会定时调用该进程以使应用进行一些逻辑操作

### 1. 利用JobScheduler机制保活
#### 简单介绍
* JobScheduler是job的调度类，负责执行、取消任务等逻辑。
* JobService内部使用AIDL+Handler的方式来传递消息。
* JobInfo每个任务详细信息。
#### 适用范围
该方案在 Android5.0 以上版本中不受 forcestop 影响，被强制停止的应用依然可以被拉活，在 Android5.0 以上版本拉活效果非常好。仅在小米手机可能会出现有时无法拉活的问题。


## Android 6.0
### 1.Doze模式讲解
<p>Doze，即休眠、打盹之意，是谷歌在Android M(6.0)提出为了延长电池使用寿命的一种节能方式，它的核心思想是在手机处于屏幕熄灭、不插电或静止不动一段时间后，手机会自动进入Doze模式，处于Doze模式的手机将停止所有非系统应用的WalkLocks、网络访问、闹钟、GPS/WIFI扫描、JobSheduler活动。当进入Doze模式的手机屏幕被点亮、移动或充电时，会立即从Doze模式恢复到正常，系统继续执行被Doze模式"冷冻"的各项活着。换而言之，Doze模式不会杀死进程，只是停止了进程相关的耗电活动，使其进入"休眠"状态。
至Android N(7.0)后，谷歌进一步对Doze休眠机制进行了优化，休眠机制的应用场景和使用规则进行了扩展。Doze在Android 6.0中需要将手机平行放置一段时间才能开启，在7.0中则可随时开启。</p>
<p>因此，对于Android 5.0，JobSheduler的唤醒是非常有效的；对于Android 6.0，虽然谷歌引入了Doze模式，但通常很难真正进入Doze模式，所以JobSheduler的唤醒依然有效；对于Android 7.0，JobSheduler的唤醒会有一定的影响，我们可以在电池管理中给APP开绿色通道，防止手机Doze模式后阻止APP使用JobSheduler功能。注：如果遇到深度定制机型，这就要看运气了...</p>

## 账户同步，定时唤醒
1. 建立数据同步系统(ContentProvider)
2. 建立Sync系统(SyncAdapter)
    通过实现SyncAdapter这个系统服务后，利用系统的定时器对程序数据ContentProvider进行更新
3. 建立账号系统(Account Authenticator)
    通过建立Account，并关联SyncAdapter服务实现同步

Android N（7.0）对账号同步做了变动，不能对应用进行拉活。

## 利用通知监听服务


## 其他方法（针对当前应用不适用）
1. 利用Activity提升权限，
  监听手机解锁实践，在屏幕锁屏时启动1个像素的Activity，在用户解锁时将Activity销毁掉。注意该Activity需设计成用户无感知。
通过该方案，可以使进程的优先级在屏幕锁屏时间由4提升为最高优先级1.


## 参考文献
* [Android中的各种保活1](https://blog.csdn.net/zhangweiwtmdbf/article/details/52369276)
* [Android 进程常驻----MarsDaemon使用说明](https://blog.csdn.net/marswin89/article/details/50917098)