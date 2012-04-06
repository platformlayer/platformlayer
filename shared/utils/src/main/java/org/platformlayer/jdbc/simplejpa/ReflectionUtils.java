package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static Field findField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName))
                return field;
        }
        return null;
    }

}
