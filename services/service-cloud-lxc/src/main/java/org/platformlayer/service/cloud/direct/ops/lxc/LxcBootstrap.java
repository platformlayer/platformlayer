package org.platformlayer.service.cloud.direct.ops.lxc;

import java.io.File;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.core.model.AddressModel;
import org.platformlayer.ops.ChrootOpsTarget;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.TemplateHelpers;
import org.platformlayer.ops.networks.InterfaceModel;
import org.platformlayer.ops.ssh.SshAuthorizedKey;
import org.platformlayer.service.cloud.direct.model.DirectHost;

import com.fathomdb.Utf8;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LxcBootstrap {
	public String lxcId;
	public String hostname;
	public File instanceDir;
	public Provider<AddressModel> address4;
	public Provider<AddressModel> address6;

	@Inject
	TemplateHelpers template;

	// public boolean startOnBoot = true;
	public PublicKey sshPublicKey;

	Map<String, Object> buildModel() {
		Map<String, Object> model = Maps.newHashMap();
		model.put("hostname", getHostname());

		model.put("name", lxcId);

		InterfaceModel eth0 = InterfaceModel.build("eth0");
		AddressModel ipv4 = address4.get();
		eth0.addAddress(ipv4);

		AddressModel ipv6 = address6.get();
		eth0.addAddress(ipv6);

		List<InterfaceModel> interfaces = Lists.newArrayList();
		interfaces.add(eth0);

		model.put("interfaces", interfaces);

		model.put("externalBridge", OpsContext.get().getInstance(DirectHost.class).bridge);

		model.put("ipv4", ipv4);
		model.put("ipv6", ipv6);

		return model;
	}

	OpsTarget getTarget() {
		return OpsContext.get().getInstance(OpsTarget.class);
	}

	byte[] runTemplate(String templateName) throws OpsException {
		Map<String, Object> model = buildModel();
		String resourcePath = template.toResourcePath(this, templateName);
		String templateString = template.runTemplate(resourcePath, model);
		return Utf8.getBytes(templateString);
	}

	File getInstanceDir() {
		return instanceDir;
	}

	public File getRoot() {
		return new File(getInstanceDir(), "rootfs");
	}

	File getConfigFile() {
		return new File(getInstanceDir(), "config");
	}

	void setupInterfaces() throws OpsException {
		File file = new File(getRoot(), "etc/network/interfaces");
		FileUpload.upload(getTarget(), file, runTemplate("etc.network.interfaces"));
	}

	void setupIpv6Script() throws OpsException {
		File file = new File(getRoot(), "etc/network/if-up.d/ipv6");
		OpsTarget target = getTarget();
		FileUpload.upload(target, file, runTemplate("ipv6"));
		target.chmod(file, "755");
	}

	void setupLxcConfig() throws OpsException {
		File file = getConfigFile();
		FileUpload.upload(getTarget(), file, runTemplate("lxc.config"));
	}

	void setupHostname() throws OpsException {
		String hostname = getHostname();

		File file = new File(getRoot(), "etc/hostname");
		FileUpload.upload(getTarget(), file, hostname);
	}

	private String getHostname() {
		if (hostname != null) {
			return hostname;
		}
		return lxcId;
	}

	void setupInittab() throws OpsException {
		File file = new File(getRoot(), "etc/inittab");
		if (getTarget().readTextFile(file) == null) {
			// Only for the initial build
			FileUpload.upload(getTarget(), file, runTemplate("etc.inittab"));
		}
	}

	private void setupResolveConf() throws OpsException {
		File file = new File(getRoot(), "etc/resolv.conf");
		if (getTarget().readTextFile(file) == null) {
			// Only for the initial build
			FileUpload.upload(getTarget(), file, runTemplate("etc.resolv.conf"));
		}
	}

	// private void setupAutostart() throws OpsException {
	// // ln -s /var/lib/lxc/${NAME}/config /etc/lxc/auto/${NAME}
	//
	// target.symlink(getConfigFile(), new File("/etc/lxc/auto/" + lxcId), false);
	// }

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (OpsContext.isConfigure()) {
			// TODO: Move to children
			//
			// File rootDir = getRoot();
			// target.mkdir(rootDir);

			// target.executeCommand(Command.build("cd {0}; tar jxf {1}", rootDir,
			// imagePath).setTimeout(TimeSpan.FIVE_MINUTES));

			// mknod -m 666 ${ROOT}/dev/tty1 c 4 1
			// mknod -m 666 ${ROOT}/dev/tty2 c 4 2
			// mknod -m 666 ${ROOT}/dev/tty3 c 4 3
			// mknod -m 666 ${ROOT}/dev/tty4 c 4 4
			// mknod -m 666 ${ROOT}/dev/tty5 c 4 5
			// mknod -m 666 ${ROOT}/dev/tty6 c 4 6

			{
				ChrootOpsTarget chrootTarget = new ChrootOpsTarget(getRoot(), new File("/tmp"), target);
				if (sshPublicKey != null) {
					SshAuthorizedKey.ensureSshAuthorization(chrootTarget, "root", sshPublicKey);
				}
			}

			setupLxcConfig();

			setupResolveConf();
			setupInittab();
			setupInterfaces();
			setupIpv6Script();
			setupHostname();

			// if (startOnBoot) {
			// setupAutostart();
			// }
		}

	}
}
