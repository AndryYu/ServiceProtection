#include "dameon-lib.h"
#include "constant.h"


// 全局变量，代表应用程序进程.
ProcessBase *g_process = NULL;
// 应用进程的UID.
const char* g_userId = NULL;

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_example_km_ndk_jni_NativeJNI_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    LOGI("这是一条测试数据");
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jboolean
JNICALL
Java_com_example_km_ndk_jni_NativeJNI_createWatcher(
        JNIEnv* env, jobject thiz, jstring user) {
    g_process = new Parent(env, thiz);//创建父进程

    g_userId = env->GetStringUTFChars(user,0);//用户ID
    g_process->catch_child_dead_signal();//接收子线程死掉的信号

    if (!g_process->create_child()) {
        LOGE("<<create child error!>>");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/**
 * <p>find_pid_by_name</p>
 * @param pid_name
 * @param pid_list
 * @return
 */
int find_pid_by_name(char* pid_name, int* pid_list){
    DIR *dir;
    struct dirent *next;
    int i = 0;
    pid_list[0] = 0;
    dir = opendir("/proc");
    if(!dir){
        return 0;
    }
    while ((next = readdir(dir)) != NULL){
        FILE *status;
        char proc_file_name[BUFFER_SIZE];
        char buffer[BUFFER_SIZE];
        char process_name[BUFFER_SIZE];
        //比较字符串
        if(strcmp(next->d_name, "..") ==0 ){
            continue;
        }
        if(!isdigit(*next->d_name)){
            continue;
        }
        sprintf(proc_file_name,"/proc/%s/cmdline", next->d_name);
        if(!(status = fopen(proc_file_name, "r"))){
            continue;
        }
        if(fgets(buffer,BUFFER_SIZE-1,status) == NULL){
            fclose(status);
            continue;
        }
        fclose(status);
        sscanf(buffer, "%[^-]", process_name);
        if(strcmp(process_name, pid_name) == 0){
            pid_list[i++] = atoi(next->d_name);
        }
    }
    if(pid_list){
        pid_list[i]=0;
    }
    closedir(dir);
    return i;
}

/**
 * <p>kill_zombie_process</p>
 * @param zombie_name
 * @Description 通过名字杀死相关进程
 */
void kill_zombie_process(char* zombie_name){
    int pid_list[200];
    int  total_num = find_pid_by_name(zombie_name, pid_list);
    LOGI("zombie process name is %s, and number is %d, killing...", zombie_name, total_num);
    int i;
    for (i = 0; i < total_num; i ++)    {
        int retval = 0;
        int daemon_pid = pid_list[i];
        if (daemon_pid > 1 && daemon_pid != getpid() && daemon_pid != getppid()){
            //SIGTERM信号被发送给进程，通知该进程是时候终止了,并且在终止之前做一些清理活动。
            retval = kill(daemon_pid, SIGTERM);
            if (!retval){
                LOGI("kill zombie successfully, zombie`s pid = %d", daemon_pid);
            }else{
                LOGE("kill zombie failed, zombie`s pid = %d", daemon_pid);
            }
        }
    }
}

/**
 * <p>java_callback</p>
 * @param env
 * @param jobj
 * @param method_name
 * @Description  通过反射获取java回调
 */
void java_callback(JNIEnv* env, jobject jobj, char* method_name){
    jclass cls = env->GetObjectClass(jobj);
    jmethodID cb_method = env->GetMethodID(cls, method_name, "()V");
    env->CallVoidMethod(jobj, cb_method);
}

extern "C"
JNIEXPORT void
JNICALL
Java_com_example_km_ndk_jni_NativeJNI_doDaemon20(JNIEnv *env, jobject jobj, jstring pkgName, jstring svcName, jstring daemonPath){
    if(pkgName == NULL || svcName == NULL || daemonPath == NULL){
        LOGE("native doDaemon parameters cannot be NULL !");
        return ;
    }
    char* pkg_name = (char*)env->GetStringUTFChars(pkgName, 0);
    char* svc_name = (char*)env->GetStringUTFChars(svcName, 0);
    char* daemon_path = (char*)env->GetStringUTFChars(daemonPath, 0);

    kill_zombie_process(NATIVE_DAEMON_NAME);
    int pipe_fd1[2];
    int pipe_fd2[2];

    //清空内存
    char r_buf[100];
    memset(r_buf, 0, sizeof(r_buf));

    //创建监听子进程管道
    if(pipe(pipe_fd1)<0){
        LOGE("pipe1 create error");
        return ;
    }
    //创建监听主进程管道
    if(pipe(pipe_fd2)<0){
        LOGE("pipe2 create error");
        return ;
    }

    char str_p1r[10];
    char str_p1w[10];
    char str_p2r[10];
    char str_p2w[10];
    //把格式化的数据写入某个字符串中
    sprintf(str_p1r,"%d",pipe_fd1[0]);
    sprintf(str_p1w,"%d",pipe_fd1[1]);
    sprintf(str_p2r,"%d",pipe_fd2[0]);
    sprintf(str_p2w,"%d",pipe_fd2[1]);

    pid_t pid;
    if((pid=fork())==0){
        execlp(daemon_path,
               NATIVE_DAEMON_NAME,
               PARAM_PKG_NAME, pkg_name,
               PARAM_SVC_NAME, svc_name,
               PARAM_PIPE_1_READ, str_p1r,
               PARAM_PIPE_1_WRITE, str_p1w,
               PARAM_PIPE_2_READ, str_p2r,
               PARAM_PIPE_2_WRITE, str_p2w,
               (char *) NULL);
    }else if(pid>0){
        close(pipe_fd1[1]);
        close(pipe_fd2[0]);
        //wait for child
        read(pipe_fd1[0], r_buf, 100);
        LOGE("Watch >>>>CHILD<<<< Dead !!!");
        java_callback(env, jobj, DAEMON_CALLBACK_NAME);
    }
}

/**
 * <p>lock_file</p>
 * @param lock_file_path
 * @return
 * @Description  锁文件
 */
int lock_file(char* lock_file_path){
    LOGI("start try to lock file >> %s <<", lock_file_path);
    int lockFileDescriptor = open(lock_file_path, O_RDONLY);
    if (lockFileDescriptor == -1){
        lockFileDescriptor = open(lock_file_path, O_CREAT, S_IRUSR);
    }
    int lockRet = flock(lockFileDescriptor, LOCK_EX);
    if (lockRet == -1){
        LOGE("lock file failed >> %s <<", lock_file_path);
        return 0;
    }else{
        LOGI("lock file success  >> %s <<", lock_file_path);
        return 1;
    }
}

/**
 * <p>notify_and_waitfor</p>
 * @param observer_self_path
 * @param observer_daemon_path
 * @Description  通知并等待
 */
void notify_and_waitfor(char *observer_self_path, char *observer_daemon_path){
    int observer_self_descriptor = open(observer_self_path, O_RDONLY);
    if (observer_self_descriptor == -1){
        observer_self_descriptor = open(observer_self_path, O_CREAT, S_IRUSR | S_IWUSR);
    }
    int observer_daemon_descriptor = open(observer_daemon_path, O_RDONLY);
    while (observer_daemon_descriptor == -1){
        usleep(1000);
        observer_daemon_descriptor = open(observer_daemon_path, O_RDONLY);
    }
    remove(observer_daemon_path);
    LOGE("Watched >>>>OBSERVER<<<< has been ready...");
}

extern "C"
JNIEXPORT void
JNICALL
Java_com_example_km_ndk_jni_NativeJNI_doDaemon21(JNIEnv *env, jobject jobj, jstring indicatorSelfPath, jstring indicatorDaemonPath, jstring observerSelfPath, jstring observerDaemonPath){
    if(indicatorSelfPath == NULL || indicatorDaemonPath == NULL || observerSelfPath == NULL || observerDaemonPath == NULL){
        LOGE("parameters cannot be NULL !");
        return ;
    }
    LOGI("doDaemon21========1");
    char* indicator_self_path = (char*)env->GetStringUTFChars(indicatorSelfPath, 0);
    char* indicator_daemon_path = (char*)env->GetStringUTFChars(indicatorDaemonPath, 0);
    char* observer_self_path = (char*)env->GetStringUTFChars(observerSelfPath, 0);
    char* observer_daemon_path = (char*)env->GetStringUTFChars(observerDaemonPath, 0);

    int lock_status = 0;
    int try_time = 0;
    while(try_time < 3 && !(lock_status = lock_file(indicator_self_path))){
        try_time++;
        LOGI("Persistent lock myself failed and try again as %d times", try_time);
        usleep(10000);
    }
    if(!lock_status){
        LOGE("Persistent lock myself failed and exit");
        return ;
    }

//	notify_daemon_observer(observer_daemon_path);
//	waitfor_self_observer(observer_self_path);
    notify_and_waitfor(observer_self_path, observer_daemon_path);

    lock_status = lock_file(indicator_daemon_path);
    if(lock_status){
        LOGE("Watch >>>>DAEMON<<<<< Daed !!");
        remove(observer_self_path);// it`s important ! to prevent from deadlock
        java_callback(env, jobj, DAEMON_CALLBACK_NAME);
    }
}


