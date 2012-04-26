package org.platformlayer.service.openldap.ops.ldap;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.SetUtils;
import org.platformlayer.SetUtils.SetCompareResults;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.ldap.LdapDN;
import org.platformlayer.service.openldap.model.LdapService;
import org.platformlayer.service.openldap.ops.LdapHelpers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public abstract class LdapEntry {
	static final Logger log = Logger.getLogger(LdapEntry.class);

	private static final Collection<String> IGNORE_PROPERTIES_IN_VALIDATE = Lists.newArrayList("userPassword");

	LdapDN ldapDN;

	String description;

	boolean onlyConfigureOnForce;

	@Inject
	LdapHelpers ldap;

	public LdifRecord buildLdif() {
		List<String> objectClasses = Lists.newArrayList();
		getObjectClasses(objectClasses);

		Multimap<String, String> additionalProperties = HashMultimap.create();
		getAdditionalProperties(additionalProperties);

		LdifRecord ldifRecord = new LdifRecord(getLdapDN(), objectClasses, additionalProperties);
		return ldifRecord;
	}

	protected void getObjectClasses(List<String> objectClasses) {
	}

	@Handler({ OperationType.Validate })
	public void doValidate(OperationType operationType, OpsTarget target) throws OpsException {
		LdifRecord current = queryCurrentRecord(target);
		if (current == null) {
			OpsContext.get().addWarning(this, "LdapNodeNotFound", "Ldap node not found: " + getLdapDN());
		} else {
			validateRecord(current);
		}
	}

	@Handler({ OperationType.Configure })
	public void doConfigure(OpsTarget target) throws OpsException {
		boolean shouldConfigure = OpsContext.isConfigure(); // operation.isForce() || !isOnlyConfigureOnForce();
		if (shouldConfigure) {
			String ldapServerPassword = ldap.readLdapServerPassword(target);

			if (!alreadyExists()) {
				LdifRecord ldif = buildLdif();
				OpenLdapManager.doLdapModify(target, OpenLdapServer.ADMIN_DN, ldapServerPassword, true, ldif);
			}

			// } else {
			// log.info("Won't configure; OnlyConfigureOnForce is set: " + getPath());
		}
	}

	protected boolean alreadyExists() throws OpsException {
		return false;
	}

	protected void validateRecord(LdifRecord current) {
		LdifRecord desired = buildLdif();
		SetCompareResults<String> objectClassesCompare = SetUtils.setCompare(current.getObjectClasses(),
				desired.getObjectClasses());
		if (!objectClassesCompare.isMatch()) {
			log.info("ObjectClasses mismatch: " + objectClassesCompare);
			OpsContext.get().addWarning(this, "LdapObjectClassesDoNotMatch",
					"Ldap object classes do not match: " + getLdapDN());
		}

		SetCompareResults<Entry<String, String>> propertiesCompare = SetUtils.setCompare(current.getProperties()
				.entries(), desired.getProperties().entries());
		if (!isMatch(propertiesCompare, IGNORE_PROPERTIES_IN_VALIDATE)) {
			log.info("Properties mismatch: " + propertiesCompare);
			OpsContext.get().addWarning(this, "LdapPropertiesDoNotMatch",
					"Ldap properties do not match: " + getLdapDN());
		}
	}

	private boolean isMatch(SetCompareResults<Entry<String, String>> compareResult, Collection<String> ignoreProperties) {
		// TODO: How do we cope with numberindexed attributes ??
		// olcAccess={1}to * attrs=userPassword,shadowLastChange by dn="cn=admin,cn=config" write by
		// dn="cn=Manager,dc=fathomdb,dc=com" write by anonymous auth by self write by * none
		// olcAccess={x}to * attrs=userPassword,shadowLastChange by dn="cn=admin,cn=config" write by
		// dn="cn=Manager,dc=fathomdb,dc=com" write by anonymous auth by self write by * none
		// olcAccess={0}to * by dn="cn=admin,cn=config" write by dn="cn=Manager,dc=fathomdb,dc=com" write by
		// dn.subtree="ou=People,dc=fathomdb,dc=com" read by anonymous auth by * none
		// olcAccess={x}to * by dn="cn=admin,cn=config" write by dn="cn=Manager,dc=fathomdb,dc=com" write by
		// dn.subtree="ou=People,dc=fathomdb,dc=com" read by anonymous auth by * none
		// olcDatabase={1}hdb
		// olcDatabase=hdb

		for (Entry<String, String> leftNotRight : compareResult.leftNotRight) {
			String key = leftNotRight.getKey();
			if (!ignoreProperties.contains(key)) {
				return false;
			}
		}

		for (Entry<String, String> rightNotLeft : compareResult.rightNotLeft) {
			String key = rightNotLeft.getKey();
			if (!ignoreProperties.contains(key)) {
				return false;
			}
		}
		return true;
	}

	protected LdifRecord queryCurrentRecord(OpsTarget target) throws OpsException {
		LdapService ldapService = OpsContext.get().getInstance(LdapService.class);
		String ldapPassword = ldapService.ldapServerPassword.plaintext();

		String filter = null;
		LdapDN searchBaseDN = getLdapDN();
		return OpenLdapManager.doLdapQuerySingle(target, OpenLdapServer.ADMIN_DN, ldapPassword, searchBaseDN, filter);
	}

	public LdapDN getLdapDN() {
		return ldapDN;
	}

	public void setLdapDN(LdapDN ldapDN) {
		this.ldapDN = ldapDN;
	}

	protected void getAdditionalProperties(Multimap<String, String> additionalProperties) {
		if (getDescription() != null) {
			additionalProperties.put("description", getDescription());
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isOnlyConfigureOnForce() {
		return onlyConfigureOnForce;
	}

	public void setOnlyConfigureOnForce(boolean onlyConfigureOnForce) {
		this.onlyConfigureOnForce = onlyConfigureOnForce;
	}

}
