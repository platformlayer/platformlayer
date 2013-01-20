package org.openstack.service.imagestore.ops;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;

import javax.inject.Inject;

import org.openstack.service.imagestore.model.ImageStore;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpaqueMachine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.images.ImageStoreProvider;
import org.platformlayer.ops.images.direct.DirectImageStore;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.service.ManagedService;
import org.platformlayer.ops.ssh.SshAuthorizedKey;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class ImageStoreController extends OpsTreeBase implements ImageStoreProvider {

	private static final Logger log = LoggerFactory.getLogger(ImageStoreController.class);

	@Inject
	ServiceContext service;

	@Bound
	ImageStore model;

	@Inject
	SshKeys sshKeys;

	@Handler
	public void handler() throws OpsException, IOException {
	}

	private boolean isFlavorGlance(ImageStore model) {
		return false;
	}

	@Override
	protected void addChildren() throws OpsException {
		Tag tag;

		boolean useGlance = isFlavorGlance(model);

		String host = model.dnsName;
		if (host.contains(":")) {
			// IPV6
			host = "[" + host + "]";
		}

		if (useGlance) {
			if (Strings.isNullOrEmpty(model.dnsName)) {
				throw new IllegalArgumentException("dnsName must be specified");
			}

			addChildrenGlance(model);
			tag = Tag.build("endpoint", "glance://" + host);
		} else {
			addDirectStore(model);
			tag = Tag.build("endpoint", "ssh://imagestore@" + host);
		}

		addChild(ItemTagger.build(tag));
	}

	private void addDirectStore(ImageStore model) throws OpsException {
		// Serious bootstrapping problem here!!!
		SshKey serviceKey = service.getSshKey();
		PublicKey sshPublicKey = serviceKey.getKeyPair().getPublic();

		OpaqueMachine machine = new OpaqueMachine(NetworkPoint.forPublicHostname(model.dnsName));
		OpsTarget target = machine.getTarget("imagestore", serviceKey.getKeyPair());

		SshAuthorizedKey.ensureSshAuthorization(target, "imagestore", sshPublicKey);

		// addChild(SshAuthorizedKey.build("root", publicKey));

		// TODO: Re-introduce (but we don't have the machine)
		// addChild(PackageDependency.build("socat"));
	}

	protected void addChildrenGlance(ImageStore model) throws OpsException {
		InstanceBuilder instance = InstanceBuilder.build(model.dnsName, this);
		addChild(instance);

		// We’ll stick with glance using SQLite (for now)
		instance.addChild(PackageDependency.build("glance"));
		instance.addChild(ManagedService.build("glance"));

		instance.addChild(MetricsInstance.class);
	}

	@Override
	public org.platformlayer.ops.images.ImageStore getImageStore() throws OpsException {
		String endpoint = model.getTags().findUnique("endpoint");
		if (endpoint == null) {
			log.warn("ImageStore not yet active: " + model);
			return null;
		}

		URI url;
		try {
			url = new URI(endpoint);
		} catch (URISyntaxException e) {
			throw new OpsException("Cannot parse endpoint: " + endpoint, e);
		}
		// if (url.getScheme().equals("glance")) {
		// int port = url.getPort();
		// if (port == -1)
		// port = 9292;
		// String glanceUrl = "http://" + url.getHost() + ":" + port + "/v1";
		// GlanceImageStore glanceImageStore = new GlanceImageStore(glanceUrl);
		// return glanceImageStore;
		// } else

		if (url.getScheme().equals("ssh")) {
			String myAddress = url.getHost();
			Machine machine = new OpaqueMachine(NetworkPoint.forPublicHostname(myAddress));
			// This is nasty; we're in the context of another service here...
			SshKey sshKey = sshKeys.findOtherServiceKey(new ServiceType("imagestore"));
			OpsTarget target = machine.getTarget("imagestore", sshKey.getKeyPair());

			DirectImageStore directImageStore = OpsContext.get().getInjector().getInstance(DirectImageStore.class);
			directImageStore.connect(target);
			return directImageStore;
		} else {
			throw new OpsException("Unknown protocol for endpoint: " + endpoint);
		}
	}
}
