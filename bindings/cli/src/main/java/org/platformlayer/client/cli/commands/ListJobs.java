package org.platformlayer.client.cli.commands;

import java.io.IOException;
import java.io.PrintWriter;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.xml.JsonHelper;

import com.fathomdb.cli.CliException;
import com.fathomdb.cli.autocomplete.AutoCompletor;
import com.fathomdb.cli.autocomplete.SimpleAutoCompleter;
import com.fathomdb.cli.commands.Ansi;
import com.google.common.base.Objects;

public class ListJobs extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	public ItemPath path;

	public ListJobs() {
		super("list", "jobs");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		JobDataList jobs = client.listJobs();

		if (path != null) {
			PlatformLayerKey resolved = path.resolve(getContext());

			JobDataList matches = JobDataList.create();

			for (JobData job : jobs.getJobs()) {
				if (!Objects.equal(job.getTargetItemKey(), resolved)) {
					continue;
				}

				matches.jobs.add(job);
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
		JobDataList jobs = (JobDataList) o;

		switch (getFormat()) {
		case JSON:
			JsonHelper<JobDataList> jsonHelper = JsonHelper.build(JobDataList.class);
			boolean formatted = true;
			try {
				String json = jsonHelper.marshal(jobs, formatted);
				writer.println(json);
				return;
			} catch (IOException e) {
				throw new CliException("Error formatting for output", e);
			}
		}

		Ansi ansi = new Ansi(writer);

		for (JobData job : jobs.getJobs()) {
			// JobState state = job.state;
			// if (state != null) {
			// ansi.setColorBlue();
			// switch (job.state) {
			// case FAILED:
			// ansi.setColorRed();
			// break;
			//
			// case SUCCESS:
			// ansi.setColorGreen();
			// break;
			//
			// case RUNNING:
			// ansi.setColorBlue();
			// break;
			//
			// default:
			// ansi.setColorBlue();
			// break;
			// }
			// }

			writer.println(job.key);
		}

		ansi.reset();
	}

}
