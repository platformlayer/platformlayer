package org.platformlayer.service.tomcat.ops;

import java.io.File;
import java.util.Map;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.tree.OpsTreeBase;

public class TomcatUsers extends OpsTreeBase implements TemplateDataSource {
    @Handler
    public void doConfigure() throws OpsException {
    }

    public static TomcatUsers build() {
        return Injection.getInstance(TomcatUsers.class);
    }

    @Override
    protected void addChildren() throws OpsException {
        addChild(TemplatedFile.build(this, new File("/etc/tomcat6/tomcat-users.xml")));
    }

    @Override
    public void buildTemplateModel(Map<String, Object> model) throws OpsException {
        // TODO: Don't hard-code the credentials
        // TODO: Also make credentials optional
        // TODO: Should we use LDAP???
        String managerUsername = "manager";
        String managerPassword = "managersecret";

        model.put("managerUsername", managerUsername);
        model.put("managerPassword", managerPassword);
    }

}
