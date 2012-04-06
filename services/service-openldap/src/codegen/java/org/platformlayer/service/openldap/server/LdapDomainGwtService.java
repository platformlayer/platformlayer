package org.platformlayer.service.openldap.server;

import org.platformlayer.service.openldap.model.LdapDomain;
import org.platformlayer.ui.shared.server.GwtServiceBase;

public class LdapDomainGwtService extends GwtServiceBase<LdapDomain> {
    public LdapDomainGwtService() {
        super(LdapDomain.class);
    }
}