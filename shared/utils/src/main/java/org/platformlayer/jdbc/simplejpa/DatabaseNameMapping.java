package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.Table;

import com.google.common.base.Strings;

public class DatabaseNameMapping {
    public static final DatabaseNameMapping MYSQL = new DatabaseNameMapping(false);
    public static final DatabaseNameMapping POSTGRESQL = new DatabaseNameMapping(false);

    final boolean lowerCaseColumnNames;

    public DatabaseNameMapping(boolean lowerCaseColumnNames) {
        super();
        this.lowerCaseColumnNames = lowerCaseColumnNames;
    }

    public String getTableName(Class<?> clazz) {
        String tableName = clazz.getSimpleName();

        Table table = clazz.getAnnotation(Table.class);
        if (table != null) {
            if (!Strings.isNullOrEmpty(table.name())) {
                tableName = table.name();
            }
        }
        return tableName;
    }

    public String getColumnName(Field field) {
        String columnName = field.getName();

        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            if (!Strings.isNullOrEmpty(column.name())) {
                columnName = column.name();
            }
        }

        if (lowerCaseColumnNames) {
            columnName = columnName.toLowerCase();
        }
        return columnName;
    }
}
