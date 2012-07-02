package org.platformlayer.auth.keystone;

import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openstack.keystone.services.SystemAuth;
import org.openstack.keystone.services.SystemAuthenticator;
import org.openstack.utils.Hex;

import com.google.common.collect.Sets;

public class ClientCertificateSystemAuthenticator implements SystemAuthenticator {
	private static final Logger log = Logger.getLogger(ClientCertificateSystemAuthenticator.class);

	@Override
	public SystemAuth authenticate(X509Certificate[] certChain) {
		if (certChain.length == 0) {
			return null;
		}

		X509Certificate head = certChain[0];

		Principal subject = head.getSubjectDN();
		PublicKey publicKey = head.getPublicKey();

		String publicKeyHex = Hex.toHex(publicKey.getEncoded());
		Set<String> trusted = Sets.newHashSet();
		trusted.add("30820122300d06092a864886f70d01010105000382010f003082010a0282010100945d7ba2d1513eeff00eef508025e1dde5e5b6fc2bbfdd54c75e8367b930bf2e137e01e93ab619a1bc6d6bd736ae3ac596711eeea34eabd7fce7c2114727c012f3e1ff31cea64176ef06210c4a35fed4195573010dec50918839077d77968c19147d38d1f865747b107576cada9dbe08a0e9188a197e2708ed6be55e8a8ba0ebbbd9c2ca4bc1c9ba083baddcc61bce8872e56722596523bbf6e994dbaca08c7e582656ca873d85ee076bd57df0d8255f519b2bd14632af9778500d41ac29568a2701d04bf44ae731c9699fd248b533fa28c88c7deb8bd55e44cd680fa8618873e3cc4e9cde8c6db51c45ca93938ed5d76173388497059521930f9e01cd70872d0203010001");

		if (trusted.contains(publicKeyHex)) {
			return new SystemAuth(subject.getName());
		}

		log.debug("Authentication failed - public key not recognized: " + publicKeyHex);

		return null;
	}
}
