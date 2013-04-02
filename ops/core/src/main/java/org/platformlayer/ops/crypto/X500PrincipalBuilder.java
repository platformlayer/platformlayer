package org.platformlayer.ops.crypto;

import java.util.List;

import javax.security.auth.x500.X500Principal;

import com.google.common.collect.Lists;

public class X500PrincipalBuilder {
	final List<X500PrincipalComponent> components = Lists.newArrayList();

	static class X500PrincipalComponent {
		final String type;
		final String value;

		public X500PrincipalComponent(String type, String value) {
			super();
			this.type = type;
			this.value = value;
		}

	}

	public void addCn(String value) {
		String escaped = escape(value);

		components.add(new X500PrincipalComponent("CN", escaped));
	}

	private String escape(String value) {
		StringBuilder sb = new StringBuilder();

		for (char c : value.toCharArray()) {
			if (c >= '0' && c <= '9') {
				sb.append(c);
			} else if (c >= 'a' && c <= 'z') {
				sb.append(c);
			} else if (c >= 'A' && c <= 'Z') {
				sb.append(c);
			} else {
				switch (c) {

				case '-':
				case '_':
				case '.':
					sb.append(c);
					break;

				default:
					throw new IllegalArgumentException("Unsure whether character is safe in X500 name: " + c);
				}
			}
		}
		return sb.toString();
	}

	public X500Principal build() {
		StringBuilder sb = new StringBuilder();
		for (X500PrincipalComponent component : components) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append(component.type + "=" + component.value);
		}
		return new X500Principal(sb.toString());
	}

}
