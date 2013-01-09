package org.platformlayer.service.httpproxy.ops;

import java.net.URI;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.proxy.HttpProxyController;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.httpproxy.model.UnmanagedHttpProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnmanagedHttpProxyController extends OpsTreeBase implements HttpProxyController {

	private static final Logger log = LoggerFactory.getLogger(UnmanagedHttpProxyController.class);

	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
	}

	@Override
	public String getUrl(Object modelObject, NetworkPoint forNetworkPoint, URI uri) throws OpsException {
		UnmanagedHttpProxy model = (UnmanagedHttpProxy) modelObject;

		if (!canProxy(model, uri)) {
			log.warn("Won't proxy: " + uri);
			return null;
		}

		return model.url;
	}

	boolean canProxy(UnmanagedHttpProxy model, URI uri) throws OpsException {
		if (uri == null) {
			return true;
		}

		int port = uri.getPort();
		String scheme = uri.getScheme();

		if (scheme.equals("http")) {
			if ((port != -1) && (port != 80)) {
				return false;
			}
			return true;
		}
		if (scheme.equals("https")) {
			if ((port != -1) && (port != 443)) {
				return false;
			}
			return true;
		}

		return false;
	}
}
