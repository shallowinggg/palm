package com.shallowinggg.util.array;

/**
 * @author dingshimin
 */
class SuperArrayUtil {

    private SuperArrayUtil() {
    }

    static void clearBytes(long size, AbstractSuperArray<?> superArray) {
        long tmp;
        long len4;
        long len2;
        long len1;

        long len8 = size / 8;
        tmp = size % 8;
        len4 = tmp / 4;
        tmp = tmp % 4;
        len2 = tmp / 2;
        len1 = tmp % 2;

        superArray.fill0(len8, len4, len2, len1);
    }

    static void clearShorts(long size, AbstractSuperArray<?> superArray) {
        long tmp;
        long len4;
        long len2;

        long len8 = size / 8;
        tmp = size % 8;
        len4 = tmp / 4;
        tmp = tmp % 4;
        len2 = tmp / 2;

        superArray.fill0(len8, len4, len2, 0);
    }

    static void clearInts(long size, AbstractSuperArray<?> superArray) {
        long tmp;
        long len4;

        long len8 = size / 8;
        tmp = size % 8;
        len4 = tmp / 4;

        superArray.fill0(len8, len4, 0, 0);
    }

    static void clearLongs(long size, AbstractSuperArray<?> superArray) {
        superArray.fill0(size, 0, 0, 0);
    }
}
