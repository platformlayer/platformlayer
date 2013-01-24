package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.common.JobState;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;

import com.fathomdb.cli.autocomplete.AutoCompletor;
import com.fathomdb.cli.autocomplete.SimpleAutoCompleter;
import com.fathomdb.cli.commands.Ansi;

public class ListJobExecutions extends PlatformLayerCommandRunnerBase {
	// @Argument(index = 0)
	// public ItemPath path;

	public ListJobExecutions() {
		super("list", "runs");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		JobExecutionList jobs = client.listJobExecutions();

		// if (path != null) {
		// PlatformLayerKey resolved = path.resolve(getContext());
		//
		// JobExecutionList matches = JobExecutionList.create();
		//
		// for (JobExecutionData run : jobs.getRuns()) {
		// if (!Objects.equal(run.getJob()..getTargetItemKey(), resolved)) {
		// continue;
		// }
		//
		// matches.runs.add(run);
		// }
		//
		// jobs = matches;
		// }

		return jobs;
	}

	@Override
	public AutoCompletor getAutoCompleter() {
		return new SimpleAutoCompleter();
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		JobExecutionList jobs = (JobExecutionList) o;

		Ansi ansi = new Ansi(writer);

		for (JobExecutionData job : jobs) {
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

			writer.println(job.executionId);
		}

		ansi.reset();
	}

}
