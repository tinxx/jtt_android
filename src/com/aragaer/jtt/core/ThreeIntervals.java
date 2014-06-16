package com.aragaer.jtt.core;

public class ThreeIntervals {
	private final DayInterval previous;
	private final DayInterval current;
	private final DayInterval next;

	public ThreeIntervals(long previousStart, long currentStart,
			long currentEnd, long nextEnd, boolean isDayCurrently) {
		previous = new DayInterval(previousStart, currentStart, !isDayCurrently);
		current = new DayInterval(currentStart, currentEnd, isDayCurrently);
		next = new DayInterval(currentEnd, nextEnd, !isDayCurrently);
	}

	public ThreeIntervals(long[] transitions, boolean isDayCurrently) {
		this(transitions[0], transitions[1], transitions[2], transitions[3], isDayCurrently);
	}

	public boolean isInCurrentInterval(long timestamp) {
		return current.contains(timestamp);
	}

	public boolean notInCurrentInterval(long timestamp) {
		return !isInCurrentInterval(timestamp);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof ThreeIntervals))
			return false;
		ThreeIntervals otherTransitions = (ThreeIntervals) other;
		return previous.equals(otherTransitions.previous)
				&& next.equals(otherTransitions.next);
	}

	public ThreeIntervals shiftToPast(long transition) {
		return new ThreeIntervals(transition, getPreviousStart(), getCurrentStart(),
				getCurrentEnd(), !isDayCurrently());
	}

	public ThreeIntervals shiftToFuture(long transition) {
		return new ThreeIntervals(getCurrentStart(), getCurrentEnd(), getNextEnd(),
				transition, !isDayCurrently());
	}

	public long getPreviousStart() {
		return previous.getStart();
	}

	public long getCurrentStart() {
		return current.getStart();
	}

	public long getCurrentEnd() {
		return current.getEnd();
	}

	public long getNextEnd() {
		return next.getEnd();
	}

	public boolean isDayCurrently() {
		return current.isDay();
	}
}
