package com.shallowinggg.palm.cache;

public class LoadingRuntimeException extends RuntimeException {

    public LoadingRuntimeException(Throwable e) {
        super(e);
    }

    public LoadingRuntimeException(String msg) {
        super(msg);
    }

    public LoadingRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }
}
