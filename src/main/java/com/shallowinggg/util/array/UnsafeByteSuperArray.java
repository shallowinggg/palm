package com.shallowinggg.util.array;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnsafeByteSuperArray extends AbstractSuperArray<Byte> {

    private static final Logger LOG = LoggerFactory.getLogger(UnsafeByteSuperArray.class);

    public UnsafeByteSuperArray(long sz) {
        super(sz, PrimitiveType.BYTE);
    }

    @Override
    public void set(long index, Byte val) {
        setByte(index, val);
    }

    @Override
    public Byte get(long index) {
        return getByte(index);
    }


}
