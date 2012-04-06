package org.platformlayer.service.wordpress.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;

public class WordpressBootstrap extends OpsTreeBase {

    @Handler
    public void handler() {
    }

    public static WordpressBootstrap build() {
        return Injection.getInstance(WordpressBootstrap.class);
    }

    @Override
    protected void addChildren() throws OpsException {
        WordpressTemplateData templateData = Injection.getInstance(WordpressTemplateData.class);

        String domainName = templateData.getDomainName();

        File configBase = new File("/etc/wordpress");
        File configFile = new File(configBase, "config-" + domainName + ".php");
        addChild(TemplatedFile.build(templateData, configFile, TemplatedFile.getDefaultResourceName(getClass(), "config.php")).setFileMode("640").setGroup("www-data").setOwner("root"));

        File webBase = new File("/srv/www");
        addChild(ManagedDirectory.build(webBase, "0750").setOwner("root").setGroup("www-data"));

        File uploadParentDir = new File(webBase, "wp-uploads");
        addChild(ManagedDirectory.build(uploadParentDir, "0750").setOwner("root").setGroup("www-data"));

        File uploadDir = new File(uploadParentDir, domainName);
        addChild(ManagedDirectory.build(uploadDir, "0774").setOwner("root").setGroup("www-data"));

        File webBaseDir = new File(webBase, domainName);
        addChild(ManagedSymlink.build(webBaseDir, new File("/usr/share/wordpress")));

        // chown -R root:www-data /srv/www/$DOMAIN/wp-content
        // chmod -R 0770 /srv/www/$DOMAIN/wp-content
        // File contextDir = new File(webBaseDir, "wp-content");
        // addChild(ManagedDirectory.build(uploadDir, "0770").setOwner("root").setGroup("www-data"));
    }
}
