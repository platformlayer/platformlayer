package org.platformlayer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SetUtils {
	public static class SetCompareResults<T> {
		public List<T> leftNotRight = Lists.newArrayList();
		public List<T> rightNotLeft = Lists.newArrayList();
		public List<T> both = Lists.newArrayList();

		final Sink<T> FunctionLeftNotRight = new Sink<T>() {
			@Override
			public void apply(T arg) {
				leftNotRight.add(arg);
			}
		};
		final Sink<T> FunctionRightNotLeft = new Sink<T>() {
			@Override
			public void apply(T arg) {
				rightNotLeft.add(arg);
			}
		};

		final Sink<T> FunctionBoth = new Sink<T>() {
			@Override
			public void apply(T arg) {
				both.add(arg);
			}
		};

		public boolean isMatch() {
			return leftNotRight.isEmpty() && rightNotLeft.isEmpty();
		}

		@Override
		public String toString() {
			if (isMatch()) {
				return "SetCompareResults:Match";
			} else {
				return "SetCompareResults:[leftNotRight=" + leftNotRight + ", rightNotLeft=" + rightNotLeft + "]";
			}
		}
	}

	private static <T> Collection<T> getFastCompareCollection(Collection<T> collection) {
		if (collection instanceof Set) {
			return collection;
		} else if (collection.size() <= 8) {
			return collection;
		} else {
			return Sets.newHashSet(collection); // turn it into a hash set for O(n)!
		}
	}

	public static <T> SetCompareResults<T> setCompare(Collection<T> left, Collection<T> right) {
		SetCompareResults<T> results = new SetCompareResults<T>();
		setCompare(getFastCompareCollection(left), getFastCompareCollection(right), results.FunctionLeftNotRight,
				results.FunctionRightNotLeft, results.FunctionBoth);
		return results;
	}

	private static <T> void setCompare(Collection<T> left, Collection<T> right, Sink<T> leftNotRight,
			Sink<T> rightNotLeft, Sink<T> both) {
		if (left == null) {
			if ((right != null) && (rightNotLeft != null)) {
				for (T rightItem : right) {
					rightNotLeft.apply(rightItem);
				}
			}
			return;
		}
		if (right == null) {
			if ((left != null) && (leftNotRight != null)) {
				for (T leftItem : left) {
					leftNotRight.apply(leftItem);
				}
			}
			return;
		}
		for (T leftItem : left) {
			if (right.contains(leftItem)) {
				if (both != null) {
					both.apply(leftItem);
				}
			} else {
				if (leftNotRight != null) {
					leftNotRight.apply(leftItem);
				}
			}
		}
		for (T rightItem : right) {
			if (!left.contains(rightItem)) {
				if (rightNotLeft != null) {
					rightNotLeft.apply(rightItem);
				}
			} else {
				// Both => Already handled in left loop
			}
		}
	}
}
