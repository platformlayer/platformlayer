package org.platformlayer.service.dns.shared;

import org.platformlayer.ui.shared.client.commons.Accessor;
import org.platformlayer.ui.shared.client.model.DomainModel;
import org.platformlayer.service.dns.client.DnsRecordEditor;
import org.platformlayer.ui.shared.server.inject.InjectingServiceLocator;
import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import org.platformlayer.service.dns.model.DnsRecord;

import org.platformlayer.service.dns.server.DnsRecordGwtService;

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

@ProxyFor(value = DnsRecord.class)
@SkipInterfaceValidation
// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
public interface DnsRecordProxy extends EntityProxy {
    EntityProxyId<DnsRecordProxy> stableId();

    java.lang.String getDnsName();
    void setDnsName(java.lang.String value);
    
    public static final Accessor<DnsRecordProxy, java.lang.String> DnsName = new Accessor<DnsRecordProxy, java.lang.String>() {
        @Override
        public java.lang.String get(DnsRecordProxy o) {
            return o.getDnsName();
        }

        @Override
        public void set(DnsRecordProxy o, java.lang.String value) {
            o.setDnsName(value);
        }
    };

    java.lang.String getRecordType();
    void setRecordType(java.lang.String value);
    
    public static final Accessor<DnsRecordProxy, java.lang.String> RecordType = new Accessor<DnsRecordProxy, java.lang.String>() {
        @Override
        public java.lang.String get(DnsRecordProxy o) {
            return o.getRecordType();
        }

        @Override
        public void set(DnsRecordProxy o, java.lang.String value) {
            o.setRecordType(value);
        }
    };

    long getVersion();
    void setVersion(long value);
    
    public static final Accessor<DnsRecordProxy, java.lang.Long> Version = new Accessor<DnsRecordProxy, java.lang.Long>() {
        @Override
        public java.lang.Long get(DnsRecordProxy o) {
            return o.getVersion();
        }

        @Override
        public void set(DnsRecordProxy o, java.lang.Long value) {
            o.setVersion(value);
        }
    };


    @Service(value = DnsRecordGwtService.class, locator = InjectingServiceLocator.class)
    @SkipInterfaceValidation
    public static interface DnsRecordRequest extends RequestContext, BaseEntityRequest<DnsRecordProxy> {
        Request<DnsRecordProxy> persist(DnsRecordProxy proxy);
    }

    public static interface EditorDriver extends RequestFactoryEditorDriver<DnsRecordProxy, DnsRecordEditor> {
    }

	public static interface DnsRecordRequestFactory {
    	DnsRecordProxy.DnsRecordRequest getDnsRecordRequest();
	}

    public static final DomainModel<DnsRecordProxy, DnsRecordRequest> Model = new DomainModel<DnsRecordProxy, DnsRecordRequest>(DnsRecordProxy.class) {
        @Override
        public Editor<DnsRecordProxy> buildEditor() {
            return new DnsRecordEditor();
        }

        @Override
        public DnsRecordRequest context(RequestFactory requestFactory) {
            return ((DnsRecordRequestFactory) requestFactory).getDnsRecordRequest();
        }

        @Override
        public void persist(DnsRecordRequest context, DnsRecordProxy item) {
            context.persist(item);
        }

        @Override
        public RequestFactoryEditorDriver<DnsRecordProxy, Editor<DnsRecordProxy>> buildEditorDriver() {
            return GWT.create(EditorDriver.class);
        }
    };
}
