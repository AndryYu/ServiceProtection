//
// Created by KM-ZhangYufei on 2018/4/2.
//
#include "dameon-lib.h"

extern ProcessBase *g_process;
extern const char* g_userId;
extern JNIEnv* g_env;

//子进程有权访问父进程的私有目录，再次建立跨进程通信的套接字文件
static const char* PATH = "/data/data/com.example.km.ndk/ndk.sock";
//服务名称
static const char* SERVICE_NAME = "com.example.km.ndk/com.example.km.ndk.service.DaemonService";

bool ProcessBase::create_channel() {
    return false;
}
int ProcessBase::write_to_channel(void *data, int len) {
    return write(m_channel, data, len);
}
int ProcessBase::read_from_channel(void *data, int len) {
    return read(m_channel,data,len);
}
int ProcessBase::get_channel() const {
    return m_channel;
}
void ProcessBase::set_channel(int channel_fd) {
    m_channel = channel_fd;
}
ProcessBase::ProcessBase() {}
ProcessBase::~ProcessBase() {close(m_channel);}



Parent::Parent(JNIEnv *env, jobject jobj) {
    LOGE("<<new parent instance>>");
    m_jobj=env->NewGlobalRef(jobj);
}
Parent::~Parent() {
    LOGE("<<Parent::~Parent()>>");
    g_process=NULL;
}
void Parent::do_work() {}
JNIEnv* Parent::get_jni_env() const {
    return m_env;
}
jobject Parent::get_jobj() const {
    return m_jobj;
}
bool Parent::create_channel() {
    //父进程创建通道，这里其实是创建一个客户端并尝试连接服务器(子进程)
    int sockfd;
    sockaddr_un addr;
    sockfd = socket(AF_LOCAL, SOCK_STREAM, 0);
    if (sockfd<0){
        LOGI("<<Parent create channel failed>>");
        return false;
    }
    memset(&addr,0, sizeof(addr));
    addr.sun_family = AF_LOCAL;
    strcpy(addr.sun_path, PATH);
    while (1){
        if(connect(sockfd, ( sockaddr *) &addr, sizeof(addr))<0){
            close(sockfd);
            sleep(1);
            continue;
        }
        set_channel(sockfd);
        LOGI("<<parent channel fd %d>>", m_channel);
        break;
    }
    return true;
}
static void sig_handler(int signo){
    //子进程死亡会发出SIGCHLD信号
    pid_t  pid;
    int status;
    //调用wait等待子进程死亡时发出的SIGCHLD信号以给子进程收尸，防止它变成僵尸进程
    pid = wait(&status);
    if(g_process!=NULL){
        g_process->on_child_end();
    }
}
void Parent::catch_child_dead_signal() {
    LOGI("<<process %d install child dead signal detector!>>",getpid());
    struct sigaction sa;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags=0; //只接受一次信号
    sa.sa_handler=sig_handler; //指出和signum关联的行动
    //监听SIGCHLD信号
    sigaction(SIGCHLD, &sa ,NULL);
   /* g_process->on_child_end();*/
}
void Parent::on_child_end() {
    LOGI("<<on_child_end:create a new child process>>");
    create_child();
}
bool Parent::create_child() {
    pid_t  pid = fork();
    if(pid < 0){
        return false;
    }else if (pid == 0){ //子进程
        LOGI("<<In child process,pid=%d>>",getpid());
        Child child;
        ProcessBase & ref_child = child;
        ref_child.do_work();
    }else if (pid > 0){ //父进程
        LOGI("<<fork函数返回值:%d>>",pid);
        LOGI("<<In parent process,pid=%d>>",getpid());
    }
    return true;
}




bool Child::create_child() {
    return false;//子进程不需要再去创建子进程，此函数留空
}
Child::Child() {
    RTN_MAP.member_rtn = &Child::parent_monitor;
}
Child::~Child() {
    LOGI("<<~Child(),unlink %s>>",PATH);
    unlink(PATH);
}
void Child::catch_child_dead_signal() {
    return;//子进程不需要捕捉SIGCHLD信号
}
void Child::on_child_end() {
    return;//子进程不需要处理
}
void Child::handle_parent_die() {
    //子进程成为了孤儿进程，等待被Init进程收养后在进行后续处理
    while(getppid()!=1){
        usleep(500);//休眠0.5ms
    }
    close(m_channel);
    //重启父进程服务
    LOGI("<<parent died,restart now>>");
    restart_parent();
}
void Child::restart_parent() {
    LOGI("<<restart_parent enter>>");
    //重启父进程，通过am启动Java空间的任一组件(service或者activity等)即可让应用重新启动
    execlp("am","am","startservice","--user",g_userId,"-n",SERVICE_NAME,(char*)NULL);
}
void* Child::parent_monitor() {
    handle_parent_die();
}
void Child::start_parent_monitor() {
    pthread_t tid;
    pthread_create(&tid,NULL,RTN_MAP.thread_rtn,this);
}
bool Child::create_channel() {
    int listenfd, connfd;
    struct sockaddr_un addr;
    listenfd = socket(AF_LOCAL, SOCK_STREAM, 0);
    unlink(PATH);

    //清空内存
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_LOCAL;
    strcpy(addr.sun_path, PATH);

    if (bind(listenfd, (sockaddr *) &addr, sizeof(addr)) < 0){
        LOGE("<<bind error,errno(%d)>>", errno);
        return false;
    }

    //监听，最多监听5个进程
    listen(listenfd, 5);
    while (true){
        //返回值 客户端地址 阻塞式函数
        if((connfd=accept(listenfd, NULL, NULL))<0){
            if(errno == EINTR)
                continue;
            else{
                LOGE("<<accept error>>");
                return false;
            }
        }
        set_channel(connfd);
        break;
    }
    LOGI("<<child channel fd %d>>", m_channel);
    return true;
}
void Child::handle_msg(const char *msg) {}
void Child::listen_msg() {
    fd_set rfds;
    int retry = 0;
    while(1){
        FD_ZERO(&rfds);
        FD_SET(m_channel, &rfds);
        timeval timeout = {3, 0};
        int r = select(m_channel+1,&rfds,NULL,NULL,&timeout);
        if(r>0){
            char pkg[256] = {0};
            if(FD_ISSET(m_channel, &rfds)){
                read_from_channel(pkg, sizeof(pkg));
                LOGI("<<A message comes:%s>>",pkg);
                handle_msg((const char*)pkg);
            }
        }
    }
}
void Child::do_work() {
    LOGI("<<Child do_work>>");
    start_parent_monitor();//启动监视线程
    if(create_channel()){ //等待并且处理来自父进程发送的消息
        listen_msg();
    }
}






