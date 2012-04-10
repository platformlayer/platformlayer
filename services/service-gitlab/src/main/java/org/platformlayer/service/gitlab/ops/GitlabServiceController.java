package org.platformlayer.service.gitlab.ops;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.users.PosixGroup;
import org.platformlayer.ops.users.PosixUser;
import org.platformlayer.service.gitlab.model.GitlabService;

import com.google.common.base.Splitter;

public class GitlabServiceController extends OpsTreeBase implements TemplateDataSource {
    static final Logger log = Logger.getLogger(GitlabServiceController.class);

    @Handler
    public void doOperation() throws OpsException, IOException {
    }

    @Override
    protected void addChildren() throws OpsException {
        GitlabService model = OpsContext.get().getInstance(GitlabService.class);

        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
        addChild(instance);

        instance.addChildren(PackageDependency
                .build(Splitter
                        .on(" ")
                        .split("sudo git-core wget curl gcc checkinstall libxml2-dev libxslt-dev sqlite3 libsqlite3-dev libcurl4-openssl-dev libreadline-dev libc6-dev libssl-dev libmysql++-dev make build-essential zlib1g-dev libicu-dev redis-server openssh-server git-core python-dev python-pip sendmail")));

        {
            PosixGroup group = injected(PosixGroup.class);
            group.groupName = "git";
            instance.addChild(group);
        }

        {
            PosixUser user = injected(PosixUser.class);
            user.userName = "git";
            user.primaryGroup = "git";
            instance.addChild(user);
        }

        {
            PosixUser user = injected(PosixUser.class);
            user.userName = "gitlab";
            user.secondaryGroups.add("sudo");
            user.secondaryGroups.add("git");
            instance.addChild(user);
        }

        // sudo -H -u gitlab ssh-keygen -q -N '' -t rsa -f /home/gitlab/.ssh/id_rsa

        instance.addChild(PackageDependency.build("ruby1.9.1"));

        {
            GitCheckout checkout = injected(GitCheckout.class);
            checkout.targetDir = new File("/opt/gitlabhq");
            checkout.source = "https://github.com/gitlabhq/gitlabhq.git";
            instance.addChild(checkout);
        }

        {
            GitCheckout checkout = injected(GitCheckout.class);
            checkout.targetDir = new File("/opt/gitolite");
            checkout.source = "https://github.com/gitlabhq/gitolite.git";
            instance.addChild(checkout);
        }

        // Eeek... we have to run a gitolite install script.
        // I don't fancy picking it apart right now...

        // As gitlabhq...
        // git config --global user.email "admin@local.host"
        // git config --global user.name "GitLabHQ Admin User"

        // su -c "ssh-keygen -q -f /home/gitlabhq/.ssh/id_rsa -N ''" gitlabhq
    }

    @Override
    public void buildTemplateModel(Map<String, Object> model) throws OpsException {
        // // TODO: Don't hard-code this
        // String authLdapUrl =
        // "ldap://192.168.192.67:389/ou=Users,dc=com,dc=fathomscale?uid";
        // String authLDAPBindDN = "cn=Manager,dc=com,dc=fathomscale";
        // String authLDAPBindPassword = "adminsecret";
        // String requireLdapGroup = "cn=Git,ou=Groups,dc=com,dc=fathomscale";
        //
        // model.put("AuthLDAPURL", authLdapUrl);
        // model.put("AuthLDAPBindDN", authLDAPBindDN);
        // model.put("AuthLDAPBindPassword", authLDAPBindPassword);
        // model.put("requireLdapGroup", requireLdapGroup);
    }
}
