package org.platformlayer.ops.log;

import java.util.List;

import org.platformlayer.ops.OpsContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;

import com.google.common.collect.Lists;

public class LogbackHook<E> extends AppenderBase<E> {

	public static void attachToRootLogger() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		LogbackHook appender = new LogbackHook();
		// PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		// encoder.setContext(loggerContext);
		// encoder.setPattern("%-4relative [%thread] %-5level %logger{35} - %msg%n");
		// encoder.start();

		// appender.setLayout(new PatternLayout("%d [%t] %-5p %c %x - %m%n"));
		appender.start();

		Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		logbackLogger.addAppender(appender);
	}

	@Override
	protected void append(E e) {
		OpsContext opsContext = OpsContext.get();

		if (opsContext != null) {
			ILoggingEvent event = (ILoggingEvent) e;

			String message = null;
			Object o = event.getMessage();
			if (o != null) {
				message = o.toString();
			}
			Level level = event.getLevel();
			int levelInt = level.toInt();

			List<String[]> exceptionStacks = null;

			IThrowableProxy throwableInformation = event.getThrowableProxy();
			while (throwableInformation != null) {
				String[] exceptionStackTrace = null;
				StackTraceElementProxy[] trace = throwableInformation.getStackTraceElementProxyArray();

				String exceptionMessage = throwableInformation.getMessage();
				String exceptionClass = throwableInformation.getClassName();

				if (trace != null) {
					exceptionStackTrace = new String[1 + trace.length];
					exceptionStackTrace[0] = exceptionClass + ": " + exceptionMessage;

					for (int i = 0; i < trace.length; i++) {
						exceptionStackTrace[1 + i] = trace[i].getSTEAsString();
					}
				} else {
					exceptionStackTrace = new String[1];
					exceptionStackTrace[0] = exceptionClass + ": " + exceptionMessage;
				}

				if (exceptionStacks == null) {
					exceptionStacks = Lists.newArrayList();
				}
				exceptionStacks.add(exceptionStackTrace);

				throwableInformation = throwableInformation.getCause();
			}

			if (message != null || exceptionStacks != null) {
				opsContext.getJobLogger().logMessage(message, exceptionStacks, levelInt);

				if (levelInt >= Level.ERROR_INT) {
					// String key = "warn-" + OpsSystem.buildSimpleTimeString() + "-" + (System.nanoTime() % 1000);
					if (opsContext != null) { // && opsContext.getOperation() != null) {
						if (exceptionStacks != null && !exceptionStacks.isEmpty()) {
							String[] exceptionStack = exceptionStacks.get(0);
							if (exceptionStack != null && exceptionStack.length > 0) {
								message += "; " + exceptionStack[0];
							}
						}

						opsContext.addWarning(null, message);
					}
				}
			}
		}
	}
}