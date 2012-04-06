package org.platformlayer.service.openldap.ops.ldap;

import java.util.List;

import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.ldap.LdapAttributes;
import org.platformlayer.ops.ldap.LdapDN;

import com.google.common.collect.Multimap;

public class OrganizationalUnitLdapEntry extends LdapEntry {
    String ou;

    @Override
    protected void getObjectClasses(List<String> objectClasses) {
        super.getObjectClasses(objectClasses);

        objectClasses.add("organizationalUnit");
    }

    public static OrganizationalUnitLdapEntry build(String ou, LdapDN parentLdapDN) {
        OrganizationalUnitLdapEntry entry = OpsContext.get().getInjector().getInstance(OrganizationalUnitLdapEntry.class);
        entry.setOu(ou);
        entry.setLdapDN(parentLdapDN.childDN(LdapAttributes.LDAP_ATTRIBUTE_OU, ou));
        return entry;
    }

    @Override
    protected void getAdditionalProperties(Multimap<String, String> additionalProperties) {
        super.getAdditionalProperties(additionalProperties);

        additionalProperties.put("ou", getOu());
    }

    public String getOu() {
        return ou;
    }

    public void setOu(String ou) {
        this.ou = ou;
    }

}
