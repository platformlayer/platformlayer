package org.platformlayer.ops.multitenant;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstack.crypto.CertificateAndKey;
import org.platformlayer.ApplicationMode;
import org.platformlayer.auth.AuthenticationService;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.PlatformlayerAuthenticationException;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.model.AuthenticationToken;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.MultitenantConfiguration;
import org.platformlayer.ops.OpsConfiguration;
import org.platformlayer.ops.OpsException;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class SimpleMultitenantConfiguration implements MultitenantConfiguration {
	private static final Logger log = Logger.getLogger(SimpleMultitenantConfiguration.class);

	final ProjectAuthorization masterProject;
	final List<PlatformLayerKey> mappedItems;

	public SimpleMultitenantConfiguration(ProjectAuthorization masterProject, List<PlatformLayerKey> mappedItems) {
		super();
		this.masterProject = masterProject;
		this.mappedItems = mappedItems;
	}

	@Override
	public ProjectAuthorization getMasterProject() {
		return masterProject;
	}

	@Override
	public Iterable<PlatformLayerKey> getMappedItems() {
		return Collections.unmodifiableList(mappedItems);
	}

	public static MultitenantConfiguration build(OpsConfiguration configuration, EncryptionStore encryptionStore,
			AuthenticationService authenticationService, AuthenticationTokenValidator authenticationTokenValidator)
			throws OpsException {
		String projectKey = configuration.lookup("multitenant.project", null);
		String username = configuration.lookup("multitenant.user", null);
		String password = configuration.lookup("multitenant.password", null);
		String certAlias = configuration.lookup("multitenant.cert", null);

		CertificateAndKey certificateAndKey = null;
		if (certAlias != null) {
			certificateAndKey = encryptionStore.getCertificateAndKey(certAlias);
		}

		String message = "Invalid multitenant configuration";

		if (username == null || projectKey == null) {
			throw new OpsException(message);
		}

		AuthenticationToken authn = null;

		if (certificateAndKey != null) {
			try {
				authn = authenticationService.authenticateWithCertificate(username, certificateAndKey.getPrivateKey(),
						certificateAndKey.getCertificateChain());
			} catch (PlatformlayerAuthenticationException e) {
				throw new OpsException(message, e);
			}
		} else if (password != null) {
			log.warn("Using password authentication with multitenant");

			if (!ApplicationMode.isDevelopment()) {
				throw new IllegalStateException();
			}

			try {
				authn = authenticationService.authenticateWithPassword(username, password);
			} catch (PlatformlayerAuthenticationException e) {
				throw new OpsException(message, e);
			}
		}

		if (authn == null) {
			throw new OpsException(message);
		}

		ProjectAuthorization authz = authenticationTokenValidator.validate(authn, projectKey);
		if (authz == null) {
			throw new OpsException(message);
		}

		// {
		// try {
		// project = userRepository.findProject(user, projectKey);
		// } catch (RepositoryException e) {
		// throw new OpsException(message, e);
		// }
		//
		// if (project == null) {
		// throw new OpsException(message);
		// }
		// }

		List<PlatformLayerKey> mappedItems = Lists.newArrayList();

		for (String key : Splitter.on(",").split(configuration.lookup("multitenant.keys", ""))) {
			String[] tokens = key.split("/");
			if (tokens.length != 2) {
				throw new IllegalStateException();
			}
			String serviceType = tokens[0];
			String itemType = tokens[1];
			mappedItems.add(PlatformLayerKey.fromServiceAndItem(serviceType, itemType));
		}

		if (mappedItems.isEmpty()) {
			throw new OpsException(message);
		}

		MultitenantConfiguration config = new SimpleMultitenantConfiguration(authz, mappedItems);

		return config;
	}

}
