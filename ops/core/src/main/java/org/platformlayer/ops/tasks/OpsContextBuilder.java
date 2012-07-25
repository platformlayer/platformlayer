package org.platformlayer.ops.tasks;

import java.util.List;

import javax.crypto.SecretKey;
import javax.inject.Inject;

import org.platformlayer.HttpPlatformLayerClient;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.RepositoryException;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.auth.DirectAuthenticationToken;
import org.platformlayer.auth.DirectAuthenticator;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.federation.FederatedPlatformLayerClient;
import org.platformlayer.federation.FederationMap;
import org.platformlayer.federation.FederationMapping;
import org.platformlayer.federation.model.FederationConfiguration;
import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.model.ProjectAuthorization;
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

import com.google.common.collect.Lists;

public class OpsContextBuilder {
	@Inject
	ServiceAuthorizationService serviceAuthenticationService;

	@Inject
	OpsSystem opsSystem;

	@Inject
	ServiceProviderHelpers serviceProviderHelpers;

	@Inject
	PlatformLayerTypedItemMapper mapper;

	public ProjectId getRunAsProjectId(ProjectAuthorization project) throws OpsException {
		ProjectAuthorization runAsProject = project; // authentication.getProject();

		MultitenantConfiguration multitenant = opsSystem.getMultitenantConfiguration();
		if (multitenant != null) {
			runAsProject = multitenant.getMasterProject();
		}

		ProjectId runAsProjectId = new ProjectId(runAsProject.getName());
		return runAsProjectId;
	}

	public OpsContext buildOpsContext(JobRecord jobRecord) throws OpsException {
		ServiceType serviceType = jobRecord.getServiceType();
		ProjectAuthorization projectAuthz = jobRecord.getProjectAuthorization();

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

		FederationMap federationMap = new FederationMap(mapper, federationMapConfig);

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

		federationMap.addDefault(defaultClient);

		ProjectId runAsProjectId = new ProjectId(runAsProject.getName());
		PlatformLayerClient platformLayerClient = FederatedPlatformLayerClient.build(runAsProjectId, federationMap);

		ServiceConfiguration serviceConfiguration = new ServiceConfiguration(serviceAuthenticationService, serviceType,
				runAsProjectId);

		ServiceAuthorization serviceAuthorization;
		try {
			serviceAuthorization = serviceConfiguration.findServiceAuthorization();
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

		JobRegistry jobRegistry = opsSystem.getJobRegistry();
		jobRegistry.startJob(jobRecord);

		OpsContext opsContext = new OpsContext(opsSystem, jobRecord, serviceConfiguration, platformLayerClient,
				projects);
		return opsContext;
	}

	public OpsContext buildTemporaryOpsContext(ServiceType serviceType, ProjectAuthorization authentication)
			throws OpsException {
		JobRecord jobRecord = new JobRecord(serviceType, authentication, null);
		return buildOpsContext(jobRecord);
	}

	public Class<?> getJavaClass(PlatformLayerKey key) {
		return opsSystem.getJavaClass(key);
	}

	private TypedPlatformLayerClient buildClient(ProjectAuthorization project) throws OpsException {
		DirectAuthenticator directAuthenticator = buildDirectAuthenticator(project);
		ProjectId projectId = new ProjectId(project.getName());

		// TODO: Introduce a direct client for "loopback" (normal) calls?
		String platformLayerUrl = OpsSystem.getPlatformLayerUrlBase();
		List<String> trustKeys = opsSystem.getServerTrustKeys();
		HttpPlatformLayerClient client = HttpPlatformLayerClient.build(platformLayerUrl, directAuthenticator,
				projectId, trustKeys);

		return new PlatformLayerHelpers(client, serviceProviderHelpers);
	}

	private DirectAuthenticator buildDirectAuthenticator(ProjectAuthorization project) {
		String auth = DirectAuthenticationToken.encodeToken(project.getId(), project.getName());
		SecretKey secret = project.getProjectSecret();

		DirectAuthenticationToken token = new DirectAuthenticationToken(auth, secret);
		DirectAuthenticator directAuthenticator = new DirectAuthenticator(token);
		return directAuthenticator;
	}

}
