package org.platformlayer.service.certificates.ops;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.platformlayer.crypto.CertificateReader;
import org.platformlayer.crypto.KeyParser;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.crypto.ManagedSecretKey;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.certificates.model.Certificate;

public class CertificateController extends OpsTreeBase implements ManagedSecretKey {
	static final Logger log = Logger.getLogger(CertificateController.class);

	public static final int MEMCACHE_PORT = 11211;

	@Bound
	public Certificate model;

	@Handler
	public void handler() throws OpsException, IOException {
	}

	// public GoogleComputeClient getComputeClient(GoogleCloud cloud) throws OpsException {
	// byte[] data = CryptoUtils.fromBase64(cloud.serviceAccountKey.plaintext());
	//
	// KeyParser parser = new KeyParser(data);
	//
	// Object parsed = parser.parse();
	// PrivateKey privateKey;
	// if (parsed instanceof PrivateKey) {
	// privateKey = (PrivateKey) parsed;
	// } else {
	// throw new OpsException("Expected private key, found: " + parsed.getClass().getSimpleName());
	// }
	//
	// // Build service account credential.
	// GoogleCredential credential = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
	// .setJsonFactory(JSON_FACTORY).setServiceAccountId(cloud.serviceAccountId)
	// .setServiceAccountScopes(ComputeScopes.COMPUTE).setServiceAccountPrivateKey(privateKey).build();
	//
	// Compute compute = new Compute(HTTP_TRANSPORT, JSON_FACTORY, credential);
	//
	// return new GoogleComputeClient(platformLayerClient, compute, cloud.projectId);
	// }

	@Override
	protected void addChildren() throws OpsException {
		Certificate model = OpsContext.get().getInstance(Certificate.class);

		// InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
		// DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		//
		// // TODO: Memory _really_ needs to be configurable here!
		// instance.publicPorts.add(MEMCACHE_PORT);
		//
		// instance.minimumMemoryMb = 1024;
		//
		// instance.hostPolicy.allowRunInContainer = true;
		// addChild(instance);
		//
		// instance.addChild(PackageDependency.build("memcached"));
		//
		// MemcacheTemplateModel template = injected(MemcacheTemplateModel.class);
		//
		// instance.addChild(TemplatedFile.build(template, new File("/etc/memcached.conf")));
		//
		// // Collectd not restarting correctly (doesn't appear to be hostname problems??)
		// // instance.addChild(CollectdCollector.build());
		//
		// {
		// PublicEndpoint endpoint = injected(PublicEndpoint.class);
		// // endpoint.network = null;
		// endpoint.publicPort = MEMCACHE_PORT;
		// endpoint.backendPort = MEMCACHE_PORT;
		// endpoint.dnsName = model.dnsName;
		//
		// endpoint.tagItem = model.getKey();
		// endpoint.parentItem = model.getKey();
		//
		// instance.addChild(endpoint);
		// }
		//
		// instance.addChild(ManagedService.build("memcached"));
	}

	@Override
	public X509Certificate getCertificate() throws OpsException {
		CertificateReader reader = new CertificateReader();
		Object parsed = reader.parse(model.certificate);
		if (parsed == null) {
			throw new OpsException("Cannot parse certificate");
		} else if (parsed instanceof X509Certificate) {
			return (X509Certificate) parsed;
		} else {
			throw new OpsException("Expected X509 certificate, found: " + parsed.getClass().getSimpleName());
		}
	}

	@Override
	public PrivateKey getPrivateKey() throws OpsException {
		KeyParser parser = new KeyParser();
		Object parsed = parser.parse(model.privateKey.plaintext());

		if (parsed == null) {
			throw new OpsException("Cannot parse private key");
		} else if (parsed instanceof PrivateKey) {
			return (PrivateKey) parsed;
		} else {
			throw new OpsException("Expected private key, found: " + parsed.getClass().getSimpleName());
		}
	}
}
