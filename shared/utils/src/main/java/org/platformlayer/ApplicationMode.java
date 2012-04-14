package org.platformlayer;

public enum ApplicationMode {
	DEVELOPMENT, PRODUCTION;

	public static void onlyForDevelopment() {
		if (getMode() != DEVELOPMENT) {
			throw new IllegalStateException("Tried to use development code in non-development mode");
		}
	}

	public static ApplicationMode getMode() {
		String applicationModeString = System.getProperty("application.mode");
		if (applicationModeString == null) {
			applicationModeString = PRODUCTION.toString();
		}
		return ApplicationMode.valueOf(applicationModeString.toUpperCase());
	}

	public static boolean isDevelopment() {
		return getMode() == ApplicationMode.DEVELOPMENT;
	}

}
