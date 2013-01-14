package org.platformlayer.service.jenkins.ops;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.jenkins.model.JenkinsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class EnsureJenkinsSshKey extends OpsTreeBase {
	private static final Logger log = LoggerFactory.getLogger(EnsureJenkinsSshKey.class);

	@Bound
	JenkinsService jenkins;

	@Inject
	PlatformLayerHelpers platformlayer;

	@Handler
	public void doHandle(OpsTarget target) throws OpsException {
		String sshKey = Tag.SSH_KEY.findFirst(jenkins);

		if (Strings.isNullOrEmpty(sshKey)) {
			if (OpsContext.isConfigure()) {
				File sshPublicKeyPath = new File("/var/lib/jenkins/.ssh/id_rsa.pub");

				sshKey = target.readTextFile(sshPublicKeyPath);
				if (sshKey == null) {
					// su -c "ssh-keygen -q -f /var/lib/jenkins/.ssh/id_rsa -N ''" jenkins

					Command keygenCommand = Command.build("su");
					keygenCommand.addLiteral("-c").addQuoted("ssh-keygen -q -f /var/lib/jenkins/.ssh/id_rsa -N ''");
					keygenCommand.addLiteral("jenkins");

					target.executeCommand(keygenCommand);

					sshKey = target.readTextFile(sshPublicKeyPath);
				}

				if (Strings.isNullOrEmpty(sshKey)) {
					throw new OpsException("Unable to generate SSH key");
				}

				Tag tag = Tag.SSH_KEY.build(sshKey);
				platformlayer.addTag(jenkins.getKey(), tag);
			}

			if (OpsContext.isValidate()) {
				log.error("SSH Key not configured for Jenkins");
			}
		}

		if (sshKey != null) {
			log.debug("SSH Key is " + sshKey);
		}
	}

	@Override
	protected void addChildren() throws OpsException {
	}

}
