//package org.platformlayer.xaas.helpers;
//
//import javax.xml.bind.annotation.adapters.XmlAdapter;
//
//public class ItemTypeXmlAdapter extends XmlAdapter<String, ItemType> {
//
//    @Override
//    public ItemType unmarshal(String v) throws Exception {
//        if (v == null)
//            return null;
//        return new ItemType(v);
//    }
//
//    @Override
//    public String marshal(ItemType v) throws Exception {
//        if (v == null)
//            return null;
//        return v.getKey();
//    }
//
// }
