package com.rhsystem.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Reflections {

    public static <T> Class<T> getGenericType(Class<T> target, int index) {
        var superclass = target.getGenericSuperclass();
        if (superclass instanceof ParameterizedType parameterizedType) {
            Type type = parameterizedType.getActualTypeArguments()[index];
            return (Class<T>) type;
        }
        return null;
    }

    public static <T> T cast(Class<T> target, Object obj) {
        return target.cast(obj);
    }
}
