package org.platformlayer.ops.databases;

import javax.inject.Inject;

import org.slf4j.*;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ProviderHelper;

public class DatabaseHelper {
	static final Logger log = LoggerFactory.getLogger(DatabaseHelper.class);

	@Inject
	ProviderHelper providerHelper;

	// public List<String> findDatabases(OpsTarget target, URI uri) throws OpsException {
	// List<String> proxies = Lists.newArrayList();
	//
	// for (ProviderOf<Database> provider : providerHelper.listItemsProviding(Database.class)) {
	// ItemBase item = provider.getItem();
	// if (item.getState() != ManagedItemState.ACTIVE) {
	// continue;
	// }
	//
	// Database database = provider.get();
	//
	// NetworkPoint forNetworkPoint = NetworkPoint.forTarget(target);
	// String url = httpProxy.getUrl(provider.getItem(), forNetworkPoint, uri);
	// if (url == null) {
	// log.info("Could not get URL for proxy: " + item);
	// } else {
	// proxies.add(url);
	// }
	// }
	//
	// // TODO: Support grabbing a proxy from the configuration
	// // proxies.add("http://192.168.192.40:3142/");
	//
	// return proxies;
	// }

	public Database toDatabase(ItemBase item) throws OpsException {
		return providerHelper.toInterface(item, Database.class);
	}

}
