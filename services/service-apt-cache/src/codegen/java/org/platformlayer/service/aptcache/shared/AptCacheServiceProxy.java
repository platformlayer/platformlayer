package org.platformlayer.service.aptcache.shared;

import org.platformlayer.ui.shared.client.commons.Accessor;
import org.platformlayer.ui.shared.client.model.DomainModel;
import org.platformlayer.service.aptcache.client.AptCacheServiceEditor;
import org.platformlayer.ui.shared.server.inject.InjectingServiceLocator;
import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import org.platformlayer.service.aptcache.model.AptCacheService;

import org.platformlayer.service.aptcache.server.AptCacheServiceGwtService;

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

@ProxyFor(value = AptCacheService.class)
@SkipInterfaceValidation
// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
public interface AptCacheServiceProxy extends EntityProxy {
    EntityProxyId<AptCacheServiceProxy> stableId();

    java.lang.String getDnsName();
    void setDnsName(java.lang.String value);
    
    public static final Accessor<AptCacheServiceProxy, java.lang.String> DnsName = new Accessor<AptCacheServiceProxy, java.lang.String>() {
        @Override
        public java.lang.String get(AptCacheServiceProxy o) {
            return o.getDnsName();
        }

        @Override
        public void set(AptCacheServiceProxy o, java.lang.String value) {
            o.setDnsName(value);
        }
    };

    long getVersion();
    void setVersion(long value);
    
    public static final Accessor<AptCacheServiceProxy, java.lang.Long> Version = new Accessor<AptCacheServiceProxy, java.lang.Long>() {
        @Override
        public java.lang.Long get(AptCacheServiceProxy o) {
            return o.getVersion();
        }

        @Override
        public void set(AptCacheServiceProxy o, java.lang.Long value) {
            o.setVersion(value);
        }
    };


    @Service(value = AptCacheServiceGwtService.class, locator = InjectingServiceLocator.class)
    @SkipInterfaceValidation
    public static interface AptCacheServiceRequest extends RequestContext, BaseEntityRequest<AptCacheServiceProxy> {
        Request<AptCacheServiceProxy> persist(AptCacheServiceProxy proxy);
    }

    public static interface EditorDriver extends RequestFactoryEditorDriver<AptCacheServiceProxy, AptCacheServiceEditor> {
    }

	public static interface AptCacheServiceRequestFactory {
    	AptCacheServiceProxy.AptCacheServiceRequest getAptCacheServiceRequest();
	}

    public static final DomainModel<AptCacheServiceProxy, AptCacheServiceRequest> Model = new DomainModel<AptCacheServiceProxy, AptCacheServiceRequest>(AptCacheServiceProxy.class) {
        @Override
        public Editor<AptCacheServiceProxy> buildEditor() {
            return new AptCacheServiceEditor();
        }

        @Override
        public AptCacheServiceRequest context(RequestFactory requestFactory) {
            return ((AptCacheServiceRequestFactory) requestFactory).getAptCacheServiceRequest();
        }

        @Override
        public void persist(AptCacheServiceRequest context, AptCacheServiceProxy item) {
            context.persist(item);
        }

        @Override
        public RequestFactoryEditorDriver<AptCacheServiceProxy, Editor<AptCacheServiceProxy>> buildEditorDriver() {
            return GWT.create(EditorDriver.class);
        }
    };
}
