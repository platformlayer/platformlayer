package org.platformlayer.choice;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsException;

import com.google.common.base.Function;

public class ScoreChooser<T, V extends Comparable<V>> implements Chooser<T> {
	private static final Logger log = Logger.getLogger(ScoreChooser.class);

	final Function<T, V> scoreFunction;
	final boolean maximize;

	public ScoreChooser(Function<T, V> scoreFunction, boolean maximize) {
		this.scoreFunction = scoreFunction;
		this.maximize = maximize;
	}

	@Override
	public T choose(List<T> choices) throws OpsException {
		T best = null;
		V bestScore = null;

		for (T candidate : choices) {
			V score = scoreFunction.apply(candidate);

			if ((best == null) || (maximize && score.compareTo(bestScore) > 0)
					|| (!maximize && score.compareTo(bestScore) < 0)) {
				best = candidate;
				bestScore = score;
			}
		}

		return best;
	}

	public static <T, V extends Comparable<V>> ScoreChooser<T, V> chooseMin(Function<T, V> score) {
		return new ScoreChooser<T, V>(score, false);
	}

	public static <T, V extends Comparable<V>> ScoreChooser<T, V> chooseMax(Function<T, V> score) {
		return new ScoreChooser<T, V>(score, true);
	}
}
