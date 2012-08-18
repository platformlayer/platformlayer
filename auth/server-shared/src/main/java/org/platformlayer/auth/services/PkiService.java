package org.platformlayer.auth.services;

import java.security.cert.X509Certificate;
import java.util.List;

import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.services.pki.PkiServiceImpl;
import org.platformlayer.ops.OpsException;

import com.google.inject.ImplementedBy;

@ImplementedBy(PkiServiceImpl.class)
public interface PkiService {
	List<X509Certificate> signCsr(ProjectEntity project, String csr) throws OpsException;
}
