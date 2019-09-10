package com.shallowinggg.util.array;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SuperArrayTest {

    @Test
    public void testUnsafeByteSuperArray() {
        SuperArray<Byte> byteSuperArray = new UnsafeByteSuperArray(100);
        for(long i = 0; i < 100; ++i) {
            System.out.println(byteSuperArray.get(i));
        }
        byteSuperArray.free();
    }

    @Test
    public void testSliceAndClear() {
        SuperArray<Byte> byteSuperArray = new UnsafeByteSuperArray(100);
        for(Byte b : byteSuperArray) {
            System.out.print(b + " ");
        }
        System.out.println();
        SuperArray<Byte> slice = byteSuperArray.slice(0, 20);
        slice.clear();
        for(Byte b : slice) {
            System.out.print(b + " ");
        }
        System.out.println();
        for(Byte b : byteSuperArray) {
            System.out.print(b + " ");
        }
        byteSuperArray.free();
    }

    @Test
    public void testDuplicate() {
        SuperArray<Byte> byteSuperArray = SuperArrays.newByteArray(100);
        System.out.print(byteSuperArray.memoryAddress() + ": ");
        for(Byte b : byteSuperArray) {
            System.out.print(b + " ");
        }
        System.out.println();

        SuperArray<Byte> duplicate = byteSuperArray.duplicate(0, 20);
        duplicate.clear();
        System.out.print(duplicate.memoryAddress() + ": ");
        for(Byte b : duplicate) {
            System.out.print(b + " ");
        }
        duplicate.free();
        System.out.println();

        for(Byte b : byteSuperArray) {
            System.out.print(b + " ");
        }
        byteSuperArray.free();
    }

    @Test
    public void testLeakAware() {
        // -XX:+PrintGCDetails -Xmn20m -Xmx20m
        constructArray();

        List<byte[]> list = new ArrayList<>();
        int _1M = 1024 * 1024;
        for(int i = 0; i < 10; ++i) {
            list.add(new byte[_1M]);
        }
        SuperArray<?> superArray = constructArray();
        superArray.free();
    }

    private SuperArray<?> constructArray() {
        return SuperArrays.newByteArray(100);
    }
}
