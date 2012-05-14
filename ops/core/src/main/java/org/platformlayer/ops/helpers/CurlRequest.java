package org.platformlayer.ops.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.platformlayer.TimeSpan;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.process.ProcessExecution;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class CurlRequest {
	final URI url;

	public final Multimap<String, String> headers = HashMultimap.create();
	static final String metadataDelimiter = "\n\n";;

	public TimeSpan timeout;

	public String proxy;

	public String method;

	public String body;
	public boolean bodyFromStdin;
	public boolean bareRequest;

	public CurlRequest(String url) {
		try {
			this.url = new URI(url);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Error parsing URI", e);
		}
	}

	public CurlRequest(URI url) {
		this.url = url;
	}

	public URI getUrl() {
		return url;
	}

	public CurlResult executeRequest(OpsTarget target) throws OpsException {
		Command command = toCommand();

		ProcessExecution execution = target.executeCommand(command);

		return parseResponse(execution);
	}

	public CurlResult parseResponse(ProcessExecution execution) {
		if (bareRequest) {
			throw new IllegalStateException();
		}

		List<String> tags = buildTags();

		String stdout = execution.getStdOut();

		String bodyDelimiter = "\r\n\r\n";
		int bodyStart = stdout.indexOf(bodyDelimiter);
		int metadataStart = stdout.lastIndexOf(metadataDelimiter);
		if (bodyStart == -1 || metadataStart == -1) {
			throw new IllegalStateException("Unexpected format for curl output: " + stdout);
		}

		String headers = stdout.substring(0, bodyStart);
		String contents = stdout.substring(bodyStart + bodyDelimiter.length(), metadataStart);
		String metadata = stdout.substring(metadataStart + metadataDelimiter.length());

		String[] metadataLines = metadata.split("\n");

		if (metadataLines.length != tags.size()) {
			throw new IllegalStateException("Unable to match up curl metadata: " + metadata);
		}

		Map<String, String> metadataMap = Maps.newHashMap();

		for (int i = 0; i < metadataLines.length; i++) {
			String tag = tags.get(i);
			metadataMap.put(tag, metadataLines[i]);
		}

		return new CurlResult(contents, headers, metadataMap);
	}

	public Multimap<String, String> getHeaders() {
		return headers;
	}

	public Command toCommand() {
		List<String> tags = buildTags();

		List<String> escaped = Lists.newArrayList();
		for (String tag : tags) {
			escaped.add("%{" + tag + "}");
		}

		String format = metadataDelimiter.replace("\n", "\\n") + Joiner.on("\\n").join(escaped);

		Command command = Command.build("curl");
		if (!bareRequest) {
			command.addLiteral("--include");
			command.addLiteral("--write-out");
			command.addQuoted(format);
		}

		if (proxy != null) {
			command.addLiteral("--proxy");
			command.addQuoted(proxy);
		}

		if (timeout != null) {
			command.addLiteral("--max-time");
			command.addQuoted(Long.toString(timeout.getTotalSeconds() + 1));
		}

		for (Entry<String, String> entry : headers.entries()) {
			command.addLiteral("-H");
			command.addQuoted(entry.getKey() + ": " + entry.getValue());
		}

		if (body != null) {
			command.addLiteral("--data");
			command.addQuoted(body);
		}

		if (bodyFromStdin) {
			command.addLiteral("--data-binary");
			command.addLiteral("@-");
		}

		if (method != null) {
			command.addLiteral("--request");
			command.addQuoted(method);
		}

		command.addQuoted(getUrl().toString());
		return command;
	}

	private List<String> buildTags() {
		List<String> tags = Lists.newArrayList();
		tags.add(CurlResult.METADATA_HTTP_CODE);
		tags.add(CurlResult.METADATA_TIME_TOTAL);
		return tags;
	}
}
