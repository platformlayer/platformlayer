package org.platformlayer.common;

import java.text.ParseException;
import java.util.Date;

import org.platformlayer.core.model.DateUtils;
import org.platformlayer.core.model.Tag;

public class DateTagKey extends TagKey<Date> {
	public DateTagKey(String key) {
		super(key, null);
	}

	@Override
	protected Date toT(String s) {
		try {
			return DateUtils.parse(s);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Cannot parse value as date: " + s, e);
		}
	}

	public Tag build(Date v) {
		String s = v != null ? DateUtils.format(v) : null;
		return Tag.build(key, s);
	}
}