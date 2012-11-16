package org.platformlayer.service.gerrit.ops;

import java.io.File;
import java.io.IOException;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.tree.OpsTreeBase;

public class GerritBootstrap extends OpsTreeBase {
	// @Inject
	// TemplateHelpers templates;

	@Bound
	GerritTemplate template;

	@Handler
	public void handler(OpsTarget target) throws OpsException, IOException {
		File canary = new File(template.getDataDir(), "bin/gerrit.sh");

		// TODO: Upgrades need to run init

		// Note: There's a nasty migration issue (see release notes for 2.5); dropping the index first fixes it:
		// drop index submodule_subscription_access_bysubscription;

		if (target.getFilesystemInfoFile(canary) == null) {
			if (OpsContext.isConfigure()) {
				File dataDir = template.getDataDir();

				Command command = Command.build("java");
				command.addLiteral("-jar").addFile(template.getInstallWarFile());
				command.addLiteral("init");
				command.addLiteral("--no-auto-start");
				command.addLiteral("--batch");
				command.addLiteral("-d").addFile(dataDir);

				target.executeCommand(command);
			}
		}

		// DROP TABLE contributor_agreements;
		// DROP TABLE account_agreements;
		// DROP TABLE account_group_agreements;
		// ALTER TABLE accounts DROP COLUMN display_patch_sets_in_reverse_order;
		// ALTER TABLE accounts DROP COLUMN display_person_name_in_review_category;
		// ALTER TABLE tracking_ids DROP COLUMN tracking_id;
		// ALTER TABLE account_groups DROP COLUMN owner_group_id;
		// ALTER TABLE account_groups DROP COLUMN external_name;

	}

	@Override
	protected void addChildren() throws OpsException {
		// GerritInstanceModel template = injected(GerritInstanceModel.class);
		//
		// File dataDir = template.getDataDir();
		// addChild(ManagedDirectory.build(dataDir, "700").setOwner(template.getUser()));
	}

}