package com.shallowinggg.palm.reflect;

/**
 * @author dingshimin
 */
public class MethodUtil {
    /**
     * 栈轨迹只有三层时，当前方法已是最高调用者
     */
    private static final int TOP_STACK_INDEX = 3;

    private MethodUtil() {}

    public static String getCaller() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement prevStackTrace;
        if(stackTraceElements.length == TOP_STACK_INDEX) {
            prevStackTrace = stackTraceElements[2];
        } else {
            prevStackTrace = stackTraceElements[3];
        }
        return prevStackTrace.getClassName() + "." + prevStackTrace.getMethodName();
    }

}
