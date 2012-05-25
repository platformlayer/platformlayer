package org.platformlayer.ops.ssh;

import java.io.File;
import java.security.PublicKey;

import org.platformlayer.ops.Deviations;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.SshKeys;

public class SshAuthorizedKey {
	public String user;
	public PublicKey publicKey;

	public static SshAuthorizedKey build(String user, PublicKey publicKey) {
		SshAuthorizedKey item = Injection.getInstance(SshAuthorizedKey.class);
		item.user = user;
		item.publicKey = publicKey;
		return item;
	}

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		ensureSshAuthorization(target, user, publicKey);
	}

	public static void ensureSshAuthorization(OpsTarget target, String user, PublicKey sshPublicKey)
			throws OpsException {
		File homeDir;
		if (user.equals("root")) {
			homeDir = new File("/root");
		} else {
			homeDir = new File("/home/" + user);
		}

		File sshDir = new File(homeDir, ".ssh");
		if (target.getFilesystemInfoFile(sshDir) == null) {
			target.mkdir(sshDir, "500");
			target.chown(sshDir, user, null, false, false);
		}

		File sshAuthorizationsFile = new File(sshDir, "authorized_keys");
		String sshAuthorizations = target.readTextFile(sshAuthorizationsFile);

		String serialized = SshKeys.serialize(sshPublicKey);
		boolean keyPresent = sshAuthorizations != null && sshAuthorizations.contains(serialized);

		if (OpsContext.isValidate()) {
			Deviations.assertTrue(keyPresent, "SSH key not present");
		}

		if (OpsContext.isConfigure()) {
			if (!keyPresent) {
				if (sshAuthorizations == null) {
					sshAuthorizations = "";
				} else {
					sshAuthorizations += "\n";
				}
				sshAuthorizations += serialized;

				FileUpload upload = FileUpload.build(sshAuthorizations);
				upload.mode = "644";
				upload.path = sshAuthorizationsFile;

				FileUpload.upload(target, sshAuthorizationsFile, sshAuthorizations);
			}
		}
	}

}
