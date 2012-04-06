package org.platformlayer.ui.shared.client.commons;


public abstract class Accessor<T, PropertyType> /* implements Function<T, PropertyType> */{

    public abstract PropertyType get(T o);

    public abstract void set(T o, PropertyType value);

}
