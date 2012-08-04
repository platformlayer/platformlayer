package org.platformlayer.ops.firewall;

import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

public class Sanitizer {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(Sanitizer.class);

	private final Decision defaultDecision;
	private final char blockChar;
	private final Map<Integer, Decision> typeRules = Maps.newHashMap();
	private final Map<Integer, Decision> characterRules = Maps.newHashMap();

	boolean combineBlocked;

	public Sanitizer(Decision defaultDecision, char blockChar) {
		this.defaultDecision = defaultDecision;
		this.blockChar = blockChar;

	}

	public static Sanitizer forFileName() {
		Sanitizer sanitizer = new Sanitizer(Decision.Replace, '_');
		sanitizer.allowAlphanumeric();
		sanitizer.allowCharacters("_-.");
		return sanitizer;
	}

	public void allowCharacters(String s) {
		setDecision(s, Decision.Allow);
	}

	public void setDecision(String s, Decision decision) {
		for (int i = 0; i < s.length(); i++) {
			int cp = s.codePointAt(i);
			characterRules.put(cp, decision);
		}
	}

	public Sanitizer allowAlphanumeric() {
		// typeRules.put((int) Character.LETTER_NUMBER, Decision.Allow);
		typeRules.put((int) Character.DECIMAL_DIGIT_NUMBER, Decision.Allow);
		typeRules.put((int) Character.LOWERCASE_LETTER, Decision.Allow);
		typeRules.put((int) Character.UPPERCASE_LETTER, Decision.Allow);

		return this;
	}

	public static enum Decision {
		Allow, Replace, Throw;
	};

	public String clean(String s) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			int c = s.codePointAt(i);
			int type = Character.getType(c);
			Decision decision = typeRules.get(type);
			if (decision == null) {
				decision = characterRules.get(c);
				if (decision == null) {
					log.debug("Using default decision for character: " + s.charAt(i));
					decision = defaultDecision;
				}
			}

			switch (decision) {
			case Allow:
				out.appendCodePoint(c);
				break;

			case Replace: {
				boolean writeBlockChar = true;

				if (combineBlocked) {
					if (out.length() != 0) {
						char lastChar = out.charAt(out.length() - 1);
						if (lastChar == blockChar) {
							writeBlockChar = false;
						}
					}
				}

				if (writeBlockChar) {
					out.append(blockChar);
				}
			}
				break;

			case Throw: {
				throw new IllegalArgumentException("Found invalid character: " + c);
			}

			default:
				throw new IllegalStateException();
			}
		}

		return out.toString();
	}

	public boolean isCombineBlocked() {
		return combineBlocked;
	}

	public Sanitizer setCombineBlocked(boolean combineBlocked) {
		this.combineBlocked = combineBlocked;

		return this;
	}

}
