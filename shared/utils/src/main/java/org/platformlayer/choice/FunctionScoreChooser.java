package org.platformlayer.choice;

import org.apache.log4j.Logger;

import com.google.common.base.Function;

public class FunctionScoreChooser<T, V extends Comparable<V>> extends ScoreChooser<T, V> {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(FunctionScoreChooser.class);

	final Function<T, V> scoreFunction;

	public FunctionScoreChooser(boolean maximize, Function<T, V> scoreFunction) {
		super(maximize);
		this.scoreFunction = scoreFunction;
	}

	@Override
	protected V score(T candidate) {
		return scoreFunction.apply(candidate);
	}
}
