package com.shehzad.keyclaockintegeration.utils;

import jakarta.persistence.Column;

import java.lang.reflect.Field;
import java.util.Iterator;

public class HelpUtil {

    public static <T> T getLast(Iterator<T> iterator) {
        T value = null;
        while (iterator.hasNext()) {
            value = iterator.next();
        }
        return value;
    }

    public static String getColumnNameFromModel(Class<?> model, String fieldName, String defaultName) {
        for (Field field : model.getDeclaredFields()) {
            Column jsonProperty = field.getAnnotation(Column.class);
            if (jsonProperty == null) {
                if (field.getName().equals(fieldName)) {
                    return fieldName;
                }
            } else {
                if (jsonProperty.name().equals(fieldName)) {
                    return jsonProperty.name();
                }
            }
        }

        return defaultName;
    }
}
