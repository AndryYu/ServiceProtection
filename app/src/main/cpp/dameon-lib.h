//
// Created by KM-ZhangYufei on 2018/4/2.
//
#include <jni.h>
#include <string>
#include <unistd.h>
#include <pthread.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <sys/un.h>
#include <sys/stat.h>
#include <sys/file.h>
#include <sys/inotify.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <errno.h>
#include <string.h>
#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <android/log.h>
#include <signal.h>

#define LOG_TAG "Android-Zhang:"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


/**
 * 功能：对父子进程的一个抽象
 */
class ProcessBase{
public:
    ProcessBase();

    //父子进程要做的工作不相同，留出一个抽象接口由父子进程自己去实现
    virtual void do_work() = 0;

    //进程可以根据需要创建子进程，如果不需要创建子进程，可以给此接口一个空实现即可
    virtual bool create_child() = 0;

    //捕捉子进程死亡的信号，如果没有子进程，此方法可以给一个空实现
    virtual void catch_child_dead_signal() = 0;

    //在子进程死亡之后做任意事情
    virtual void on_child_end() = 0;

    //创建父子进程通信通道
    bool create_channel();

    /**
     * <p>set_channel</p>
     * @param channel_fd
     * @Description 给进程设置通信通道
     */
    void set_channel(int channel_fd);

    /**
     * <p>write_to_channel</p>
     * @param data  写入通道的数据
     * @param len   写入的字节数
     * @return      实际写入通道的字节数
     * @Description  向通道中写入数据
     */
    int write_to_channel(void* data, int len);

    /**
     * <p>read_from_channel</p>
     * @param data
     * @param len
     * @return
     * @Description  从通道中读数据
     */
    int read_from_channel(void* data, int len);

    //获取通道对应的文件描述符
    int get_channel() const ;

    virtual ~ProcessBase();//析构函数，类实例被销毁时自动调用

protected:
    int m_channel;
};

/**
 * 功能：父进程的实现
 */
class Parent: public ProcessBase{
public:
    Parent(JNIEnv* env, jobject jobj);

    virtual  bool create_child();

    virtual void do_work();

    virtual void catch_child_dead_signal();

    virtual void on_child_end();

    virtual ~Parent();

    bool create_channel();

    //获取父进程的JNIEnv
    JNIEnv *get_jni_env() const;

    //获取Java层的对象
    jobject get_jobj() const;

private:
    JNIEnv *m_env;
    jobject  m_jobj;
};

class Child: public ProcessBase{
public:
    Child();

    virtual ~Child();

    virtual void do_work();

    virtual bool create_child();

    virtual void catch_child_dead_signal();

    virtual void on_child_end();

    bool create_channel();

private:
    //处理父进程死亡事件
    void handle_parent_die();

    //侦听父进程发送的消息
    void listen_msg();

    //重新启动父进程
    void restart_parent();

    //处理来自父进程的消息
    void handle_msg(const char* msg);

    //用于检测父进程是否挂掉
    void* parent_monitor();

    void start_parent_monitor();

    //这个联合体的作用是帮助将类的成员函数做为线程函数使用
    union{
        void* (*thread_rtn)(void*);
        void* (Child::*member_rtn)();
    } RTN_MAP;
};
