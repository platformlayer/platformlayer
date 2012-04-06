package org.platformlayer.service.dns.shared;

import org.platformlayer.ui.shared.client.commons.Accessor;
import org.platformlayer.ui.shared.client.model.DomainModel;
import org.platformlayer.service.dns.client.DnsServerEditor;
import org.platformlayer.ui.shared.server.inject.InjectingServiceLocator;
import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import org.platformlayer.service.dns.model.DnsServer;

import org.platformlayer.service.dns.server.DnsServerGwtService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.google.web.bindery.requestfactory.shared.Service;
import com.google.web.bindery.requestfactory.shared.SkipInterfaceValidation;

@ProxyFor(value = DnsServer.class)
@SkipInterfaceValidation
// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
public interface DnsServerProxy extends EntityProxy {
    EntityProxyId<DnsServerProxy> stableId();

    java.lang.String getDnsName();
    void setDnsName(java.lang.String value);
    
    public static final Accessor<DnsServerProxy, java.lang.String> DnsName = new Accessor<DnsServerProxy, java.lang.String>() {
        @Override
        public java.lang.String get(DnsServerProxy o) {
            return o.getDnsName();
        }

        @Override
        public void set(DnsServerProxy o, java.lang.String value) {
            o.setDnsName(value);
        }
    };

    long getVersion();
    void setVersion(long value);
    
    public static final Accessor<DnsServerProxy, java.lang.Long> Version = new Accessor<DnsServerProxy, java.lang.Long>() {
        @Override
        public java.lang.Long get(DnsServerProxy o) {
            return o.getVersion();
        }

        @Override
        public void set(DnsServerProxy o, java.lang.Long value) {
            o.setVersion(value);
        }
    };


    @Service(value = DnsServerGwtService.class, locator = InjectingServiceLocator.class)
    @SkipInterfaceValidation
    public static interface DnsServerRequest extends RequestContext, BaseEntityRequest<DnsServerProxy> {
        Request<DnsServerProxy> persist(DnsServerProxy proxy);
    }

    public static interface EditorDriver extends RequestFactoryEditorDriver<DnsServerProxy, DnsServerEditor> {
    }

	public static interface DnsServerRequestFactory {
    	DnsServerProxy.DnsServerRequest getDnsServerRequest();
	}

    public static final DomainModel<DnsServerProxy, DnsServerRequest> Model = new DomainModel<DnsServerProxy, DnsServerRequest>(DnsServerProxy.class) {
        @Override
        public Editor<DnsServerProxy> buildEditor() {
            return new DnsServerEditor();
        }

        @Override
        public DnsServerRequest context(RequestFactory requestFactory) {
            return ((DnsServerRequestFactory) requestFactory).getDnsServerRequest();
        }

        @Override
        public void persist(DnsServerRequest context, DnsServerProxy item) {
            context.persist(item);
        }

        @Override
        public RequestFactoryEditorDriver<DnsServerProxy, Editor<DnsServerProxy>> buildEditorDriver() {
            return GWT.create(EditorDriver.class);
        }
    };
}
