package org.platformlayer.ops.tasks;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.RepositoryException;
import org.platformlayer.TypedItemMapper;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.auth.DirectAuthenticationToken;
import org.platformlayer.auth.DirectAuthenticator;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.federation.FederatedPlatformLayerClient;
import org.platformlayer.federation.FederationMap;
import org.platformlayer.federation.FederationMapping;
import org.platformlayer.federation.model.FederationConfiguration;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.DirectAuthentication;
import org.platformlayer.ops.DirectPlatformLayerClient;
import org.platformlayer.ops.MultitenantConfiguration;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.ServiceConfiguration;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.machines.PlatformLayerTypedItemMapper;
import org.platformlayer.ops.machines.ServiceProviderHelpers;
import org.platformlayer.xaas.model.ServiceAuthorization;
import org.platformlayer.xaas.repository.ServiceAuthorizationService;

import com.fathomdb.crypto.CryptoKey;
import com.google.common.collect.Lists;

public class OpsContextBuilder {
	@Inject
	ServiceAuthorizationService serviceAuthorizationService;

	@Inject
	OpsSystem opsSystem;

	@Inject
	ServiceProviderHelpers serviceProviderHelpers;

	@Inject
	PlatformLayerTypedItemMapper mapper;

	@Inject
	HttpStrategy httpStrategy;

	@Inject
	JobRegistry jobRegistry;

	public ProjectId getRunAsProjectId(ProjectAuthorization project) throws OpsException {
		ProjectAuthorization runAsProject = project; // authentication.getProject();

		MultitenantConfiguration multitenant = opsSystem.getMultitenantConfiguration();
		if (multitenant != null) {
			runAsProject = multitenant.getMasterProject();
		}

		ProjectId runAsProjectId = new ProjectId(runAsProject.getName());
		return runAsProjectId;
	}

	public OpsContext buildOpsContext(ActiveJobExecution activeJob) throws OpsException {
		ServiceType serviceType = activeJob.getServiceType();
		ProjectAuthorization projectAuthz = activeJob.getProjectAuthorization();

		List<ProjectAuthorization> projects = Lists.newArrayList();

		ProjectAuthorization runAsProject = projectAuthz; // .getProject();
		projects.add(runAsProject);

		MultitenantConfiguration multitenant = opsSystem.getMultitenantConfiguration();
		if (multitenant != null) {
			ProjectAuthorization masterProject = multitenant.getMasterProject();
			if (runAsProject.getName().equals(masterProject.getName())) {
				// We're in the master project
				multitenant = null;
			} else {
				runAsProject = masterProject;
				projects.add(runAsProject);
			}
		}

		TypedPlatformLayerClient defaultClient = buildClient(runAsProject);

		FederationConfiguration federationMapConfig = FederatedPlatformLayerClient
				.buildFederationConfiguration(defaultClient);

		FederationMap federationMap = new FederationMap(httpStrategy, mapper, federationMapConfig);

		if (multitenant != null) {
			ProjectAuthorization localProject = projectAuthz; // .getProject();
			TypedPlatformLayerClient localClient = buildClient(localProject);

			FederationKey host = FederationKey.LOCAL;
			ProjectId project = localClient.getProject();
			FederationMapping mapKey = new FederationMapping(host, project);

			federationMap.addMapping(mapKey, localClient);

			for (PlatformLayerKey mappedService : multitenant.getMappedItems()) {
				FederationMap.Rule rule = new FederationMap.Rule();
				rule.mappedItems = mappedService;
				rule.targetKey = mapKey;
				federationMap.addRule(rule);
			}
		}

		ProjectId runAsProjectId = new ProjectId(runAsProject.getName());

		PlatformLayerClient platformLayerClient;
		if (federationMap.isEmpty()) {
			platformLayerClient = defaultClient;
		} else {
			federationMap.addDefault(defaultClient);

			platformLayerClient = FederatedPlatformLayerClient.build(runAsProjectId, federationMap);
		}

		ServiceConfiguration serviceConfiguration = new ServiceConfiguration(runAsProjectId, serviceType);

		ServiceAuthorization serviceAuthorization;
		try {
			serviceAuthorization = serviceAuthorizationService.findServiceAuthorization(serviceType, runAsProjectId);
			// if (serviceAuthorization == null) {
			// throw new OpsServiceNotAuthorizedException();
			// }
			if (serviceAuthorization == null) {
				serviceAuthorization = new ServiceAuthorization();
				serviceAuthorization.serviceType = serviceConfiguration.getServiceType().getKey();
			}
		} catch (RepositoryException e) {
			throw new OpsException("Error reading from repository", e);
		}

		// OpsConfig opsConfig = OpsConfig.build(serviceAuthorization);
		// UserInfo userInfo = new SimpleUserInfo(auth, opsConfig);

		OpsContext opsContext = new OpsContext(opsSystem, activeJob, serviceConfiguration, platformLayerClient,
				projects);
		return opsContext;
	}

	public OpsContext buildTemporaryOpsContext(ServiceType serviceType, ProjectAuthorization authentication)
			throws OpsException {
		ActiveJobExecution activeJob = jobRegistry.startSystemJob(serviceType, authentication);
		return buildOpsContext(activeJob);
	}

	public Class<?> getJavaClass(PlatformLayerKey key) {
		return opsSystem.getJavaClass(key);
	}

	private TypedPlatformLayerClient buildClient(ProjectAuthorization project) throws OpsException {
		ProjectId projectId = new ProjectId(project.getName());

		DirectAuthenticator directAuthenticator = buildDirectAuthenticator(project);
		// TODO: Introduce a direct client for "loopback" (normal) calls?
		String platformLayerUrl = OpsSystem.getPlatformLayerUrlBase();
		List<String> trustKeys = opsSystem.getServerTrustKeys();

		PlatformLayerClient client;

		// client = HttpPlatformLayerClient.build(httpStrategy, platformLayerUrl,
		// directAuthenticator, projectId, trustKeys);

		DirectAuthentication auth = new DirectAuthentication(project);
		TypedItemMapper mapper = null;
		client = new DirectPlatformLayerClient(mapper, opsSystem, projectId, auth);

		return new PlatformLayerHelpers(client, serviceProviderHelpers);
	}

	private DirectAuthenticator buildDirectAuthenticator(ProjectAuthorization project) {
		String auth = DirectAuthenticationToken.encodeToken(project.getId(), project.getName());
		CryptoKey secret = project.getProjectSecret();

		DirectAuthenticationToken token = new DirectAuthenticationToken(auth, secret);
		DirectAuthenticator directAuthenticator = new DirectAuthenticator(token);
		return directAuthenticator;
	}

}
