package com.itheima.cdl.util;


public class ThreadLocalUtil {

    private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<Integer>();

    public static void setData(Integer userId){
        THREAD_LOCAL.set(userId);
    }

    public static Integer getData(){
        return THREAD_LOCAL.get();
    }

    public static void clean(){
        THREAD_LOCAL.remove();
    }
}
