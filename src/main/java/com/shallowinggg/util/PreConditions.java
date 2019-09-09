package com.shallowinggg.util;

/**
 * @author dingshimin
 */
public final class PreConditions {

    public static void checkState(boolean expr) {
        if(!expr) {
            throw new IllegalStateException();
        }
    }

    public static void checkState(boolean expr, String formatMsg, Object... objects) {
        if(!expr) {
            throw new IllegalStateException(String.format(formatMsg, objects));
        }
    }

    public static void checkArgument(boolean expr) {
        if(!expr) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkArgument(boolean expr, String formatMsg, Object... objects) {
        if(!expr) {
            throw new IllegalArgumentException(String.format(formatMsg, objects));
        }
    }

    public static <T> T checkNotNull(T obj) {
        if(obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    public static <T> T checkNotNull(T obj, String formatMsg, Object... objects) {
        if(obj == null) {
            throw new NullPointerException(String.format(formatMsg, objects));
        }
        return obj;
    }

    public static void checkIndex(boolean expr) {
        if(!expr) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static void checkIndex(boolean expr, String formatMsg, Object... objects) {
        if(!expr) {
            throw new IndexOutOfBoundsException(String.format(formatMsg, objects));
        }
    }
}
