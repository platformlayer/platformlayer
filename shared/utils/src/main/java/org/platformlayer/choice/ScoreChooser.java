package org.platformlayer.choice;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsException;

import com.google.common.base.Function;

public abstract class ScoreChooser<T, V extends Comparable<V>> implements Chooser<T> {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ScoreChooser.class);

	final boolean maximize;

	public ScoreChooser(boolean maximize) {
		this.maximize = maximize;
	}

	@Override
	public T choose(List<T> choices) throws OpsException {
		T best = null;
		V bestScore = null;

		for (T candidate : choices) {
			V score = score(candidate);

			if ((best == null) || (maximize && score.compareTo(bestScore) > 0)
					|| (!maximize && score.compareTo(bestScore) < 0)) {
				best = candidate;
				bestScore = score;
			}
		}

		return best;
	}

	protected abstract V score(T candidate);

	public static <T, V extends Comparable<V>> FunctionScoreChooser<T, V> chooseMin(Function<T, V> score) {
		return new FunctionScoreChooser<T, V>(false, score);
	}

	public static <T, V extends Comparable<V>> FunctionScoreChooser<T, V> chooseMax(Function<T, V> score) {
		return new FunctionScoreChooser<T, V>(true, score);
	}

}
