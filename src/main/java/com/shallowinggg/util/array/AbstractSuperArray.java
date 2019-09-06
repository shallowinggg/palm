package com.shallowinggg.util.array;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSuperArray<T extends Number> implements SuperArray<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSuperArray.class);

    private final long memory;
    private final long size;
    private final PrimitiveType type;

    AbstractSuperArray(long size, PrimitiveType type) {
        this.memory = PlatformDependent.allocateMemory(size * type.getSize());
        this.type = type;
        this.size = size;
    }

    public long size() {
        return size;
    }

    void setByte(long index, byte val) {
        checkIndex(index);
        PlatformDependent.setByte(memory + index, val);
    }

    byte getByte(long index) {
        checkIndex(index);
        return PlatformDependent.getByte(memory + index);
    }

    void setShort(long index, short val) {
        checkIndex(index);
        PlatformDependent.setShort(memory + index * PrimitiveType.SHORT.getSize(), val);
    }

    short getShort(long index) {
        checkIndex(index);
        return PlatformDependent.getShort(memory + index * PrimitiveType.SHORT.getSize());
    }

    void setChar(long index, char val) {
        checkIndex(index);
        PlatformDependent.setChar(memory + index * PrimitiveType.CHAR.getSize(), val);
    }

    char getChar(long index) {
        checkIndex(index);
        return PlatformDependent.getChar(memory + index * PrimitiveType.CHAR.getSize());
    }

    void setInt(long index, int val) {
        checkIndex(index);
        PlatformDependent.setInt(memory + index * PrimitiveType.INT.getSize(), val);
    }

    int getInt(long index) {
        checkIndex(index);
        return PlatformDependent.getInt(memory + index * PrimitiveType.INT.getSize());
    }

    void setFloat(long index, float val) {
        checkIndex(index);
        PlatformDependent.setFloat(memory + index * PrimitiveType.FLOAT.getSize(), val);
    }

    float getFloat(long index) {
        checkIndex(index);
        return PlatformDependent.getFloat(memory + index * PrimitiveType.FLOAT.getSize());
    }

    void setLong(long index, long val) {
        checkIndex(index);
        PlatformDependent.setLong(memory + index * PrimitiveType.LONG.getSize(), val);
    }

    long getLong(long index) {
        checkIndex(index);
        return PlatformDependent.getLong(memory + index * PrimitiveType.LONG.getSize());
    }

    void setDouble(long index, double val) {
        checkIndex(index);
        PlatformDependent.setDouble(memory + index * PrimitiveType.DOUBLE.getSize(), val);
    }

    double getDouble(long index) {
        checkIndex(index);
        return PlatformDependent.getDouble(memory + index * PrimitiveType.DOUBLE.getSize());
    }

    public void freeMemory() {
        PlatformDependent.freeMemory(memory);
    }

    void checkIndex(long index) {
        if (outOfRange(index, size())) {
            throw new ArrayIndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private static boolean outOfRange(long index, long size) {
        return (index | (size - index)) < 0;
    }

    public enum PrimitiveType {
        /**
         * byte
         */
        BYTE(1, "byte"),
        /**
         * short
         */
        SHORT(2, "short"),
        /**
         * char
         */
        CHAR(2, "char"),
        /**
         * int
         */
        INT(4, "int"),
        /**
         * float
         */
        FLOAT(4, "float"),
        /**
         * long
         */
        LONG(8, "long"),
        /**
         * double
         */
        DOUBLE(8, "double");

        int size;
        String name;

        PrimitiveType(int sz, String name) {
            this.size = sz;
        }

        int getSize() {
            return size;
        }

        String getName() {
            return name;
        }
    }

}
