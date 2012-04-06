package org.platformlayer.service.wordpress;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.ops.crypto.Passwords;
import org.platformlayer.service.wordpress.model.WordpressService;
import org.platformlayer.xaas.Service;

@Service("wordpress")
public class WordpressProvider extends ServiceProviderBase {

    @Override
    public void beforeCreateItem(ItemBase item) throws OpsException {
        super.beforeCreateItem(item);

        // TODO: This doesn't feel like the right place for this
        if (item instanceof WordpressService) {
            WordpressService wordpressService = (WordpressService) item;
            Passwords passwords = new Passwords();

            if (Secret.isNullOrEmpty(wordpressService.adminPassword)) {
                wordpressService.adminPassword = passwords.generateRandomPassword(10);
            }

            if (Secret.isNullOrEmpty(wordpressService.databasePassword)) {
                wordpressService.databasePassword = passwords.generateRandomPassword(20);
            }

            if (Secret.isNullOrEmpty(wordpressService.wordpressSecretKey)) {
                wordpressService.wordpressSecretKey = passwords.generateRandomPassword(50);
            }
        }
    }

}
