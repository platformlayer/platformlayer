package org.platformlayer;

public class SimpleThreadLocal<T> {
    static class CurrentItemHolder<T> {
        public T item;
    }

    private ThreadLocal<CurrentItemHolder<T>> currentThreadMap = new ThreadLocal<CurrentItemHolder<T>>() {
        @Override
        protected synchronized CurrentItemHolder<T> initialValue() {
            CurrentItemHolder<T> itemHolder = new CurrentItemHolder<T>();
            itemHolder.item = buildNewItem();
            return itemHolder;
        }
    };

    protected CurrentItemHolder<T> getCurrentItemHolder() {
        return currentThreadMap.get();
    }

    protected T buildNewItem() {
        return null;
    }

    public T getCurrent() {
        CurrentItemHolder<T> currentItemHolder = getCurrentItemHolder();
        return currentItemHolder.item;
    }

    public void push(T item) {
        CurrentItemHolder<T> currentItemHolder = getCurrentItemHolder();
        if (currentItemHolder.item != null) {
            throw new IllegalStateException();
        }

        currentItemHolder.item = item;
    }

    public void pop(T item) {
        CurrentItemHolder<T> currentItemHolder = getCurrentItemHolder();
        if (currentItemHolder.item != item) {
            throw new IllegalStateException();
        }

        currentItemHolder.item = null;
    }
}
