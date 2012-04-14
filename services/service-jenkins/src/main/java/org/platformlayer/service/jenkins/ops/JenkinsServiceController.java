package org.platformlayer.service.jenkins.ops;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.backups.BackupDirectory;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.imagefactory.v1.Repository;
import org.platformlayer.service.imagefactory.v1.RepositoryKey;
import org.platformlayer.service.jenkins.model.JenkinsService;

public class JenkinsServiceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(JenkinsServiceController.class);

	@Handler
	public void doOperation() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		JenkinsService model = OpsContext.get().getInstance(JenkinsService.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		instance.publicPorts.add(8080);

		instance.hostPolicy.allowRunInContainer = true;
		instance.minimumMemoryMb = 2048;

		addChild(instance);

		instance.addChild(JavaVirtualMachine.buildJava7());

		{
			PackageDependency jenkinsPackage = PackageDependency.build("jenkins");
			jenkinsPackage.repositoryKey = new RepositoryKey();
			jenkinsPackage.repositoryKey.setUrl("http://pkg.jenkins-ci.org/debian/jenkins-ci.org.key");
			jenkinsPackage.repository = new Repository();
			jenkinsPackage.repository.setKey("jenkins");
			jenkinsPackage.repository.getSource().add("deb http://pkg.jenkins-ci.org/debian binary/");
			instance.addChild(jenkinsPackage);
		}

		// We use curl for backups
		instance.addChild(PackageDependency.build("curl"));

		// Jenkins git usually relies on git being installed
		// git-core is valid on both Debian & Ubuntu
		instance.addChild(PackageDependency.build("git-core"));

		// If we're building Java projects, we'll want a JDK
		instance.addChild(PackageDependency.build("openjdk-6-jdk"));

		// Collectd not in wheezy??
		// instance.addChild(CollectdCollector.build());

		// TODO: If we're going to support SSH git....
		// TODO: We need to ssh-keygen for jenkins
		// TODO: Someone has to add the jenkins ssh key to the git repo
		// TODO: We need to set the git user variables (name & email)
		// TODO: We need to add the ssh key(s) of any git repos we're going to be using over ssh

		// su -c "ssh-keygen -q -f /var/lib/jenkins/.ssh/id_rsa -N ''" jenkins

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = 8080;
			endpoint.backendPort = 8080;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			instance.addChild(endpoint);
		}

		{
			BackupDirectory backup = injected(BackupDirectory.class);
			backup.itemKey = model.getKey();

			File jenkinsRoot = new File("/var/lib/jenkins");
			backup.backupRoot = jenkinsRoot;

			String[] excludes = { "jobs/*/workspace", "jobs/*/modules", "jobs/*/builds/*/workspace.tar.gz",
					".m2/repository" };

			for (String exclude : excludes) {
				backup.excludes.add(new File(jenkinsRoot, exclude));
			}

			instance.addChild(backup);
		}

	}
}
