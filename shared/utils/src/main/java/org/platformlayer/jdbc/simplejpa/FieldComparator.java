package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;
import java.util.Comparator;

public class FieldComparator implements Comparator {

    private final Field orderByField;

    public FieldComparator(Field orderByField) {
        this.orderByField = orderByField;
    }

    @Override
    public int compare(Object o1, Object o2) {
        try {
            Object v1 = orderByField.get(o1);
            Object v2 = orderByField.get(o2);
            return ((Comparable) v1).compareTo(v2);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot get field value: " + orderByField);
        }
    }

}
