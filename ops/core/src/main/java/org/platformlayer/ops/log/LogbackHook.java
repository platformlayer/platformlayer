package org.platformlayer.ops.log;

import org.platformlayer.ops.OpsContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;

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

			String[] exceptionStackTrace = null;

			IThrowableProxy throwableInformation = event.getThrowableProxy();
			if (throwableInformation != null) {
				StackTraceElementProxy[] trace = throwableInformation.getStackTraceElementProxyArray();

				if (trace != null) {
					exceptionStackTrace = new String[trace.length];
					for (int i = 0; i < exceptionStackTrace.length; i++) {
						exceptionStackTrace[i] = trace[i].getSTEAsString();
					}
				}
			}

			if (message != null || exceptionStackTrace != null) {
				opsContext.getJobLogger().logMessage(message, exceptionStackTrace, levelInt);

				if (levelInt >= Level.ERROR_INT) {
					// String key = "warn-" + OpsSystem.buildSimpleTimeString() + "-" + (System.nanoTime() % 1000);
					if (opsContext != null) { // && opsContext.getOperation() != null) {
						if (exceptionStackTrace != null && exceptionStackTrace.length >= 1) {
							message += "; " + exceptionStackTrace[0];
						}

						opsContext.addWarning(null, message);
					}
				}
			}
		}
	}
}