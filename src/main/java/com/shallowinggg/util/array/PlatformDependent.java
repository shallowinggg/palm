package com.shallowinggg.util.array;

import com.shallowinggg.util.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class PlatformDependent {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformDependent.class);

    private static final int JAVA_VERSION = javaVersion0();

    private static final Unsafe UNSAFE;
    private static final Method ALLOCATE_ARRAY_METHOD;
    private static final Object INTERNAL_UNSAFE;

    public static final boolean BIG_ENDIAN_BYTE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    private static final long BYTE_ARRAY_BASE_OFFSET;
    private static final long SHORT_ARRAY_BASE_OFFSET;
    private static final long CHAR_ARRAY_BASE_OFFSET;
    private static final long INT_ARRAY_BASE_OFFSET;
    private static final long FLOAT_ARRAY_BASE_OFFSET;
    private static final long LONG_ARRAY_BASE_OFFSET;
    private static final long DOUBLE_ARRAY_BASE_OFFSET;

    static {
        Unsafe unsafe;
        Object internalUnsafe = null;
        Method allocationArrayMethod = null;

        final Object maybeUnsafe = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                return unsafeField.get(null);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                return e;
            }
        });

        if (maybeUnsafe instanceof Throwable) {
            unsafe = null;
            LOG.debug("sun.misc.Unsafe.theUnsafe: unavailable", (Throwable) maybeUnsafe);
        } else {
            unsafe = (Unsafe) maybeUnsafe;
            LOG.debug("sun.misc.Unsafe.theUnsafe: available");
        }

        if (unsafe != null) {
            // There are assumptions made where ever BYTE_ARRAY_BASE_OFFSET is used (equals, hashCodeAscii, and
            // primitive accessors) that arrayIndexScale == 1, and results are undefined if this is not the case.
            boolean usingUnsafe = true;
            long byteArrayIndexScale = unsafe.arrayIndexScale(byte[].class);
            if (byteArrayIndexScale != 1) {
                LOG.debug("unsafe.arrayIndexScale is {} (expected: 1). Not using unsafe.", byteArrayIndexScale);
                usingUnsafe = false;
            }
            long shortArrayIndexScale = unsafe.arrayIndexScale(short[].class);
            if (shortArrayIndexScale != 2) {
                LOG.debug("unsafe.arrayIndexScale is {} (expected: 1). Not using unsafe.", shortArrayIndexScale);
                usingUnsafe = false;
            }
            long charArrayIndexScale = unsafe.arrayIndexScale(char[].class);
            if (charArrayIndexScale != 2) {
                LOG.debug("unsafe.arrayIndexScale is {} (expected: 1). Not using unsafe.", charArrayIndexScale);
                usingUnsafe = false;
            }
            long intArrayIndexScale = unsafe.arrayIndexScale(int[].class);
            if (intArrayIndexScale != 4) {
                LOG.debug("unsafe.arrayIndexScale is {} (expected: 1). Not using unsafe.", intArrayIndexScale);
                usingUnsafe = false;
            }
            long floatArrayIndexScale = unsafe.arrayIndexScale(float[].class);
            if (floatArrayIndexScale != 4) {
                LOG.debug("unsafe.arrayIndexScale is {} (expected: 1). Not using unsafe.", floatArrayIndexScale);
                usingUnsafe = false;
            }
            long longArrayIndexScale = unsafe.arrayIndexScale(long[].class);
            if (longArrayIndexScale != 8) {
                LOG.debug("unsafe.arrayIndexScale is {} (expected: 1). Not using unsafe.", longArrayIndexScale);
                usingUnsafe = false;
            }
            long doubleArrayIndexScale = unsafe.arrayIndexScale(double[].class);
            if (doubleArrayIndexScale != 8) {
                LOG.debug("unsafe.arrayIndexScale is {} (expected: 1). Not using unsafe.", doubleArrayIndexScale);
                usingUnsafe = false;
            }
            if (!usingUnsafe) {
                unsafe = null;
            }
        }

        UNSAFE = unsafe;

        if (unsafe == null) {
            BYTE_ARRAY_BASE_OFFSET = -1;
            SHORT_ARRAY_BASE_OFFSET = -1;
            CHAR_ARRAY_BASE_OFFSET = -1;
            INT_ARRAY_BASE_OFFSET = -1;
            FLOAT_ARRAY_BASE_OFFSET = -1;
            LONG_ARRAY_BASE_OFFSET = -1;
            DOUBLE_ARRAY_BASE_OFFSET = -1;

            ALLOCATE_ARRAY_METHOD = null;
        } else {
            BYTE_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
            SHORT_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(short[].class);
            CHAR_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(char[].class);
            INT_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(int[].class);
            FLOAT_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(float[].class);
            LONG_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(long[].class);
            DOUBLE_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(double[].class);

            if (javaVersion() >= 9) {
                Object maybeException = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    try {
                        Class<?> internalUnsafeClass = getClassLoader(PlatformDependent.class)
                                .loadClass("jdk.internal.misc.Unsafe");
                        Method method = internalUnsafeClass.getDeclaredMethod("getUnsafe");
                        return method.invoke(null);
                    } catch (Throwable e) {
                        return e;
                    }
                });

                if (!(maybeException instanceof Throwable)) {
                    internalUnsafe = maybeException;
                    final Object finalInternalUnsafe = internalUnsafe;
                    maybeException = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        try {
                            return finalInternalUnsafe.getClass().getDeclaredMethod(
                                    "allocateUninitializedArray", Class.class, int.class);
                        } catch (NoSuchMethodException | SecurityException e) {
                            return e;
                        }
                    });

                    if (maybeException instanceof Method) {
                        try {
                            Method m = (Method) maybeException;
                            byte[] bytes = (byte[]) m.invoke(finalInternalUnsafe, byte.class, 8);
                            assert bytes.length == 8;
                            allocationArrayMethod = m;
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            maybeException = e;
                        }
                    }
                }

                if (maybeException instanceof Throwable) {
                    LOG.debug("jdk.internal.misc.Unsafe.allocateUninitializedArray(int): unavailable",
                            (Throwable) maybeException);
                } else {
                    LOG.debug("jdk.internal.misc.Unsafe.allocateUninitializedArray(int): available");
                }
            } else {
                LOG.debug("jdk.internal.misc.Unsafe.allocateUninitializedArray(int): unavailable prior to Java9");
            }

            ALLOCATE_ARRAY_METHOD = allocationArrayMethod;
        }

        INTERNAL_UNSAFE = internalUnsafe;
    }

    static boolean hasUnsafe() {
        return UNSAFE != null;
    }

    static byte[] allocateUninitializedByteArray(int sz) {
        try {
            return (byte[]) ALLOCATE_ARRAY_METHOD.invoke(INTERNAL_UNSAFE, byte.class, sz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    static short[] allocateUninitializedShortArray(int sz) {
        try {
            return (short[]) ALLOCATE_ARRAY_METHOD.invoke(INTERNAL_UNSAFE, short.class, sz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    static char[] allocateUninitializedCharArray(int sz) {
        try {
            return (char[]) ALLOCATE_ARRAY_METHOD.invoke(INTERNAL_UNSAFE, char.class, sz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    static int[] allocateUninitializedIntArray(int sz) {
        try {
            return (int[]) ALLOCATE_ARRAY_METHOD.invoke(INTERNAL_UNSAFE, int.class, sz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    static float[] allocateUninitializedFloatArray(int sz) {
        try {
            return (float[]) ALLOCATE_ARRAY_METHOD.invoke(INTERNAL_UNSAFE, float.class, sz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    static long[] allocateUninitializedLongArray(int sz) {
        try {
            return (long[]) ALLOCATE_ARRAY_METHOD.invoke(INTERNAL_UNSAFE, long.class, sz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    static double[] allocateUninitializedDoubleArray(int sz) {
        try {
            return (double[]) ALLOCATE_ARRAY_METHOD.invoke(INTERNAL_UNSAFE, double.class, sz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    static long allocateMemory(long sz) {
        return UNSAFE.allocateMemory(sz);
    }

    static void freeMemory(long address) {
        UNSAFE.freeMemory(address);
    }

    static void setByte(long index, byte val) {
        UNSAFE.putByte(index, val);
    }

    static byte getByte(long index) {
        return UNSAFE.getByte(index);
    }

    static void setChar(long index, char val) {
        UNSAFE.putChar(index, val);
    }

    static char getChar(long index) {
        return UNSAFE.getChar(index);
    }

    static void setShort(long index, short val) {
        UNSAFE.putShort(index, val);
    }

    static short getShort(long index) {
        return UNSAFE.getShort(index);
    }

    static void setInt(long index, int val) {
        UNSAFE.putInt(index, val);
    }

    static int getInt(long index) {
        return UNSAFE.getInt(index);
    }

    static void setFloat(long index, float val) {
        UNSAFE.putFloat(index, val);
    }

    static float getFloat(long index) {
        return UNSAFE.getFloat(index);
    }

    static void setLong(long index, long val) {
        UNSAFE.putLong(index, val);
    }

    static long getLong(long index) {
        return UNSAFE.getLong(index);
    }

    static void setDouble(long index, double val) {
        UNSAFE.putDouble(index, val);
    }

    static double getDouble(long index) {
        return UNSAFE.getDouble(index);
    }

    private static ClassLoader getClassLoader(Class<?> clazz) {
        if (System.getSecurityManager() == null) {
            return clazz.getClassLoader();
        } else {
            return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) clazz::getClassLoader);
        }
    }

    static int javaVersion() {
        return JAVA_VERSION;
    }

    private static int javaVersion0() {
        final int majorVersion;
        majorVersion = majorVersionFromJavaSpecificationVersion();
        LOG.debug("Java version: {}", majorVersion);
        return majorVersion;
    }

    private static int majorVersionFromJavaSpecificationVersion() {
        return majorVersion(SystemPropertyUtil.get("java.specification.version", "1.6"));
    }

    // Package-private for testing only
    static int majorVersion(final String javaSpecVersion) {
        final String[] components = javaSpecVersion.split("\\.");
        final int[] version = new int[components.length];
        for (int i = 0; i < components.length; i++) {
            version[i] = Integer.parseInt(components[i]);
        }

        if (version[0] == 1) {
            assert version[1] >= 8;
            return version[1];
        } else {
            return version[0];
        }
    }

}
