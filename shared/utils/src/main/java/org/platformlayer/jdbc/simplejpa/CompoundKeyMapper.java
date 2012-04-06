package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CompoundKeyMapper extends KeyMapper {

    private final List<Integer> columnNumbers;
    final Field[] pkFields;
    private final Class<?> pkType;

    public CompoundKeyMapper(Class<?> keyType, List<Field> sourceFields, List<Integer> columnNumbers) {
        this.pkType = keyType;
        this.columnNumbers = columnNumbers;
        this.pkFields = new Field[sourceFields.size()];
        for (int i = 0; i < sourceFields.size(); i++) {
            Field sourceField = sourceFields.get(i);
            String name = sourceField.getName();
            Field pkField;
            try {
                pkField = ReflectionUtils.findField(keyType, name);
                if (pkField == null)
                    throw new IllegalStateException("Expected matching field on PK class: " + keyType + "::" + name);
                pkField.setAccessible(true);
            } catch (SecurityException e) {
                throw new IllegalStateException("Cannot access field of PK class: " + keyType + "::" + name, e);
            }
            pkFields[i] = pkField;
        }
    }

    @Override
    public Object getKey(ResultSet rs) throws SQLException {
        try {
            Object pk = pkType.newInstance();
            for (int i = 0; i < pkFields.length; i++) {
                pkFields[i].set(pk, rs.getObject(columnNumbers.get(i)));
            }
            return pk;
        } catch (InstantiationException e) {
            throw new IllegalStateException("Error while mapping to PK: " + pkType, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Error while mapping to PK: " + pkType, e);
        }
    }
}
