package com.shallowinggg.util.array;

import org.junit.Test;

public class SuperArrayTest {

    @Test
    public void testUnsafeByteSuperArray() {
        SuperArray<Byte> byteSuperArray = new UnsafeByteSuperArray(100);
        for(long i = 0; i < 100; ++i) {
            System.out.println(byteSuperArray.get(i));
        }

    }

    @Test
    public void testClear() {
        SuperArray<Byte> byteSuperArray = new UnsafeByteSuperArray(100);
        for(Byte b : byteSuperArray) {
            System.out.print(b + " ");
        }
        System.out.println();
        SuperArray<Byte> slice = byteSuperArray.slice(10, 20);
        slice.clear();
        for(Byte b : slice) {
            System.out.print(b + " ");
        }
        System.out.println();
        for(Byte b : byteSuperArray) {
            System.out.print(b + " ");
        }
    }
}
