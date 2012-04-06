package org.platformlayer.service.openldap.ops.ldap;

import java.util.List;

import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.ldap.LdapDN;

import com.google.common.collect.Multimap;

public class OrganizationLdapEntry extends LdapEntry {
    String organizationName;
    String cn;
    boolean isTop;

    @Override
    protected void getObjectClasses(List<String> objectClasses) {
        super.getObjectClasses(objectClasses);

        objectClasses.add("dcObject");
        objectClasses.add("organization");
        if (isTop())
            objectClasses.add("top");
    }

    @Override
    protected void getAdditionalProperties(Multimap<String, String> additionalProperties) {
        super.getAdditionalProperties(additionalProperties);

        additionalProperties.put("o", organizationName);
        additionalProperties.put("dc", getCn());
    }

    /**
     * Note that dn is the actual DN used; we don't prefix with o=XXX
     */
    public static OrganizationLdapEntry build(String o, String cn, LdapDN organizationDN) {
        OrganizationLdapEntry entry = OpsContext.get().getInjector().getInstance(OrganizationLdapEntry.class);
        entry.setOrganizationName(o);
        entry.setCn(cn);
        entry.setLdapDN(organizationDN);
        return entry;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean isTop) {
        this.isTop = isTop;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

}
