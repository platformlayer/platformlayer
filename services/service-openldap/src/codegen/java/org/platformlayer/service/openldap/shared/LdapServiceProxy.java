package org.platformlayer.service.openldap.shared;

import org.platformlayer.ui.shared.client.commons.Accessor;
import org.platformlayer.ui.shared.client.model.DomainModel;
import org.platformlayer.service.openldap.client.LdapServiceEditor;
import org.platformlayer.ui.shared.server.inject.InjectingServiceLocator;
import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import org.platformlayer.service.openldap.model.LdapService;

import org.platformlayer.service.openldap.server.LdapServiceGwtService;

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

@ProxyFor(value = LdapService.class)
@SkipInterfaceValidation
// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
public interface LdapServiceProxy extends EntityProxy {
    EntityProxyId<LdapServiceProxy> stableId();

    java.lang.String getDnsName();
    void setDnsName(java.lang.String value);
    
    public static final Accessor<LdapServiceProxy, java.lang.String> DnsName = new Accessor<LdapServiceProxy, java.lang.String>() {
        @Override
        public java.lang.String get(LdapServiceProxy o) {
            return o.getDnsName();
        }

        @Override
        public void set(LdapServiceProxy o, java.lang.String value) {
            o.setDnsName(value);
        }
    };

    long getVersion();
    void setVersion(long value);
    
    public static final Accessor<LdapServiceProxy, java.lang.Long> Version = new Accessor<LdapServiceProxy, java.lang.Long>() {
        @Override
        public java.lang.Long get(LdapServiceProxy o) {
            return o.getVersion();
        }

        @Override
        public void set(LdapServiceProxy o, java.lang.Long value) {
            o.setVersion(value);
        }
    };


    @Service(value = LdapServiceGwtService.class, locator = InjectingServiceLocator.class)
    @SkipInterfaceValidation
    public static interface LdapServiceRequest extends RequestContext, BaseEntityRequest<LdapServiceProxy> {
        Request<LdapServiceProxy> persist(LdapServiceProxy proxy);
    }

    public static interface EditorDriver extends RequestFactoryEditorDriver<LdapServiceProxy, LdapServiceEditor> {
    }

	public static interface LdapServiceRequestFactory {
    	LdapServiceProxy.LdapServiceRequest getLdapServiceRequest();
	}

    public static final DomainModel<LdapServiceProxy, LdapServiceRequest> Model = new DomainModel<LdapServiceProxy, LdapServiceRequest>(LdapServiceProxy.class) {
        @Override
        public Editor<LdapServiceProxy> buildEditor() {
            return new LdapServiceEditor();
        }

        @Override
        public LdapServiceRequest context(RequestFactory requestFactory) {
            return ((LdapServiceRequestFactory) requestFactory).getLdapServiceRequest();
        }

        @Override
        public void persist(LdapServiceRequest context, LdapServiceProxy item) {
            context.persist(item);
        }

        @Override
        public RequestFactoryEditorDriver<LdapServiceProxy, Editor<LdapServiceProxy>> buildEditorDriver() {
            return GWT.create(EditorDriver.class);
        }
    };
}
