package com.raddle.util;

/**
 * description: 在内部类中传递变量
 */
public class ObjectHolder<T> {

    public ObjectHolder(T value) {
        this.value = value;
    }

    public T value;
}
