package org.platformlayer.service.nexus.ops;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.ExceptionUtils;
import org.platformlayer.ResourceUtils;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.TemplateHelpers;
import org.platformlayer.service.nexus.utils.NexusLdapPasswords;

import com.google.common.collect.Maps;

public class NexusBootstrap {
	@Inject
	TemplateHelpers templates;

	@Handler
	public void handler() throws OpsException, IOException {
		// TODO: This needs to be idempotent
		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

		// Nexus needs a workdir; by default it's in the home directory of the user we're running under
		// With jetty, the jetty user can't create this directory; we do it
		File sonatypeDir = new File("/usr/share/jetty/sonatype-work");
		target.mkdir(sonatypeDir, "750");

		File nexusDir = new File(sonatypeDir, "nexus");
		target.mkdir(nexusDir, "750");

		File confDir = new File(nexusDir, "conf");
		target.mkdir(confDir, "750");
		{
			String contents = ResourceUtils.get(getClass(), "conf/security.xml");
			FileUpload.upload(target, new File(confDir, "security.xml"), contents);
		}

		{
			String contents = ResourceUtils.get(getClass(), "conf/security-configuration.xml");
			FileUpload.upload(target, new File(confDir, "security-configuration.xml"), contents);
		}

		{
			// TODO: Bind with a low-privilege account
			// TODO: Don't hard-code this stuff
			String ldapHost = "192.168.192.67";
			String ldapDomain = "dc=com,dc=fathomscale";
			String ldapUsername = "cn=Manager," + ldapDomain;
			String ldapPassword = "adminsecret";

			Map<String, Object> vars = Maps.newHashMap();
			vars.put("searchBase", ldapDomain);
			vars.put("systemUsername", ldapUsername);
			vars.put("systemPassword", encryptNexusPassword(ldapPassword));
			vars.put("ldapHost", ldapHost);

			// TODO: This is a bit limiting; we should use memberOf
			// Avoids escaping ${username}
			vars.put("groupMemberFormat", "uid=${username},ou=Users,dc=com,dc=fathomscale");

			String resourcePath = templates.toResourcePath(this, "conf/ldap.xml");
			String contents = templates.runTemplate(resourcePath, vars);
			FileUpload.upload(target, new File(confDir, "ldap.xml"), contents);
		}

		target.chown(sonatypeDir, "jetty", "jetty", true, false);
	}

	private String encryptNexusPassword(String ldapPassword) throws OpsException {
		NexusLdapPasswords nexusLdapPasswords = new NexusLdapPasswords();
		nexusLdapPasswords.addEscapeCharacters = false;
		try {
			return nexusLdapPasswords.encrypt(ldapPassword);
		} catch (Exception e) {
			ExceptionUtils.handleInterrupted(e);
			throw new OpsException("Error encrypting password", e);
		}
	}

	public static NexusBootstrap build() {
		return Injection.getInstance(NexusBootstrap.class);
	}
}