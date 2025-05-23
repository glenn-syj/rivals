package com.glennsyj.rivals.api.config;

import java.lang.reflect.Field;

public class EntityTestUtil {

    public static void setFieldWithValue(Object entity, String fieldName, Object value) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }

    // 편의를 위한 단축 메서드
    public static void setId(Object entity, Object value) {
        setFieldWithValue(entity, "id", value);
    }
}
