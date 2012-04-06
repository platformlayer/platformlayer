package org.platformlayer.service.dnsresolver.shared;

import org.platformlayer.ui.shared.client.commons.Accessor;
import org.platformlayer.ui.shared.client.model.DomainModel;
import org.platformlayer.service.dnsresolver.client.DnsResolverServiceEditor;
import org.platformlayer.ui.shared.server.inject.InjectingServiceLocator;
import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import org.platformlayer.service.dnsresolver.model.DnsResolverService;

import org.platformlayer.service.dnsresolver.server.DnsResolverServiceGwtService;

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

@ProxyFor(value = DnsResolverService.class)
@SkipInterfaceValidation
// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
public interface DnsResolverServiceProxy extends EntityProxy {
    EntityProxyId<DnsResolverServiceProxy> stableId();

    java.lang.String getDnsName();
    void setDnsName(java.lang.String value);
    
    public static final Accessor<DnsResolverServiceProxy, java.lang.String> DnsName = new Accessor<DnsResolverServiceProxy, java.lang.String>() {
        @Override
        public java.lang.String get(DnsResolverServiceProxy o) {
            return o.getDnsName();
        }

        @Override
        public void set(DnsResolverServiceProxy o, java.lang.String value) {
            o.setDnsName(value);
        }
    };

    long getVersion();
    void setVersion(long value);
    
    public static final Accessor<DnsResolverServiceProxy, java.lang.Long> Version = new Accessor<DnsResolverServiceProxy, java.lang.Long>() {
        @Override
        public java.lang.Long get(DnsResolverServiceProxy o) {
            return o.getVersion();
        }

        @Override
        public void set(DnsResolverServiceProxy o, java.lang.Long value) {
            o.setVersion(value);
        }
    };


    @Service(value = DnsResolverServiceGwtService.class, locator = InjectingServiceLocator.class)
    @SkipInterfaceValidation
    public static interface DnsResolverServiceRequest extends RequestContext, BaseEntityRequest<DnsResolverServiceProxy> {
        Request<DnsResolverServiceProxy> persist(DnsResolverServiceProxy proxy);
    }

    public static interface EditorDriver extends RequestFactoryEditorDriver<DnsResolverServiceProxy, DnsResolverServiceEditor> {
    }

	public static interface DnsResolverServiceRequestFactory {
    	DnsResolverServiceProxy.DnsResolverServiceRequest getDnsResolverServiceRequest();
	}

    public static final DomainModel<DnsResolverServiceProxy, DnsResolverServiceRequest> Model = new DomainModel<DnsResolverServiceProxy, DnsResolverServiceRequest>(DnsResolverServiceProxy.class) {
        @Override
        public Editor<DnsResolverServiceProxy> buildEditor() {
            return new DnsResolverServiceEditor();
        }

        @Override
        public DnsResolverServiceRequest context(RequestFactory requestFactory) {
            return ((DnsResolverServiceRequestFactory) requestFactory).getDnsResolverServiceRequest();
        }

        @Override
        public void persist(DnsResolverServiceRequest context, DnsResolverServiceProxy item) {
            context.persist(item);
        }

        @Override
        public RequestFactoryEditorDriver<DnsResolverServiceProxy, Editor<DnsResolverServiceProxy>> buildEditorDriver() {
            return GWT.create(EditorDriver.class);
        }
    };
}
