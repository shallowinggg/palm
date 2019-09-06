package com.shallowinggg.util.array;

import org.junit.Test;

public class SuperArrayTest {

    @Test
    public void testUnsafeByteSuperArray() {
        SuperArray<Byte> byteSuperArray = new UnsafeByteSuperArray(100);
        for(long i = 0; i < 100; ++i) {
            byteSuperArray.set(i, (byte) i);
        }
        for(long i = 0; i < 100; ++i) {
            System.out.println(byteSuperArray.get(i));
        }

    }

    @Test
    public void testArray() {
        int[] arr = new int[100];
        for(int i : arr) {
            System.out.print(i);
        }
    }
}
