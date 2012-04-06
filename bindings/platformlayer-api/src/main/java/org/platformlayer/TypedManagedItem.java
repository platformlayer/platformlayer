//package org.platformlayer;
//
//import org.platformlayer.core.model.ManagedItem;
//import org.platformlayer.core.model.ManagedItemState;
//import org.platformlayer.core.model.Tags;
//import org.platformlayer.ids.ItemType;
//import org.platformlayer.ids.ModelKey;
//import org.platformlayer.ids.ServiceType;
//import org.platformlayer.xml.JaxbHelper;
//
//public class TypedManagedItem<T> {
//    ManagedItem item;
//
//    ModelKey modelKey;
//
//    Class<?> javaClass;
//
//    public TypedManagedItem(Class<?> javaClass, ModelKey modelKey, ManagedItem item) {
//        this.javaClass = javaClass;
//        this.modelKey = modelKey;
//        this.item = item;
//    }
//
//    public T getModel() {
//        throw new UnsupportedOperationException();
//    }
//
//    public static <T> TypedManagedItem<T> build(Class<T> javaClass, ModelKey modelKey, ManagedItem managedItem) {
//        return new TypedManagedItem<T>(javaClass, modelKey, managedItem);
//    }
//
//    public ManagedItemState getState() {
//        return item.getState();
//    }
//
//    public Tags getTags() {
//        return item.getTags();
//    }
//
//    public ManagedItem getSerialized() {
//        return item;
//    }
//
//    public String getKey() {
//        return item.getKey();
//    }
//
//    public Class<?> getJavaClass() {
//        return javaClass;
//    }
//
//    public String getUrl() {
//        ModelKey modelKey = getModelKey();
//        return UrlUtils.toUrl(modelKey);
//    }
//
//    public JaxbHelper getJaxbHelper() {
//        return JaxbHelper.get(getJavaClass());
//    }
//
//    public ServiceType getServiceType() {
//        return getModelKey().serviceType;
//    }
//
//    public ItemType getItemType() {
//        return getModelKey().itemType;
//    }
//
//    public void setState(ManagedItemState active) {
//
//    }
//
//    public ModelKey getModelKey() {
//        return modelKey;
//    }
//
//    public String getModelData() {
//        return item.getModelData();
//    }
//
//    public void setModelData(String xml) {
//        item.modelData = xml;
//    }
// }
