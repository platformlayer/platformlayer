//package org.platformlayer.ui.web.shared;
//
//import java.util.List;
//
//import org.platformlayer.service.dns.model.DnsRecord;
//import org.platformlayer.ui.web.client.commons.Accessor;
//
//import com.google.web.bindery.requestfactory.shared.EntityProxy;
//import com.google.web.bindery.requestfactory.shared.EntityProxyId;
//import com.google.web.bindery.requestfactory.shared.ProxyFor;
//import com.google.web.bindery.requestfactory.shared.SkipInterfaceValidation;
//
//@ProxyFor(value = DnsRecord.class)
//@SkipInterfaceValidation
//// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
//public interface DnsRecordProxy extends EntityProxy {
//    // Boilerplate
//    EntityProxyId<DnsRecordProxy> stableId();
//
//    // Getters / Setters
//    String getDnsName();
//
//    void setDnsName(String dnsName);
//
//    String getRecordType();
//
//    void setRecordType(String v);
//
//    List<String> getAddress();
//
//    void setAddress(List<String> v);
//
//    static final Accessor<DnsRecordProxy, String> DnsName = new Accessor<DnsRecordProxy, String>() {
//        public String get(DnsRecordProxy o) {
//            return o.getDnsName();
//        }
//
//        @Override
//        public void set(DnsRecordProxy o, String value) {
//            o.setDnsName(value);
//        }
//    };
//
//    static final Accessor<DnsRecordProxy, String> RecordType = new Accessor<DnsRecordProxy, String>() {
//        public String get(DnsRecordProxy o) {
//            return o.getRecordType();
//        }
//
//        @Override
//        public void set(DnsRecordProxy o, String value) {
//            o.setRecordType(value);
//        }
//    };
//
//    static final Accessor<DnsRecordProxy, List<String>> Address = new Accessor<DnsRecordProxy, List<String>>() {
//        public List<String> get(DnsRecordProxy o) {
//            return o.getAddress();
//        }
//
//        @Override
//        public void set(DnsRecordProxy o, List<String> value) {
//            o.setAddress(value);
//        }
//    };
// }
