package org.platformlayer.ops.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.Name;

import com.google.common.collect.Lists;
import com.sun.jndi.ldap.LdapName;

public class LdapDN {
	final List<LdapDNComponent> components;

	public static class LdapDNComponent {
		public final String attributeName;
		public final String value;

		public LdapDNComponent(String attributeName, String value) {
			this.attributeName = attributeName;
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			LdapDNComponent other = (LdapDNComponent) obj;
			if (attributeName == null) {
				if (other.attributeName != null) {
					return false;
				}
			} else if (!attributeName.equals(other.attributeName)) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}
	};

	public LdapDN(List<LdapDNComponent> components) {
		this.components = components;
	}

	public LdapDN(String attributeName, String value) {
		this(Collections.singletonList(new LdapDNComponent(attributeName, value)));
	}

	public LdapDN childDN(String attribute, String value) {
		List<LdapDNComponent> childComponents = Lists.newArrayList(components);
		childComponents.add(0, new LdapDNComponent(attribute, value));
		return new LdapDN(childComponents);
	}

	@Override
	public String toString() {
		return this.toLdifEncoded();
	}

	public LdapDNComponent getHead() {
		return components.get(0);
	}

	public static LdapDN fromDomainName(String hostName) {
		// fathomdb.test => "dc=fathomdb,dc=test"
		String[] hostNameComponents = hostName.split("\\.");

		ArrayList<LdapDNComponent> components = Lists.newArrayList();
		for (String hostNameComponent : hostNameComponents) {
			components.add(new LdapDNComponent(LdapAttributes.LDAP_ATTRIBUTE_DC, hostNameComponent));
		}

		return new LdapDN(components);
	}

	public static LdapDN parseLdifEncoded(String value) {
		if (value == null) {
			return null;
		}

		List<LdapDNComponent> components = Lists.newArrayList();
		StringBuilder current = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '\\') {
				// Escape char
				i++;
				c = value.charAt(i);
				current.append(c);
			} else if (c == ',') {
				components.add(parseComponentPair(current));
				current.setLength(0);
			} else {
				// Normal
				current.append(c);
			}
		}

		// Be sure to add the last component
		if (current.length() != 0) {
			components.add(parseComponentPair(current));
		}

		return new LdapDN(components);
	}

	private static LdapDNComponent parseComponentPair(StringBuilder current) {
		int equalsPos = current.indexOf("=");
		if (equalsPos == -1) {
			throw new IllegalArgumentException("Cannot parse: " + current);
		} else {
			return new LdapDNComponent(current.substring(0, equalsPos), current.substring(equalsPos + 1));
		}
	}

	public String toLdifEncoded() {
		StringBuilder ldif = new StringBuilder();
		int count = 0;
		for (LdapDNComponent component : components) {
			if (count != 0) {
				ldif.append(',');
			}
			String encoded = component.attributeName + "=" + escapeForLdif(component.value);
			ldif.append(encoded);

			count++;
		}
		return ldif.toString();
	}

	public Name asJndiName() {
		try {
			LdapName name = null;
			for (LdapDNComponent component : components) {
				String encoded = component.attributeName + "=" + escapeForJndi(component.value);
				if (name == null) {
					name = new LdapName(encoded);
				} else {
					name.add(0, encoded);
				}
			}
			return name;
		} catch (InvalidNameException e) {
			throw new IllegalStateException("Unexpected invalid name: " + this, e);
		}
	}

	private String escapeForLdif(String raw) {
		StringBuilder escaped = new StringBuilder();

		for (char c : raw.toCharArray()) {
			if (Character.isLetterOrDigit(c)) {
				escaped.append(c);
			} else {
				switch (c) {
				case '-':
				case '{':
				case '}':
					escaped.append(c);
					break;

				case ',':
				case ' ':
				case '*':
				case '(':
				case ')':
				case '|':
				case '=':
					escaped.append('\\');
					escaped.append(c);
					break;

				default:
					throw new IllegalStateException("Unhandled character: " + c);
				}
			}
		}
		return escaped.toString();
	}

	private String escapeForJndi(String raw) {
		StringBuilder escaped = new StringBuilder();

		for (char c : raw.toCharArray()) {
			if (Character.isLetterOrDigit(c)) {
				escaped.append(c);
			} else {
				switch (c) {
				case ',':
					escaped.append('\\');
					escaped.append(c);
					break;
				case ' ':
				case '(':
				case ')':
				case '|':
				case '=':
				case '*':
				case '-':
					escaped.append(c);
					break;

				default:
					throw new IllegalStateException("Unhandled character: " + c);
				}
			}
		}
		return escaped.toString();
	}

	public List<LdapDNComponent> getComponents() {
		return components;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((components == null) ? 0 : components.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LdapDN other = (LdapDN) obj;
		if (components == null) {
			if (other.components != null) {
				return false;
			}
		} else if (!components.equals(other.components)) {
			return false;
		}
		return true;
	}

}
