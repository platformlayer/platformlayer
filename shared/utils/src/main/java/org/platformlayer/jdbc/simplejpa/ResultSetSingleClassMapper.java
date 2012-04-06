package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.IdClass;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ResultSetSingleClassMapper {
    final Class<?> targetClass;
    final String tableName;
    final Map<String, Field> fields = Maps.newHashMap();
    Map<Integer, Field> columnToFieldMap;
    KeyMapper keyMapper;
    final List<Field> idFields = Lists.newArrayList();
    final DatabaseNameMapping nameMapping;

    public ResultSetSingleClassMapper(DatabaseNameMapping nameMapping, Class<?> targetClass) {
        super();
        this.nameMapping = nameMapping;
        this.targetClass = targetClass;
        this.tableName = nameMapping.getTableName(targetClass);

        discoverFields();
    }

    void discoverFields() {
        for (Field field : targetClass.getFields()) {
            String columnName = nameMapping.getColumnName(field);
            fields.put(columnName, field);
            Id idAnnotation = field.getAnnotation(Id.class);
            if (idAnnotation != null)
                idFields.add(field);
        }
    }

    Object getKey(ResultSet rs) throws SQLException {
        return getKeyMapper(rs).getKey(rs);
    }

    private KeyMapper getKeyMapper(ResultSet rs) throws SQLException {
        if (this.keyMapper == null) {
            KeyMapper keyMapper = null;

            List<Integer> columnNumbers = Lists.newArrayList();
            for (Field idField : idFields) {
                Integer columnNumber = findColumn(rs, idField);
                if (columnNumber == null) {
                    throw new IllegalArgumentException("Id Field not found in result set: " + idField);
                }
                columnNumbers.add(columnNumber);
            }

            if (idFields.size() == 0) {
                throw new IllegalArgumentException("Cannot find ID for: " + targetClass);
            } else if (idFields.size() == 1) {
                keyMapper = new SimpleKeyMapper(columnNumbers.get(0));
            } else {
                IdClass idClassAnnotation = targetClass.getAnnotation(IdClass.class);
                if (idClassAnnotation == null)
                    throw new IllegalStateException("Expected @IdClass annotation for composite primary key: " + targetClass);
                keyMapper = new CompoundKeyMapper(idClassAnnotation.value(), idFields, columnNumbers);
            }

            this.keyMapper = keyMapper;
        }
        return this.keyMapper;
    }

    private Integer findColumn(ResultSet rs, Field field) throws SQLException {
        Map<Integer, Field> columnToFieldMap = getColumnToFieldMap(rs);
        for (Map.Entry<Integer, Field> entry : columnToFieldMap.entrySet()) {
            if (entry.getValue().equals(field))
                return entry.getKey();
        }
        return null;
    }

    Map<Integer, Field> getColumnToFieldMap(ResultSet rs) throws SQLException {
        if (columnToFieldMap == null) {
            Map<Integer, Field> columnToFieldMap = Maps.newHashMap();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                String tableName = DatabaseUtils.getTableName(rsmd, i + 1);
                if (!tableName.equals(this.tableName))
                    continue;
                String columnName = rsmd.getColumnName(i + 1);
                Field field = fields.get(columnName);
                if (field == null)
                    continue;
                columnToFieldMap.put(i + 1, field);
            }
            this.columnToFieldMap = columnToFieldMap;
        }
        return this.columnToFieldMap;
    }

    void mapRowToObject(ResultSet rs, Object target) throws SQLException {
        Map<Integer, Field> columnToFieldMap = getColumnToFieldMap(rs);
        for (Map.Entry<Integer, Field> entry : columnToFieldMap.entrySet()) {
            Object value = rs.getObject(entry.getKey());
            Field field = entry.getValue();
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Error mapping field: " + field.getName(), e);
            }
        }
    }

    Object newInstance() {
        try {
            return targetClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Error building new instance of: " + targetClass);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Error building new instance of: " + targetClass);
        }
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
