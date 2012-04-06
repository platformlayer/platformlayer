package org.platformlayer.service.openldap;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.ops.crypto.Passwords;
import org.platformlayer.service.openldap.model.LdapDomain;
import org.platformlayer.service.openldap.model.LdapService;
import org.platformlayer.xaas.Service;

@Service("openldap")
public class OpenLdapProvider extends ServiceProviderBase {
    @Override
    public void beforeCreateItem(ItemBase item) throws OpsException {
        super.beforeCreateItem(item);

        // TODO: This doesn't feel like the right place for this
        if (item instanceof LdapService) {
            LdapService ldapService = (LdapService) item;
            Passwords passwords = new Passwords();

            if (Secret.isNullOrEmpty(ldapService.ldapServerPassword)) {
                ldapService.ldapServerPassword = passwords.generateRandomPassword(12);
            }
        }

        if (item instanceof LdapDomain) {
            LdapDomain ldapService = (LdapDomain) item;
            Passwords passwords = new Passwords();

            if (Secret.isNullOrEmpty(ldapService.adminPassword)) {
                ldapService.adminPassword = passwords.generateRandomPassword(12);
            }
        }
    }

}
