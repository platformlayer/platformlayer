package org.openstack.keystone.resources.admin;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.model.SignCertificateRequest;
import org.platformlayer.auth.model.SignCertificateResponse;
import org.platformlayer.auth.services.PkiService;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.crypto.CertificateUtils;
import org.platformlayer.ops.OpsException;

import com.google.common.collect.Lists;

@Path("/pki")
public class PkiResource extends RootResource {
	static final Logger log = Logger.getLogger(PkiResource.class);

	@Inject
	PkiService pki;

	@POST
	@Path("csr")
	public SignCertificateResponse signCertificate(SignCertificateRequest request) {
		try {
			requireSystemAccess();
		} catch (AuthenticatorException e) {
			log.warn("Error while checking system token", e);
			throwInternalError();
		}

		// TokenInfo checkTokenInfo = tokenService.decodeToken(checkToken);
		// if (checkTokenInfo == null || checkTokenInfo.hasExpired()) {
		// throw404NotFound();
		// }
		//
		// UserEntity user = null;
		// try {
		// user = userAuthenticator.getUserFromToken(checkTokenInfo.userId, checkTokenInfo.tokenSecret);
		// } catch (AuthenticatorException e) {
		// log.warn("Error while fetching user", e);
		// throwInternalError();
		// }
		//
		// if (user == null) {
		// throw404NotFound();
		// }

		String projectKey = request.project;

		ProjectEntity project = null;

		try {
			project = userAuthenticator.findProject(projectKey);
		} catch (AuthenticatorException e) {
			log.warn("Error while fetching project", e);
			throwInternalError();
		}

		if (project == null) {
			throw404NotFound();
		}

		project.setProjectSecret(AesUtils.deserializeKey(request.projectSecret));

		// Note that we do not unlock the user / project; we don't have any secret material
		// TODO: We could return stuff encrypted with the user's public key
		// projectEntity.unlockWithUser(userEntity);
		//
		// if (!projectEntity.isSecretValid()) {
		// throw404NotFound();
		// }

		// UserProjectEntity userProject = null;
		// try {
		// userProject = userAuthenticator.findUserProject(user, project);
		// } catch (AuthenticatorException e) {
		// log.warn("Error while fetching project", e);
		// throwInternalError();
		// }
		//
		// if (userProject == null) {
		// // Not a member of project
		// throw404NotFound();
		// }
		//
		// boolean isOwner = false;
		// for (RoleId role : userProject.getRoles()) {
		// if (role.equals(RoleId.OWNER)) {
		// isOwner = true;
		// }
		// }
		//
		// if (!isOwner) {
		// throwUnauthorized();
		// }

		List<X509Certificate> certificates = null;
		try {
			certificates = pki.signCsr(project, request.csr);
		} catch (OpsException e) {
			log.warn("Error while signing CSR", e);
			throwInternalError();
		}

		SignCertificateResponse response = new SignCertificateResponse();

		response.certificates = Lists.newArrayList();
		for (X509Certificate cert : certificates) {
			response.certificates.add(CertificateUtils.toPem(cert));
		}
		return response;
	}

}
