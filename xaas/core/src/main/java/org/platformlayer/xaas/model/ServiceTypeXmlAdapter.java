//package org.platformlayer.xaas.model;
//
//import javax.xml.bind.annotation.adapters.XmlAdapter;
//
//public class ServiceTypeXmlAdapter extends XmlAdapter<String, ServiceType> {
//
//    @Override
//    public ServiceType unmarshal(String v) throws Exception {
//        if (v == null)
//            return null;
//        return new ServiceType(v);
//    }
//
//    @Override
//    public String marshal(ServiceType v) throws Exception {
//        if (v == null)
//            return null;
//        return v.getKey();
//    }
//
// }
