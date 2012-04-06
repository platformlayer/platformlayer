package org.platformlayer.service.dns.shared;

import org.platformlayer.ui.shared.client.commons.Accessor;
import org.platformlayer.ui.shared.client.model.DomainModel;
import org.platformlayer.service.dns.client.DnsZoneEditor;
import org.platformlayer.ui.shared.server.inject.InjectingServiceLocator;
import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import org.platformlayer.service.dns.model.DnsZone;

import org.platformlayer.service.dns.server.DnsZoneGwtService;

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

@ProxyFor(value = DnsZone.class)
@SkipInterfaceValidation
// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
public interface DnsZoneProxy extends EntityProxy {
    EntityProxyId<DnsZoneProxy> stableId();

    java.lang.String getDnsName();
    void setDnsName(java.lang.String value);
    
    public static final Accessor<DnsZoneProxy, java.lang.String> DnsName = new Accessor<DnsZoneProxy, java.lang.String>() {
        @Override
        public java.lang.String get(DnsZoneProxy o) {
            return o.getDnsName();
        }

        @Override
        public void set(DnsZoneProxy o, java.lang.String value) {
            o.setDnsName(value);
        }
    };

    long getVersion();
    void setVersion(long value);
    
    public static final Accessor<DnsZoneProxy, java.lang.Long> Version = new Accessor<DnsZoneProxy, java.lang.Long>() {
        @Override
        public java.lang.Long get(DnsZoneProxy o) {
            return o.getVersion();
        }

        @Override
        public void set(DnsZoneProxy o, java.lang.Long value) {
            o.setVersion(value);
        }
    };


    @Service(value = DnsZoneGwtService.class, locator = InjectingServiceLocator.class)
    @SkipInterfaceValidation
    public static interface DnsZoneRequest extends RequestContext, BaseEntityRequest<DnsZoneProxy> {
        Request<DnsZoneProxy> persist(DnsZoneProxy proxy);
    }

    public static interface EditorDriver extends RequestFactoryEditorDriver<DnsZoneProxy, DnsZoneEditor> {
    }

	public static interface DnsZoneRequestFactory {
    	DnsZoneProxy.DnsZoneRequest getDnsZoneRequest();
	}

    public static final DomainModel<DnsZoneProxy, DnsZoneRequest> Model = new DomainModel<DnsZoneProxy, DnsZoneRequest>(DnsZoneProxy.class) {
        @Override
        public Editor<DnsZoneProxy> buildEditor() {
            return new DnsZoneEditor();
        }

        @Override
        public DnsZoneRequest context(RequestFactory requestFactory) {
            return ((DnsZoneRequestFactory) requestFactory).getDnsZoneRequest();
        }

        @Override
        public void persist(DnsZoneRequest context, DnsZoneProxy item) {
            context.persist(item);
        }

        @Override
        public RequestFactoryEditorDriver<DnsZoneProxy, Editor<DnsZoneProxy>> buildEditorDriver() {
            return GWT.create(EditorDriver.class);
        }
    };
}
