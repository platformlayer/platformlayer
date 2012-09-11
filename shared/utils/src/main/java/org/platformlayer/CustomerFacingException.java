package org.platformlayer;

import java.util.List;

import org.platformlayer.ops.OpsException;

import com.google.common.collect.Lists;

/**
 * An exception where it is OK to present getMessage to the end-user
 */
public class CustomerFacingException extends OpsException {
	private static final long serialVersionUID = 1L;

	final List<Info> info = Lists.newArrayList();
	final String code;

	public CustomerFacingException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public CustomerFacingException(String code, String message) {
		super(message);
		this.code = code;
	}

	public CustomerFacingException(String code, List<Info> infos) {
		super();
		this.code = code;
		this.info.addAll(infos);
	}

	public void addInfo(Info i) {
		info.add(i);
	}

	public static class Info {
		final String field;
		final String code;
		final String message;

		public Info(String code, String message, String field) {
			this.code = code;
			this.message = message;
			this.field = field;
		}

		public String getField() {
			return field;
		}

		public String getCode() {
			return code;
		}

		public String getMessage() {
			return message;
		}

	}

	public String getCode() {
		return code;
	}

	public List<Info> getInfo() {
		return info;
	}

	public static CustomerFacingException buildRequiredField(String field) {
		String message = Character.toUpperCase(field.charAt(0)) + field.substring(1) + " is required";
		String key = "reqiured";

		return buildFieldError(field, key, message);
	}

	public static CustomerFacingException buildFieldError(String field, String key, String message) {
		List<Info> infos = Lists.newArrayList();
		infos.add(new Info(key, message, field));

		CustomerFacingException e = new CustomerFacingException("validation", infos);
		return e;
	}

	public static CustomerFacingException wrap(Exception e) {
		return new CustomerFacingException("unknown", "Internal error - please try again later", e);
	}
}
