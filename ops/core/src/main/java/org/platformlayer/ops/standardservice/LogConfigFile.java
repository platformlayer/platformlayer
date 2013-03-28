package org.platformlayer.ops.standardservice;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SyntheticFile;
import com.fathomdb.Utf8;

public class LogConfigFile extends SyntheticFile {

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		// This sort of sucks, but is probably still nicer than XML templating
		// or building the DOM
		StringBuilder xml = new StringBuilder();

		xml.append("<configuration>\n");
		xml.append("<appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\">\n");
		xml.append("<encoder>\n");

		// Note no extra Newlines...
		xml.append("<pattern>");
		xml.append("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		xml.append("</pattern>\n");

		xml.append("</encoder>\n");
		xml.append("</appender>\n");

		xml.append("\n\n");

		xml.append("<root level=\"DEBUG\">\n");
		xml.append("<appender-ref ref=\"STDOUT\" />\n");
		xml.append("</root>\n");

		xml.append("</configuration>\n");

		return Utf8.getBytes(xml.toString());
	}

	// <configuration>
	//
	// <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
	// <!-- encoders are assigned the type
	// ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
	// <encoder>
	// <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} -
	// %msg%n</pattern>
	// </encoder>
	// </appender>
	//
	// <root level="DEBUG">
	// <appender-ref ref="STDOUT" />
	// </root>
	// </configuration>

}
