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
### 1. 利用Native进程拉活<br/>
##### 适用范围<br/>
该方案不受 forcestop 影响，被强制停止的应用依然可以被拉活，在 Android5.0 以下版本拉活效果非常好。
对于 Android5.0 以上手机，系统虽然会将native进程内的所有进程都杀死，这里其实就是系统“依次”杀死进程时间与拉活逻辑执行时间赛跑的问题，如果可以跑的比系统逻辑快，依然可以有效拉起。记得网上有人做过实验，该结论是成立的，在某些 Android 5.0 以上机型有效。

##### 方案实现
一、在Native进程中感知主进程存活状态<br/>
两种实现方式：<br/>
  1.在Native进程中通过死循环或者定时器，轮询判断主进程是否存活，当住进程不存活时进行拉活。该方案的很大缺点是不停的轮询执行判断逻辑，非常耗电。
  2.在主进程中创建一个监控文件你，并且在主进程中持有文件锁。在拉活进程启动后申请文件锁将会被阻塞，一旦可以成功获取到锁，说明主进程挂掉，即可进行拉活。由于Android中的应用都运行于虚拟机之上，Java层的文件锁与Linux层的文件锁是不同的，要实现该功能需要封装Linux层的文件锁供上层调用。

二、在Native进程中如何拉活主进程<br/>
  通过Native进程拉活主线程，通过am命令，指定”--include-stopped-packages”参数拉活处于forestop状态的情况。

三、如何保证Native进程的唯一<br/>
  从可扩展性和进程唯一等多方面考虑，将Native进程设计成C/S结构模式，主进程与Native进程通过Localsocket进行通信。在Native进程中利用Localsocket保证进程的唯一性，不至于出现创建多个Native进程以及Native进程变成僵尸进程等问题。


## Android 5.0
Android5.0 以后系统对 Native 进程等加强了管理，Native 拉活方式失效。系统在 Android5.0 以上版本提供了 JobScheduler 接口，系统会定时调用该进程以使应用进行一些逻辑操作

### 1. 利用JobScheduler机制保活
#### 简单介绍
* JobScheduler是job的调度类，负责执行、取消任务等逻辑。
* JobService内部使用AIDL+Handler的方式来传递消息。
* JobInfo每个任务详细信息。
#### 适用范围
该方案在 Android5.0 以上版本中不受 forcestop 影响，被强制停止的应用依然可以被拉活，在 Android5.0 以上版本拉活效果非常好。仅在小米手机可能会出现有时无法拉活的问题。


## Android 7.0
1.

## 其他方法（针对当前应用不适用）
1. 利用Activity提升权限，
  监听手机解锁实践，在屏幕锁屏时启动1个像素的Activity，在用户解锁时将Activity销毁掉。注意该Activity需设计成用户无感知。
通过该方案，可以使进程的优先级在屏幕锁屏时间由4提升为最高优先级1.


## 参考文献
[Android中的各种保活1](https://blog.csdn.net/zhangweiwtmdbf/article/details/52369276)
