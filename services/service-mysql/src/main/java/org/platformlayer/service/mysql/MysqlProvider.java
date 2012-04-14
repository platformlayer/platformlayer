package org.platformlayer.service.mysql;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.crypto.Passwords;
import org.platformlayer.service.mysql.model.MysqlServer;
import org.platformlayer.xaas.Service;

@Service("mysql")
public class MysqlProvider extends org.platformlayer.ops.ServiceProviderBase {

	@Override
	public void beforeCreateItem(ItemBase item) throws OpsException {
		super.beforeCreateItem(item);

		// TODO: This doesn't feel like the right place for this
		if (item instanceof MysqlServer) {
			MysqlServer mysqlServer = (MysqlServer) item;
			Passwords passwords = new Passwords();

			if (Secret.isNullOrEmpty(mysqlServer.rootPassword)) {
				mysqlServer.rootPassword = passwords.generateRandomPassword(12);
			}
		}
	}

}
