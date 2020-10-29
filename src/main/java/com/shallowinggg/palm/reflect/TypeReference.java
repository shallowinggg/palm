package com.shallowinggg.palm.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeReference<T> {
    private final Type type;

    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if(superClass instanceof Class<?>) {
            throw new IllegalArgumentException();
        }
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}
