package org.platformlayer.service.openldap.shared;

import org.platformlayer.ui.shared.client.commons.Accessor;
import org.platformlayer.ui.shared.client.model.DomainModel;
import org.platformlayer.service.openldap.client.LdapDomainEditor;
import org.platformlayer.ui.shared.server.inject.InjectingServiceLocator;
import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import org.platformlayer.service.openldap.model.LdapDomain;

import org.platformlayer.service.openldap.server.LdapDomainGwtService;

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

@ProxyFor(value = LdapDomain.class)
@SkipInterfaceValidation
// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
public interface LdapDomainProxy extends EntityProxy {
    EntityProxyId<LdapDomainProxy> stableId();

    java.lang.String getOrganizationName();
    void setOrganizationName(java.lang.String value);
    
    public static final Accessor<LdapDomainProxy, java.lang.String> OrganizationName = new Accessor<LdapDomainProxy, java.lang.String>() {
        @Override
        public java.lang.String get(LdapDomainProxy o) {
            return o.getOrganizationName();
        }

        @Override
        public void set(LdapDomainProxy o, java.lang.String value) {
            o.setOrganizationName(value);
        }
    };

    long getVersion();
    void setVersion(long value);
    
    public static final Accessor<LdapDomainProxy, java.lang.Long> Version = new Accessor<LdapDomainProxy, java.lang.Long>() {
        @Override
        public java.lang.Long get(LdapDomainProxy o) {
            return o.getVersion();
        }

        @Override
        public void set(LdapDomainProxy o, java.lang.Long value) {
            o.setVersion(value);
        }
    };


    @Service(value = LdapDomainGwtService.class, locator = InjectingServiceLocator.class)
    @SkipInterfaceValidation
    public static interface LdapDomainRequest extends RequestContext, BaseEntityRequest<LdapDomainProxy> {
        Request<LdapDomainProxy> persist(LdapDomainProxy proxy);
    }

    public static interface EditorDriver extends RequestFactoryEditorDriver<LdapDomainProxy, LdapDomainEditor> {
    }

	public static interface LdapDomainRequestFactory {
    	LdapDomainProxy.LdapDomainRequest getLdapDomainRequest();
	}

    public static final DomainModel<LdapDomainProxy, LdapDomainRequest> Model = new DomainModel<LdapDomainProxy, LdapDomainRequest>(LdapDomainProxy.class) {
        @Override
        public Editor<LdapDomainProxy> buildEditor() {
            return new LdapDomainEditor();
        }

        @Override
        public LdapDomainRequest context(RequestFactory requestFactory) {
            return ((LdapDomainRequestFactory) requestFactory).getLdapDomainRequest();
        }

        @Override
        public void persist(LdapDomainRequest context, LdapDomainProxy item) {
            context.persist(item);
        }

        @Override
        public RequestFactoryEditorDriver<LdapDomainProxy, Editor<LdapDomainProxy>> buildEditorDriver() {
            return GWT.create(EditorDriver.class);
        }
    };
}
