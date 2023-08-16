package com.shiyi.gulimall.ware.rabbitmq;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {

    private static final Map<String,Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory(){

    }

    public static <T> T getInstance(Class<T> clazz){
        if(null == clazz){
            throw new IllegalArgumentException();
        }
        String key = clazz.toString();
        if(OBJECT_MAP.containsKey(key)){
            return clazz.cast(OBJECT_MAP.get(key));
        }
        return clazz.cast(OBJECT_MAP.computeIfAbsent(key,k -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e.getMessage(),e);
            }
        }));
    }
}
