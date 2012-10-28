package org.platformlayer.core.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	public static String format(Date v) {
		// Not static to avoid locking problems
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return dateFormat.format(v);
	}

	public static Date parse(String s) throws ParseException {
		// Not static to avoid locking problems
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return dateFormat.parse(s);
	}

}
