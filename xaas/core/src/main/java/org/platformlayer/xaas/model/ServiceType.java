//package org.platformlayer.xaas.model;
//
//import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
//
//@XmlJavaTypeAdapter(ServiceTypeXmlAdapter.class)
//public class ServiceType {
//    final String key;
//
//    public ServiceType(String key) {
//        super();
//        this.key = key;
//    }
//
//    public String getKey() {
//        return key;
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((key == null) ? 0 : key.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        ServiceType other = (ServiceType) obj;
//        if (key == null) {
//            if (other.key != null)
//                return false;
//        } else if (!key.equals(other.key))
//            return false;
//        return true;
//    }
//
//    @Override
//    public String toString() {
//        return "ServiceType [key=" + key + "]";
//    }
// }
