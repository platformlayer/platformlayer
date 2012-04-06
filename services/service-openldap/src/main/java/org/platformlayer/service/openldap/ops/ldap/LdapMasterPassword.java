package org.platformlayer.service.openldap.ops.ldap;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.ldap.LdapPasswords;
import org.platformlayer.service.openldap.ops.LdapHelpers;

public class LdapMasterPassword {
    Secret ldapSecret;

    @Inject
    LdapHelpers ldap;

    public static LdapMasterPassword build(Secret ldapSecret) {
        LdapMasterPassword ldapMasterPassword = Injection.getInstance(LdapMasterPassword.class);
        ldapMasterPassword.ldapSecret = ldapSecret;
        return ldapMasterPassword;
    }

    @Handler
    public void doOperation(OpsTarget target) throws OpsException {
        // TODO: Make this idempotent
        LdifRecord configLdif;
        {
            Command ldapSearch = Command.build("ldapsearch -Q -LLL -Y EXTERNAL -H ldapi:/// -b cn=config olcRootDN=cn=admin,cn=config dn olcRootDN olcRootPW");

            List<LdifRecord> ldifs = LdifRecord.parse(target.executeCommand(ldapSearch).getStdOut());
            if (ldifs.size() != 1) {
                throw new OpsException("Expected exactly one LDIF record for config");
            }
            configLdif = ldifs.get(0);
        }

        {
            StringBuilder modify = new StringBuilder();

            modify.append("dn: " + configLdif.getLdapDn().toString() + "\n");
            modify.append("replace: olcRootPW\n");
            modify.append("olcRootPW: " + LdapPasswords.getLdapPasswordEncoded(ldapSecret.plaintext()) + "\n");
            modify.append("\n");

            File tempDir = target.createTempDir();
            File modifyFile = new File(tempDir, "modifypw.ldif");
            target.setFileContents(modifyFile, modify.toString());

            Command ldapModify = Command.build("ldapmodify -Q -Y EXTERNAL -H ldapi:/// -f {0}", modifyFile);

            target.executeCommand(ldapModify);
        }

        ldap.writeLdapServerPassword(target, ldapSecret);
    }
}
