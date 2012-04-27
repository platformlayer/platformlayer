package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobState;

import com.fathomdb.cli.autocomplete.AutoCompletor;
import com.fathomdb.cli.autocomplete.SimpleAutoCompleter;
import com.fathomdb.cli.commands.Ansi;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class ListJobs extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	public ItemPath path;

	public ListJobs() {
		super("list", "jobs");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		List<JobData> jobs = Lists.newArrayList(client.listJobs());

		if (path != null) {
			PlatformLayerKey resolved = path.resolve(getContext());

			List<JobData> matches = Lists.newArrayList();

			for (JobData job : jobs) {
				if (!Objects.equal(job.targetId, resolved)) {
					continue;
				}

				matches.add(job);
			}

			jobs = matches;
		}

		return jobs;
	}

	@Override
	public AutoCompletor getAutoCompleter() {
		return new SimpleAutoCompleter();
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		Iterable<JobData> jobs = (Iterable<JobData>) o;

		Ansi ansi = new Ansi(writer);

		for (JobData job : jobs) {
			JobState state = job.state;
			if (state != null) {
				ansi.setColorBlue();
				switch (job.state) {
				case FAILED:
					ansi.setColorRed();
					break;

				case SUCCESS:
					ansi.setColorGreen();
					break;

				case RUNNING:
					ansi.setColorBlue();
					break;

				default:
					ansi.setColorBlue();
					break;
				}
			}

			writer.println(job.key);
		}

		ansi.reset();
	}

}
