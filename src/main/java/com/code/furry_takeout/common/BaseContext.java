package com.code.furry_takeout.common;

// 基于 ThreadLocal 封装的工具类
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentEmployeeId(Long id) {
        threadLocal.set(id);
    }

    public static long getCurrentEmployeeId() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
