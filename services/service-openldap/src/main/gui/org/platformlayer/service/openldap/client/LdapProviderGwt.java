package org.platformlayer.service.openldap.client;

import org.platformlayer.service.openldap.shared.LdapDomainProxy;
import org.platformlayer.service.openldap.shared.LdapServiceProxy;

public interface LdapProviderGwt {
    public static interface ProviderRequestFactory extends LdapDomainProxy.LdapDomainRequestFactory, LdapServiceProxy.LdapServiceRequestFactory {

    }
}
