package org.platformlayer.ops.supervisor;

import java.util.Map;

import com.google.common.collect.Maps;

public class SupervisorProcessConfig {
	final String id;

	final Map<String, String> properties = Maps.newHashMap();

	final Map<String, String> environment = Maps.newHashMap();

	public SupervisorProcessConfig(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	String buildEnvironmentString() {
		if (environment.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append(entry.getKey() + "=\"" + entry.getValue() + "\"");
		}
		return sb.toString();
	}

	public String buildConfigFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("[program:" + id + "]\n");
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			sb.append(entry.getKey() + "=" + entry.getValue() + "\n");
		}
		String env = buildEnvironmentString();
		if (env != null) {
			sb.append("environment=" + env + "\n");
		}

		return sb.toString();
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	// [program:cat]
	// command=/bin/cat
	// process_name=%(program_name)s
	// numprocs=1
	// directory=/tmp
	// umask=022
	// priority=999
	// autostart=true
	// autorestart=true
	// startsecs=10
	// startretries=3
	// exitcodes=0,2
	// stopsignal=TERM
	// stopwaitsecs=10
	// user=chrism
	// redirect_stderr=false
	// stdout_logfile=/a/path
	// stdout_logfile_maxbytes=1MB
	// stdout_logfile_backups=10
	// stdout_capture_maxbytes=1MB
	// stderr_logfile=/a/path
	// stderr_logfile_maxbytes=1MB
	// stderr_logfile_backups=10
	// stderr_capture_maxbytes=1MB
	// environment=A=1,B=2
	// serverurl=AUTO
}
