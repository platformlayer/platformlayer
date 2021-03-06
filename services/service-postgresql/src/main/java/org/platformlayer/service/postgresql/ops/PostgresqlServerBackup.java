package org.platformlayer.service.postgresql.ops;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.platformlayer.core.model.BackupAction;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.backups.Backup;
import org.platformlayer.ops.backups.BackupContext;
import org.platformlayer.ops.backups.BackupHelpers;
import org.platformlayer.ops.backups.BackupItem;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.postgresql.model.PostgresqlServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class PostgresqlServerBackup extends OpsTreeBase {
	private static final String FORMAT = "pgdump";

	private static final Logger log = LoggerFactory.getLogger(PostgresqlServerBackup.class);

	@Inject
	BackupHelpers backups;

	@Handler
	public void doOperation() throws OpsException, IOException {
	}

	@Handler(BackupAction.class)
	public void doBackup() throws OpsException, IOException {
		OpsContext opsContext = OpsContext.get();

		// Machine machine = opsContext.getInstance(Machine.class);
		OpsTarget target = opsContext.getInstance(OpsTarget.class);

		// We use pg_dump, not pg_dumpall:
		// 1) pg_dumpall doesn't support binary dumping (?)
		// 2) pg_dumpall wouldn't let us split the dump into different files (?)

		List<String> databases = listDatabases(target);

		BackupContext backupContext = backups.getContext();

		String baseName = UUID.randomUUID().toString();

		PostgresqlServer server = OpsContext.get().getInstance(PostgresqlServer.class);

		backupContext.add(new BackupItem(server.getKey(), FORMAT, baseName));

		{
			Command dumpAll = Command.build("su postgres -c \"pg_dumpall --globals-only\"");
			Backup request = new Backup();
			request.target = target;
			request.objectName = baseName + "/pgdump_meta";
			backupContext.uploadStream(request, dumpAll);
		}

		for (String database : databases) {
			// template0 cannot be backed up
			if (database.equals("template0")) {
				continue;
			}

			// template1 can be backed up, even though it isn't typically very useful

			String fileName = "pgdump_db_" + database;
			Backup request = new Backup();
			request.target = target;
			request.objectName = baseName + "/" + fileName;

			Command dumpDatabase = Command.build("su postgres -c \"pg_dump --oids -Fc --verbose {0}\"", database);
			backupContext.uploadStream(request, dumpDatabase);
		}
	}

	private List<String> listDatabases(OpsTarget target) throws OpsException {
		Command listDatabases = Command.build("su postgres -c \"psql -A -t -c 'select datname from pg_database'\"");
		ProcessExecution listDatabasesExecution = target.executeCommand(listDatabases);
		List<String> databases = Lists.newArrayList();
		for (String database : Splitter.on('\n').split(listDatabasesExecution.getStdOut())) {
			database = database.trim();
			if (database.isEmpty()) {
				continue;
			}
			databases.add(database);
		}
		return databases;
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
