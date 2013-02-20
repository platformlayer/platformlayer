package org.platformlayer.ops.bootstrap;

import java.io.File;
import java.util.List;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.process.ProcessExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Configures locales
 * 
 * Thanks to: http://people.debian.org/~schultmc/locales.html
 * 
 * @author justinsb
 * 
 */
public class RegenerateLocales {
	private static final Logger log = LoggerFactory.getLogger(RegenerateLocales.class);

	public static final File LOCALE_GEN_FILE = new File("/etc/locale.gen");

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		List<String> actual = getGeneratedLocales(target);
		List<String> requested = getRequestedLocales(target);

		boolean missing = false;

		for (String locale : requested) {
			if (!actual.contains(locale)) {
				log.info("Detected missing locale: " + locale);
				missing = true;
			}
		}

		if (missing && OpsContext.isConfigure()) {
			log.info("Regenerating locales");
			target.executeCommand(Command.build("/usr/sbin/locale-gen"));
		}
	}

	private List<String> getRequestedLocales(OpsTarget target) throws OpsException {
		String localeGen = target.readTextFile(LOCALE_GEN_FILE);
		if (localeGen == null) {
			throw new OpsException("/etc/locale.gen not configured");
		}

		List<String> locales = Lists.newArrayList();
		for (String line : Splitter.on("\n").split(localeGen)) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith("#")) {
				continue;
			}

			// en_US.UTF-8 UTF-8 -> en_US.UTF-8
			if (line.contains(" ")) {
				line = line.substring(0, line.indexOf(' '));
			}
			locales.add(normalize(line));
		}
		return locales;
	}

	private List<String> getGeneratedLocales(OpsTarget target) throws OpsException {
		Command listLocales = Command.build("localedef --list-archive");
		ProcessExecution execution = target.executeCommand(listLocales);

		List<String> locales = Lists.newArrayList();
		for (String line : Splitter.on("\n").split(execution.getStdOut())) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			locales.add(normalize(line));
		}
		return locales;
	}

	private String normalize(String s) {
		s = s.toLowerCase();
		s = s.replace(".utf-8", ".utf8");
		return s;
	}
}
