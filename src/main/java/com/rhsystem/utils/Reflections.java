package com.rhsystem.utils;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class Reflections {

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getGenericType(Class<T> target, int index) {
        var superclass = target.getGenericSuperclass();
        if (superclass instanceof ParameterizedType parameterizedType) {
            Type type = parameterizedType.getActualTypeArguments()[index];
            return (Class<T>) type;
        }
        return null;
    }


    public static Set<Class<?>> getLoadedClasses() {
        try {
            var classLoader = Thread.currentThread().getContextClassLoader();
            var classPath = ClassPath.from(classLoader);
            return classPath
                    .getTopLevelClassesRecursive("com.rhsystem")
                    .stream()
                    .map(x ->{
                        try {
                            return Class.forName(x.getName(), false, classLoader);
                        } catch (ClassNotFoundException | LinkageError e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (Exception e) {
            log.error("Error loading classes", e);
        }
        return new HashSet<>();
    }

    public static Set<Class<?>>  getClassesWithAnnotation(Class<? extends Annotation> annotationClass) {
        return getLoadedClasses()
                .stream()
                .filter(clazz -> clazz.isAnnotationPresent(annotationClass))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static <T extends Annotation> T getAnnotation(Class<T> annotation, Class<?> clazz) {
        return clazz.getAnnotation(annotation);
    }
}
