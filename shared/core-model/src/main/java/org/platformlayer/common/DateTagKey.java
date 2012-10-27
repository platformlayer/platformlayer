package org.platformlayer.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.platformlayer.core.model.Tag;

public class DateTagKey extends TagKey<Date> {
	public DateTagKey(String key) {
		super(key, null);
	}

	// Not static to avoid locking problems
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	@Override
	protected Date toT(String s) {
		try {
			return dateFormat.parse(s);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Cannot parse value as date: " + s, e);
		}
	}

	public Tag build(Date v) {
		String s = v != null ? dateFormat.format(v) : null;
		return new Tag(key, s);
	}
}