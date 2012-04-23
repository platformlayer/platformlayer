package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobState;

import com.fathomdb.cli.autocomplete.AutoCompletor;
import com.fathomdb.cli.autocomplete.SimpleAutoCompleter;
import com.fathomdb.cli.commands.Ansi;

public class ListJobs extends PlatformLayerCommandRunnerBase {
	public ListJobs() {
		super("list", "jobs");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		return client.listJobs();
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
