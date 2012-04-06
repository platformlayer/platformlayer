package org.platformlayer.service.openldap.server;

import org.platformlayer.service.openldap.model.LdapService;
import org.platformlayer.ui.shared.server.GwtServiceBase;

public class LdapServiceGwtService extends GwtServiceBase<LdapService> {
    public LdapServiceGwtService() {
        super(LdapService.class);
    }
}