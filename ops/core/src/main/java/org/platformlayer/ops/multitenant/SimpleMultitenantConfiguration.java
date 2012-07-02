package org.platformlayer.ops.multitenant;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.MultitenantConfiguration;
import org.platformlayer.ops.OpsConfiguration;
import org.platformlayer.ops.OpsException;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class SimpleMultitenantConfiguration implements MultitenantConfiguration {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SimpleMultitenantConfiguration.class);

	final OpsProject masterProject;
	final List<PlatformLayerKey> mappedItems;

	public SimpleMultitenantConfiguration(OpsProject masterProject, List<PlatformLayerKey> mappedItems) {
		super();
		this.masterProject = masterProject;
		this.mappedItems = mappedItems;
	}

	@Override
	public OpsProject getMasterProject() {
		return masterProject;
	}

	@Override
	public Iterable<PlatformLayerKey> getMappedItems() {
		return Collections.unmodifiableList(mappedItems);
	}

	public static MultitenantConfiguration build(OpsConfiguration configuration, UserRepository userRepository)
			throws OpsException {
		String projectKey = configuration.lookup("multitenant.project", null);
		String username = configuration.lookup("multitenant.user", null);
		String password = configuration.lookup("multitenant.password", null);

		String message = "Invalid multitenant configuration";

		if (username == null || password == null) {
			throw new OpsException(message);
		}

		OpsUser user;
		try {
			user = userRepository.authenticateWithPassword(projectKey, username, password);
		} catch (RepositoryException e) {
			throw new OpsException(message, e);
		}

		OpsProject project;
		try {
			project = userRepository.findProject(user, projectKey);
		} catch (RepositoryException e) {
			throw new OpsException(message, e);
		}

		if (project == null) {
			throw new OpsException(message);
		}

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

		MultitenantConfiguration config = new SimpleMultitenantConfiguration(project, mappedItems);

		return config;
	}
}
