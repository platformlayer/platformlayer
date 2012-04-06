//package org.platformlayer.ui.web.shared;
//
//import org.platformlayer.service.dns.client.DnsServerEditor;
//import org.platformlayer.service.dns.model.DnsServer;
//import org.platformlayer.ui.web.client.App;
//import org.platformlayer.ui.web.client.commons.Accessor;
//import org.platformlayer.ui.web.client.model.DomainModel;
//import org.platformlayer.ui.web.server.DnsServerGwtService;
//import org.platformlayer.ui.web.server.inject.InjectingServiceLocator;
//
//import com.google.gwt.core.client.GWT;
//import com.google.gwt.editor.client.Editor;
//import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
//import com.google.web.bindery.requestfactory.shared.EntityProxy;
//import com.google.web.bindery.requestfactory.shared.EntityProxyId;
//import com.google.web.bindery.requestfactory.shared.ProxyFor;
//import com.google.web.bindery.requestfactory.shared.Request;
//import com.google.web.bindery.requestfactory.shared.RequestContext;
//import com.google.web.bindery.requestfactory.shared.Service;
//import com.google.web.bindery.requestfactory.shared.SkipInterfaceValidation;
//
//@ProxyFor(value = DnsServer.class)
//@SkipInterfaceValidation
//// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
//public interface DnsServerProxy extends EntityProxy {
//    // Boilerplate
//    EntityProxyId<DnsServerProxy> stableId();
//
//    // Getters / Setters
//    String getDnsName();
//
//    void setDnsName(String dnsName);
//
//    static final Accessor<DnsServerProxy, String> DnsName = new Accessor<DnsServerProxy, String>() {
//        @Override
//        public String get(DnsServerProxy o) {
//            return o.getDnsName();
//        }
//
//        @Override
//        public void set(DnsServerProxy o, String value) {
//            o.setDnsName(value);
//        }
//    };
//
//    @Service(value = DnsServerGwtService.class, locator = InjectingServiceLocator.class)
//    @SkipInterfaceValidation
//    public interface DnsServerRequest extends RequestContext, BaseEntityRequest<DnsServerProxy> {
//        Request<DnsServerProxy> persist(DnsServerProxy proxy);
//    }
//
//    public static interface EditorDriver extends RequestFactoryEditorDriver<DnsServerProxy, DnsServerEditor> {
//
//    }
//
//    static final DomainModel<DnsServerProxy, DnsServerRequest> Model = new DomainModel<DnsServerProxy, DnsServerRequest>(DnsServerProxy.class) {
//        @Override
//        public Editor<DnsServerProxy> buildEditor() {
//            return new DnsServerEditor();
//        }
//
//        @Override
//        public DnsServerRequest context() {
//            return App.injector.getRequestFactory().dnsServerRequest();
//        }
//
//        @Override
//        public void persist(DnsServerRequest context, DnsServerProxy item) {
//            context.persist(item);
//        }
//
//        @Override
//        public RequestFactoryEditorDriver<DnsServerProxy, Editor<DnsServerProxy>> buildEditorDriver() {
//            return GWT.create(EditorDriver.class);
//        }
//    };
// }
