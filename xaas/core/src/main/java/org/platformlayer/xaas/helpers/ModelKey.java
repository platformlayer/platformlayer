//package org.platformlayer.xaas.helpers;
//
//import org.platformlayer.EqualsUtils;
//import org.platformlayer.HasIdentityValues;
//import org.platformlayer.xaas.services.ModelClass;
//
//public class ModelKey implements HasIdentityValues {
//    final ModelClass<?> modelClass;
//    final ManagedItemId id;
//
//    public ModelKey(ModelClass<?> modelClass, ManagedItemId id) {
//        super();
//        this.modelClass = modelClass;
//        this.id = id;
//    }
//
//    @Override
//    public String toString() {
//        return "ModelKey [modelClass=" + modelClass + ", id=" + id + "]";
//    }
//
//    @Override
//    public int hashCode() {
//        return EqualsUtils.computeHashCode(this);
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return EqualsUtils.equals(this, obj);
//    }
//
//    @Override
//    public Object[] getIdentityValues() {
//        return new Object[] { modelClass, id };
//    }
//
//    public ModelClass<?> getModelClass() {
//        return modelClass;
//    }
//
//    public ManagedItemId getId() {
//        return id;
//    }
// }
