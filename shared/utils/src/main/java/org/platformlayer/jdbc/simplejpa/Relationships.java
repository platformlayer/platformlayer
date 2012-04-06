package org.platformlayer.jdbc.simplejpa;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.google.common.collect.Lists;

public class Relationships {
    private final Class<?> thisClass;

    public Relationships(Class<?> sourceClass) {
        this.thisClass = sourceClass;
        findRelationships();
    }

    static class ManyToOneRelationship {
        final Class<?> thisClass;
        final Field thisField;
        final Field foreignKeyField;
        final Class<?> foreignType;

        public ManyToOneRelationship(Class<?> thisClass, Field thisField, Class<?> foreignType, Field foreignKeyField) {
            super();
            this.thisClass = thisClass;
            this.thisField = thisField;
            this.foreignKeyField = foreignKeyField;
            this.foreignType = foreignType;
        }

        public void doMap(JoinedQueryResult mapResult) throws IllegalArgumentException, IllegalAccessException {
            for (Object thisObject : mapResult.getAll(thisClass)) {
                Object foreignKey = foreignKeyField.get(thisObject);
                Object foreignObject = mapResult.get(foreignType, foreignKey);
                thisField.set(thisObject, foreignObject);
            }
        }
    }

    static class OneToManyRelationship {
        final Class<?> thisClass;
        final Field thisField;
        final Class<?> manyClass;
        final Field manyField;
        final Comparator comparator;

        public OneToManyRelationship(Class<?> thisClass, Field thisField, Class<?> manyClass, Field manyField, Comparator comparator) {
            super();
            this.thisClass = thisClass;
            this.thisField = thisField;
            this.manyClass = manyClass;
            this.manyField = manyField;
            this.comparator = comparator;
        }

        public void doMap(JoinedQueryResult mapResult) throws IllegalArgumentException, IllegalAccessException {
            for (Object oneItem : mapResult.getAll(thisClass)) {
                thisField.set(oneItem, new ArrayList());
            }

            for (Object manyItem : mapResult.getAll(manyClass)) {
                Object oneKey = manyField.get(manyItem);
                Object oneItem = mapResult.get(thisClass, oneKey);
                Collection oneCollection = (Collection) thisField.get(oneItem);
                oneCollection.add(manyItem);
            }

            if (comparator != null) {
                for (Object oneItem : mapResult.getAll(thisClass)) {
                    List oneCollection = (List) thisField.get(oneItem);
                    if (oneCollection.size() <= 1)
                        continue;
                    Collections.sort(oneCollection, comparator);
                }
            }
        }
    }

    final List<OneToManyRelationship> oneToManyRelationships = Lists.newArrayList();
    final List<ManyToOneRelationship> manyToOneRelationships = Lists.newArrayList();

    public void doMap(JoinedQueryResult mapResult) throws IllegalArgumentException, IllegalAccessException {
        for (OneToManyRelationship oneToManyRelationship : oneToManyRelationships) {
            oneToManyRelationship.doMap(mapResult);
        }

        for (ManyToOneRelationship manyToOneRelationship : manyToOneRelationships) {
            manyToOneRelationship.doMap(mapResult);
        }
    }

    private void findRelationships() {
        try {
            for (Field thisField : thisClass.getFields()) {
                OneToMany oneToManyAnnotation = thisField.getAnnotation(OneToMany.class);
                if (oneToManyAnnotation != null) {
                    Class<?> manyClass = oneToManyAnnotation.targetEntity();
                    Field manyField = manyClass.getField(oneToManyAnnotation.mappedBy());

                    JoinColumn manyFieldJoinColumn = manyField.getAnnotation(JoinColumn.class);
                    if (manyFieldJoinColumn != null) {
                        manyField = manyClass.getField(manyFieldJoinColumn.name());
                    }

                    Comparator comparator = null;
                    OrderBy orderByAnnotation = thisField.getAnnotation(OrderBy.class);
                    if (orderByAnnotation != null) {
                        Field orderByField = manyClass.getField(orderByAnnotation.value());
                        comparator = new FieldComparator(orderByField);
                    }

                    oneToManyRelationships.add(new OneToManyRelationship(thisClass, thisField, manyClass, manyField, comparator));
                }

                ManyToOne manyToOneAnnotation = thisField.getAnnotation(ManyToOne.class);
                if (manyToOneAnnotation != null) {
                    JoinColumn joinColumnAnnotation = thisField.getAnnotation(JoinColumn.class);
                    Class<?> foreignType = thisField.getType();
                    Field foreignKeyField = thisClass.getField(joinColumnAnnotation.name());

                    manyToOneRelationships.add(new ManyToOneRelationship(thisClass, thisField, foreignType, foreignKeyField));
                }
            }
        } catch (SecurityException e) {
            throw new IllegalStateException("Error while discovering JPA relationships on " + thisClass, e);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Error while discovering JPA relationships on " + thisClass, e);
        }
    }

}
