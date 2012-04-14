package javalang7;

import java.io.Closeable;
import java.io.IOException;

import org.apache.log4j.Logger;

public class Utils {
	static final Logger log = Logger.getLogger(Utils.class);

	public static void safeClose(Closeable closeable) {
		if (closeable == null) {
			return;
		}

		try {
			closeable.close();
		} catch (IOException e) {
			log.warn("Ignoring error while closing: " + closeable, e);
		}
	}

	public static void safeClose(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}

		try {
			closeable.close();
		} catch (Exception e) {
			log.warn("Ignoring error while closing: " + closeable, e);
		}
	}
}
