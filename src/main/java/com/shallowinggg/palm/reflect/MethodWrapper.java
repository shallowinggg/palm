package com.shallowinggg.palm.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author dingshimin
 */
public class MethodWrapper {
    private static final MethodHandles.Lookup lookup;

    private final Method method;
    private MethodHandle mh;

    public MethodWrapper(Method method) {
        this.method = method;
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                method.setAccessible(true);
                return null;
            });
        } else {
            method.setAccessible(true);
        }

        try {
            mh = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            // do nothing
        }
    }

    public Object invoke(Object... args) throws Throwable {
        if (mh != null) {
            if (args.length == 1) {
                return mh.invoke(args[0]);
            }
            return mh.invoke(args);
        }

        if (args.length == 1) {
            return method.invoke(args[0]);
        }
        Object[] params = new Object[args.length - 1];
        System.arraycopy(args, 1, params, 0, params.length);
        return method.invoke(args[0], params);
    }

    public static MethodWrapper findGetterMethod(Class<?> clazz, String name, Class<?>... params) throws NoSuchMethodException {
        Object maybeException;
        if (System.getSecurityManager() != null) {
            maybeException = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                try {
                    return clazz.getDeclaredMethod(name, params);
                } catch (NoSuchMethodException e) {
                    return e;
                }
            });
        } else {
            try {
                maybeException = clazz.getDeclaredMethod(name, params);
            } catch (NoSuchMethodException e) {
                maybeException = e;
            }
        }

        if (maybeException instanceof Exception) {
            throw (NoSuchMethodException) maybeException;
        }
        return new MethodWrapper((Method) maybeException);
    }

    public String getMethodName() {
        return method.getName();
    }

    static {
        lookup = MethodHandles.lookup();
    }
}
