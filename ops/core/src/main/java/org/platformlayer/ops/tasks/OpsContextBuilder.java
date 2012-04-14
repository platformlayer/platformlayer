package org.platformlayer.ops.tasks;

import javax.inject.Inject;

import org.platformlayer.RepositoryException;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsConfig;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.ServiceConfiguration;
import org.platformlayer.ops.UserInfo;
import org.platformlayer.ops.auth.OpsAuthentication;
import org.platformlayer.xaas.model.ServiceAuthorization;
import org.platformlayer.xaas.repository.ServiceAuthorizationService;

public class OpsContextBuilder {
	@Inject
	ServiceAuthorizationService serviceAuthenticationService;

	@Inject
	OpsSystem opsSystem;

	public OpsContext buildOpsContext(JobRecord jobRecord) throws OpsException {
		ServiceType serviceType = jobRecord.getServiceType();
		OpsAuthentication auth = jobRecord.getAuth();

		ServiceConfiguration serviceConfiguration = new ServiceConfiguration(serviceAuthenticationService, serviceType,
				auth.getProjectId());

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

		OpsConfig opsConfig = OpsConfig.build(serviceAuthorization);
		UserInfo userInfo = new UserInfo(auth, opsConfig);

		JobRegistry jobRegistry = opsSystem.getJobRegistry();
		jobRegistry.startJob(jobRecord);

		OpsContext opsContext = new OpsContext(opsSystem, jobRecord, userInfo, serviceConfiguration);
		return opsContext;
	}

	public OpsContext buildTemporaryOpsContext(ServiceType serviceType, OpsAuthentication authentication)
			throws OpsException {
		JobRecord jobRecord = new JobRecord(serviceType, authentication);
		return buildOpsContext(jobRecord);
	}

	public Class<?> getJavaClass(PlatformLayerKey key) {
		return opsSystem.getJavaClass(key);
	}

}
