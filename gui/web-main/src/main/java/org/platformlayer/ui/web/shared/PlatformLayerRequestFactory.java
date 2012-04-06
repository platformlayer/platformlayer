package org.platformlayer.ui.web.shared;

import java.util.List;

import org.platformlayer.service.aptcache.client.AptCacheProviderGwt;
import org.platformlayer.service.dns.client.DnsProviderGwt;
import org.platformlayer.service.dnsresolver.client.DnsResolverProviderGwt;
import org.platformlayer.service.openldap.client.LdapProviderGwt;
import org.platformlayer.ui.web.server.ServiceInfoService;
import org.platformlayer.ui.web.server.ServiceInfoServiceLocator;

import com.google.web.bindery.requestfactory.shared.LoggingRequest;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.google.web.bindery.requestfactory.shared.Service;

public interface PlatformLayerRequestFactory extends LdapProviderGwt.ProviderRequestFactory, RequestFactory, DnsResolverProviderGwt.ProviderRequestFactory, DnsProviderGwt.ProviderRequestFactory,
        AptCacheProviderGwt.ProviderRequestFactory {
    @Service(value = ServiceInfoService.class, locator = ServiceInfoServiceLocator.class)
    public interface ServiceInfoRequest extends RequestContext {
        Request<List<ServiceInfoProxy>> findAll();
    }

    LoggingRequest loggingRequest();

    // AptCacheServiceRequest aptCacheRequest();

    ServiceInfoRequest serviceInfoRequest();

    // DnsZoneRequest dnsZoneRequest();
    //
    // DnsRecordRequest dnsRecordRequest();
    //
    // DnsServerRequest dnsServerRequest();

}
