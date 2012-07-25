package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FieldMap {
	final Class<?> clazz;

	class FieldInfo {
		final Field field;
		final boolean isId;

		public FieldInfo(Field field, boolean isId) {
			super();
			this.field = field;
			this.isId = isId;
		}

	}

	final Map<String, FieldInfo> fields = Maps.newHashMap();
	final List<Field> idFields = Lists.newArrayList();
	final DatabaseNameMapping nameMapping;
	final String tableName;
	final List<String> columnNames;

	public FieldMap(DatabaseNameMapping nameMapping, Class<?> clazz) {
		this.nameMapping = nameMapping;
		this.clazz = clazz;
		this.tableName = nameMapping.getTableName(clazz);

		this.columnNames = Lists.newArrayList();

		discoverFields();
	}

	void discoverFields() {
		for (Field field : clazz.getFields()) {
			String columnName = nameMapping.getColumnName(field);
			columnNames.add(columnName);
			boolean isId = false;

			Id idAnnotation = field.getAnnotation(Id.class);
			if (idAnnotation != null) {
				idFields.add(field);
				isId = true;
			}

			fields.put(columnName, new FieldInfo(field, isId));
		}
	}

	public static FieldMap build(DatabaseNameMapping nameMapping, Class<?> clazz) {
		return new FieldMap(nameMapping, clazz);
	}

	public List<Field> getIdFields() {
		return idFields;
	}

	public Field getFieldForColumn(String columnName) {
		FieldInfo fieldInfo = fields.get(columnName);
		if (fieldInfo == null) {
			return null;
		}
		return fieldInfo.field;
	}

	public String getTableName() {
		return tableName;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public boolean isId(String columnName) {
		FieldInfo fieldInfo = fields.get(columnName);
		assert fieldInfo != null;
		return fieldInfo.isId;
	}
}
