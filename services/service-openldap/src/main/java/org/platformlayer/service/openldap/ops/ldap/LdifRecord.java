package org.platformlayer.service.openldap.ops.ldap;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;


import org.apache.log4j.Logger;
import org.platformlayer.ops.ldap.LdapDN;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class LdifRecord {
    static final Logger log = Logger.getLogger(LdifRecord.class);

    final LdapDN ldapDn;
    final List<String> objectClasses;
    final Multimap<String, String> properties;

    public LdifRecord(LdapDN dn, List<String> objectClasses, Multimap<String, String> properties) {
        super();
        this.ldapDn = dn;
        this.objectClasses = objectClasses;
        this.properties = properties;
    }

    public LdifRecord(LdapDN dn) {
        this(dn, Lists.<String> newArrayList(), HashMultimap.<String, String> create());
    }

    @Override
    public String toString() {
        return toLdifText();
    }

    public String toLdifText() {
        StringBuilder sb = new StringBuilder();
        sb.append("dn: " + ldapDn.toLdifEncoded() + "\n");
        for (String objectClass : objectClasses) {
            sb.append("objectClass: " + objectClass + "\n");
        }
        for (Entry<String, String> entry : properties.entries()) {
            sb.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
        return sb.toString();
    }

    public LdapDN getLdapDn() {
        return ldapDn;
    }

    public List<String> getObjectClasses() {
        return objectClasses;
    }

    public Multimap<String, String> getProperties() {
        return properties;
    }

    public static List<LdifRecord> parse(String ldif) {
        // ldapsearch wraps at 78 chars, we unwrap this using the replacement below
        ldif = ldif.replace("\n ", "");

        List<LdifRecord> records = Lists.newArrayList();
        LdifRecord current = null;

        int lineNumber = 0;
        for (String line : ldif.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.length() == 0)
                continue;
            if (trimmed.startsWith("#"))
                continue;

            lineNumber++;

            int delimIndex = trimmed.indexOf(':');

            if (delimIndex == -1) {
                log.warn("Line did not contain delimiter: " + line);
            }
            String key = trimmed.substring(0, delimIndex);
            String value = trimmed.substring(delimIndex + 1);
            key = key.trim();
            value = value.trim();

            if (key.equals("version")) {
                if (lineNumber != 1)
                    throw new IllegalArgumentException("Unexpected version attribute in " + ldif);

                // Ignore
                continue;
            }

            if (key.equals("dn")) {
                LdapDN ldapDN = LdapDN.parseLdifEncoded(value);
                current = new LdifRecord(ldapDN);
                records.add(current);
                continue;
            }

            if (key.equals("objectClass")) {
                current.getObjectClasses().add(value);
                continue;
            }

            current.getProperties().put(key, value);
        }

        return records;
    }

    public String getPropertyUnique(String key) {
        Collection<String> values = properties.get(key);
        if (values.size() == 0)
            return null;
        if (values.size() != 1)
            throw new IllegalStateException("Expected unique value for property: " + key);
        return Iterables.getOnlyElement(values);
    }

    public static LdifRecord whereUnique(List<LdifRecord> records, Predicate<LdifRecord> predicate) {
        Iterable<LdifRecord> filtered = Iterables.filter(records, predicate);
        if (Iterables.isEmpty(filtered))
            return null;
        return Iterables.getOnlyElement(filtered);
    }

    public static Iterable<LdifRecord> filter(List<LdifRecord> records, Predicate<LdifRecord> predicate) {
        return Iterables.filter(records, predicate);
    }

}