//package org.platformlayer.ui.web.shared;
//
//import org.platformlayer.service.dns.model.DnsZone;
//import org.platformlayer.ui.web.client.commons.Accessor;
//
//import com.google.web.bindery.requestfactory.shared.EntityProxy;
//import com.google.web.bindery.requestfactory.shared.EntityProxyId;
//import com.google.web.bindery.requestfactory.shared.ProxyFor;
//import com.google.web.bindery.requestfactory.shared.SkipInterfaceValidation;
//
//@ProxyFor(value = DnsZone.class)
//@SkipInterfaceValidation
//// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
//public interface DnsZoneProxy extends EntityProxy {
//    // Boilerplate
//    EntityProxyId<DnsZoneProxy> stableId();
//
//    // Getters / Setters
//    String getDnsName();
//
//    void setDnsName(String dnsName);
//
//    static final Accessor<DnsZoneProxy, String> DnsName = new Accessor<DnsZoneProxy, String>() {
//        @Override
//        public String get(DnsZoneProxy o) {
//            return o.getDnsName();
//        }
//
//        @Override
//        public void set(DnsZoneProxy o, String value) {
//            o.setDnsName(value);
//        }
//    };
// }
