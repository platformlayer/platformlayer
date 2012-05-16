package org.platformlayer.choice;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

public class RandomChooser<T> implements Chooser<T> {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(RandomChooser.class);

	private final Random random = new Random();

	@Override
	public T choose(List<T> choices) {
		synchronized (random) {
			int index = random.nextInt(choices.size());
			return choices.get(index);
		}
	}

	public static <T> RandomChooser<T> build() {
		return new RandomChooser<T>();
	}

	public static <T> T chooseRandom(List<T> candidates) {
		return RandomChooser.<T> build().choose(candidates);
	}
}
