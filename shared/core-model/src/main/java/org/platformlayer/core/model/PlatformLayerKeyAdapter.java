//package org.platformlayer.core.model;
//
//import javax.xml.bind.annotation.XmlTransient;
//import javax.xml.bind.annotation.adapters.XmlAdapter;
//
//@XmlTransient
//public class PlatformLayerKeyAdapter extends XmlAdapter<String, PlatformLayerKey> {
//    public PlatformLayerKey unmarshal(String val) throws Exception {
//        return PlatformLayerKey.parse(val);
//    }
//
//    public String marshal(PlatformLayerKey val) throws Exception {
//        return val.getUrl();
//    }
// }