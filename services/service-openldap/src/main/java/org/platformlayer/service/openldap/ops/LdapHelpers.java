package org.platformlayer.service.openldap.ops;

import java.io.File;

import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class LdapHelpers {

	static final File SECRET_FILE = new File("/root/ldap.secret");

	public String readLdapServerPassword(OpsTarget target) throws OpsException {
		String password = target.readTextFile(SECRET_FILE);
		password = password.trim();
		return password;
	}

	public void writeLdapServerPassword(OpsTarget target, Secret password) throws OpsException {
		target.setFileContents(SECRET_FILE, password.plaintext());
	}
}
