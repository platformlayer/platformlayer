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

	public Sanitizer(Decision defaultDecision, char blockChar) {
		this.defaultDecision = defaultDecision;
		this.blockChar = blockChar;

	}

	public static Sanitizer forFileName() {
		Sanitizer sanitizer = new Sanitizer(Decision.Block, '_');
		sanitizer.allowAlphanumeric();
		sanitizer.allowCharacters("_-.");
		return sanitizer;
	}

	private void allowCharacters(String s) {
		for (int i = 0; i < s.length(); i++) {
			int cp = s.codePointAt(i);
			characterRules.put(cp, Decision.Allow);
		}
	}

	private void allowAlphanumeric() {
		// typeRules.put((int) Character.LETTER_NUMBER, Decision.Allow);
		typeRules.put((int) Character.DECIMAL_DIGIT_NUMBER, Decision.Allow);
		typeRules.put((int) Character.LOWERCASE_LETTER, Decision.Allow);
		typeRules.put((int) Character.UPPERCASE_LETTER, Decision.Allow);
	}

	public static enum Decision {
		Allow, Block;
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

			case Block:
				out.append(blockChar);
				break;

			default:
				throw new IllegalStateException();
			}

		}

		return out.toString();
	}
}
