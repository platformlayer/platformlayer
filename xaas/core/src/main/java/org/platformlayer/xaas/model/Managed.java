//package org.platformlayer.xaas.model;
//
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlRootElement;
//import javax.xml.bind.annotation.XmlTransient;
//
//import org.platformlayer.xaas.helpers.ManagedItemId;
//import org.platformlayer.xaas.helpers.ModelKey;
//import org.platformlayer.xaas.services.ModelClass;
//import org.platformlayer.xml.JaxbHelper;
//import org.platformlayer.xml.UnmarshalException;
//import org.platformlayer.xml.XmlHelper;
//import org.w3c.dom.Document;
//
//import com.google.common.base.Strings;
//
//@XmlRootElement
//@XmlAccessorType(XmlAccessType.FIELD)
//public class Managed<T> {
//    int id;
//
//    @XmlTransient
//    ModelClass<T> modelClass;
//
//    String modelData;
//
//    ManagedItemState state;
//
//    @XmlTransient
//    T model;
//
//    Tags tags;
//
//    public Managed(ModelClass<T> modelClass, int id, ManagedItemState state, String modelData) {
//        this.modelClass = modelClass;
//        this.id = id;
//        this.state = state;
//        this.modelData = modelData;
//    }
//
//    public ManagedItemState getState() {
//        return state;
//    }
//
//    public void buildModel(JaxbHelper jaxbHelper, Class<T> clazz) {
//        if (model == null) {
//            if (Strings.isNullOrEmpty(modelData)) {
//                throw new IllegalArgumentException("No xml data present");
//            }
//
//            try {
//                model = JaxbHelper.deserializeXmlObject(modelData, clazz);
//            } catch (UnmarshalException e) {
//                throw new IllegalStateException("Error deserializing item", e);
//            }
//        }
//    }
//
//    public T getModel() {
//        if (model == null) {
//            if (Strings.isNullOrEmpty(modelData)) {
//                throw new IllegalArgumentException("No xml data present");
//            }
//
//            if (modelClass == null) {
//                throw new IllegalArgumentException("modelClass not present");
//            }
//
//            try {
//                model = modelClass.deserializeXml(modelData);
//            } catch (JAXBException e) {
//                throw new IllegalStateException("Error deserializing item", e);
//            }
//        }
//        return model;
//    }
//
//    public <V> Managed<V> as(ModelClass<V> clazz) {
//        if (modelClass.equals(clazz))
//            return (Managed<V>) this;
//        throw new IllegalArgumentException("Managed item type mismatch: expected " + clazz + ", got " + modelClass);
//    }
//
//    public ModelClass<T> getModelClass() {
//        return modelClass;
//    }
//
//    public void setModelClass(ModelClass<T> modelClass) {
//        this.modelClass = modelClass;
//    }
//
//    public String getModelData() {
//        return modelData;
//    }
//
//    public void setModelData(String modelData) {
//        this.modelData = modelData;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public void setState(ManagedItemState state) {
//        this.state = state;
//    }
//
//    public ModelKey asModelKey() {
//        return new ModelKey(getModelClass(), new ManagedItemId(getId()));
//    }
//
//    public Tags getTags() {
//        if (tags == null) {
//            tags = new Tags();
//        }
//        return tags;
//    }
//
//    public void setTags(Tags tags) {
//        this.tags = tags;
//    }
//
//    public String getConductorId() {
//        return getXmlNamespace() + "/" + getXmlElementName() + "/" + id;
//    }
//
//    private String getXmlNamespace() {
//        if (model == null && modelClass == null) {
//            Document dom;
//            try {
//                dom = XmlHelper.parseXmlDocument(modelData, true);
//            } catch (Exception e) {
//                throw new IllegalArgumentException("Error parsing XML data", e);
//            }
//            String namespaceURI = dom.getDocumentElement().getNamespaceURI();
//            return namespaceURI;
//        }
//        T model = getModel();
//        JaxbHelper jaxbHelper = JaxbHelper.get(model.getClass());
//        String primaryNamespace = jaxbHelper.getPrimaryNamespace();
//        return primaryNamespace;
//    }
//
//    private String getXmlElementName() {
//        if (model == null && modelClass == null) {
//            Document dom;
//            try {
//                dom = XmlHelper.parseXmlDocument(modelData, true);
//            } catch (Exception e) {
//                throw new IllegalArgumentException("Error parsing XML data", e);
//            }
//            String nodeName = dom.getDocumentElement().getNodeName();
//            return nodeName;
//        }
//        T model = getModel();
//        JaxbHelper jaxbHelper = JaxbHelper.get(model.getClass());
//        String xmlElementName = jaxbHelper.getXmlElementName();
//        return xmlElementName;
//    }
//
//    @Override
//    public String toString() {
//        return getConductorId();
//    }
//
// }
