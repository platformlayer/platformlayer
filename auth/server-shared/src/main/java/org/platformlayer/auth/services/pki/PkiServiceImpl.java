package org.platformlayer.auth.services.pki;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.crypto.CertificateAndKey;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.services.PkiService;
import org.platformlayer.ops.OpsException;

import com.google.common.collect.Lists;

public class PkiServiceImpl implements PkiService {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PkiServiceImpl.class);

	@Inject
	UserDatabase repository;

	@Override
	public List<X509Certificate> signCsr(ProjectEntity project, String csr) throws OpsException {
		CertificateAndKey projectPki;
		try {
			projectPki = repository.getProjectPki(project);
		} catch (RepositoryException e) {
			throw new OpsException("Error getting project PKI info", e);
		}

		SimpleCertificateAuthority ca = new SimpleCertificateAuthority();
		ca.caCertificate = projectPki.getCertificateChain();
		ca.caPrivateKey = projectPki.getPrivateKey();

		X509Certificate certificate = ca.signCsr(csr);

		List<X509Certificate> chain = Lists.newArrayList();
		chain.add(certificate);
		for (X509Certificate cert : projectPki.getCertificateChain()) {
			chain.add(cert);
		}
		return chain;
	}
}
