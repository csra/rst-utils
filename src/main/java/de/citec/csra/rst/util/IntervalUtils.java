/* 
 * Copyright (C) 2017 Patrick Holthaus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.citec.csra.rst.util;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.stream.Collectors;
import net.time4j.Moment;
import net.time4j.SystemClock;
import net.time4j.range.ChronoInterval;
import net.time4j.range.IntervalCollection;
import net.time4j.range.MomentInterval;
import net.time4j.scale.TimeScale;
import rst.timing.IntervalType.Interval;
import rst.timing.TimestampType.Timestamp;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class IntervalUtils {

	private final static class LengthComparator implements Comparator<ChronoInterval<Moment>> {

		@Override
		public int compare(ChronoInterval<Moment> o1, ChronoInterval<Moment> o2) {
			MomentInterval m1 = (MomentInterval) o1;
			MomentInterval m2 = (MomentInterval) o2;
			long length1 = m1.getRealDuration().getSeconds() * 1000000000 + m1.getRealDuration().getFraction();
			long length2 = m2.getRealDuration().getSeconds() * 1000000000 + m2.getRealDuration().getFraction();
			return (int) (length1 - length2);
		}
	}

	@Deprecated
	public static Interval buildRelativeRst(long delay, long duration) {
		return buildRelativeRst(delay, duration, MILLISECONDS);
	}

	@Deprecated
	public static Interval buildRst(long delay, long duration) {
		return buildRst(delay, duration, MILLISECONDS);
	}

	public static Interval buildRelativeRst(long delay, long duration, TimeUnit unit) {
		long now = currentTimeInMicros();
		delay = MICROSECONDS.convert(delay, unit);
		duration = MICROSECONDS.convert(duration, unit);
		return Interval.newBuilder().
				setBegin(Timestamp.newBuilder().setTime(now + delay)).
				setEnd(Timestamp.newBuilder().setTime(now + delay + duration)).build();
	}

	public static Interval buildRst(long begin, long end, TimeUnit unit) {
		return Interval.newBuilder().
				setBegin(Timestamp.newBuilder().setTime(MICROSECONDS.convert(begin, unit))).
				setEnd(Timestamp.newBuilder().setTime(MICROSECONDS.convert(end, unit))).build();
	}

	private final static SystemClock CLOCK = SystemClock.INSTANCE;
	private final static Comparator<ChronoInterval<Moment>> BY_LENGTH = new LengthComparator();
	private final static Comparator<ChronoInterval<Moment>> BY_BEGIN = MomentInterval.comparator();

	public static MomentInterval fromRst(Interval i) {
		if (i != null) {
			long startMicros = i.getBegin().getTime();
			long stopMicros = i.getEnd().getTime();

			long startSeconds = startMicros / 1000000;
			long stopSeconds = stopMicros / 1000000;

			int startNanos = (int) ((startMicros % 1000000) * 1000);
			int stopNanos = (int) ((stopMicros % 1000000) * 1000);

			Moment startMoment = Moment.of(startSeconds, startNanos, TimeScale.POSIX);
			Moment stopMoment = Moment.of(stopSeconds, stopNanos, TimeScale.POSIX);
			return MomentInterval.between(startMoment, stopMoment);
		} else {
			return null;
		}
	}

	public static List<MomentInterval> fromRst(List<Interval> i) {
		return i.stream().map(e -> fromRst(e)).collect(Collectors.toList());
	}

	public static Interval fromT4j(MomentInterval i) {
		if (i != null) {
			Moment startMoment = i.getStartAsMoment();
			Moment stopMoment = i.getEndAsMoment();
			long start
					= startMoment.getElapsedTime(TimeScale.POSIX) * 1000000
					+ startMoment.getNanosecond() / 1000;
			long stop
					= stopMoment.getElapsedTime(TimeScale.POSIX) * 1000000
					+ stopMoment.getNanosecond() / 1000;

			return Interval.newBuilder().
					setBegin(Timestamp.newBuilder().setTime(start)).
					setEnd(Timestamp.newBuilder().setTime(stop)).
					build();
		} else {
			return null;
		}
	}

	public static List<Interval> fromT4j(List<MomentInterval> i) {
		return i.stream().map(e -> fromT4j(e)).collect(Collectors.toList());
	}

	public static Interval findRemaining(Interval goal, List<Interval> blocks) {
		return fromT4j(findRemaining(fromRst(goal), fromRst(blocks)));
	}

	public static Interval findFirst(Interval goal, Interval range, List<Interval> blocks) {
		return fromT4j(findFirst(fromRst(goal), fromRst(range), fromRst(blocks)));
	}

	public static Interval findMax(Interval goal, Interval range, List<Interval> blocks) {
		return fromT4j(findMax(fromRst(goal), fromRst(range), fromRst(blocks)));
	}

	public static Interval findComplete(Interval goal, Interval range, List<Interval> blocks) {
		return fromT4j(findComplete(fromRst(goal), fromRst(range), fromRst(blocks)));
	}

	public static Interval includeNow(Interval goal) {
		return fromT4j(includeNow(fromRst(goal)));
	}

	public static MomentInterval findFirst(MomentInterval g, MomentInterval r, List<MomentInterval> b) {

		IntervalCollection<Moment> blocking = IntervalCollection.onMomentAxis().plus(b);
		IntervalCollection<Moment> goal = IntervalCollection.onMomentAxis().plus(g);
		IntervalCollection<Moment> free = IntervalCollection.onMomentAxis().plus(blocking.withComplement(r).getIntervals());
		IntervalCollection<Moment> overlaps = free.intersect(goal);

		if (!overlaps.isEmpty()) {
			return (MomentInterval) overlaps.getIntervals().stream().min(BY_BEGIN).get();
		}
		if (!free.isEmpty()) {
			return (MomentInterval) free.getIntervals().stream().min(BY_BEGIN).get();
		}
		return null;
	}

	public static MomentInterval findMax(MomentInterval g, MomentInterval r, List<MomentInterval> b) {

		IntervalCollection<Moment> blocking = IntervalCollection.onMomentAxis().plus(b);
		IntervalCollection<Moment> goal = IntervalCollection.onMomentAxis().plus(g);
		IntervalCollection<Moment> free = IntervalCollection.onMomentAxis().plus(blocking.withComplement(r).getIntervals());
		IntervalCollection<Moment> overlaps = free.intersect(goal);

		if (!overlaps.isEmpty()) {
			return (MomentInterval) overlaps.getIntervals().stream().max(BY_LENGTH).get();
		}
		if (!free.isEmpty()) {
			return (MomentInterval) free.getIntervals().stream().max(BY_LENGTH).get();
		}
		return null;
	}

	public static MomentInterval findRemaining(MomentInterval g, List<MomentInterval> b) {

		Moment now = CLOCK.currentTime();
		IntervalCollection<Moment> blocking = IntervalCollection.onMomentAxis().plus(b);

		for (ChronoInterval<Moment> m : blocking.getIntervals()) {
			if (m.contains(now)) {
				return null;
			}
		}

		IntervalCollection<Moment> goal = IntervalCollection.onMomentAxis().plus(g);
		IntervalCollection<Moment> free = IntervalCollection.onMomentAxis().plus(blocking.withComplement(g).getIntervals());
		IntervalCollection<Moment> overlaps = free.intersect(goal);

		if (!overlaps.isEmpty()) {
			return includeNow((MomentInterval) overlaps.getIntervals().stream().min(BY_BEGIN).get());
		}
		return null;
	}

	public static MomentInterval findComplete(MomentInterval g, MomentInterval r, List<MomentInterval> b) {

		IntervalCollection<Moment> blocking = IntervalCollection.onMomentAxis().plus(b);
		IntervalCollection<Moment> goal = IntervalCollection.onMomentAxis().plus(g);
		IntervalCollection<Moment> free = IntervalCollection.onMomentAxis().plus(blocking.withComplement(r).getIntervals());
		IntervalCollection<Moment> overlaps = free.intersect(goal);

		if (!overlaps.isEmpty()) {
			MomentInterval first = (MomentInterval) overlaps.getIntervals().get(0);
			if (first.equivalentTo(g)) {
				return first;
			}
		}
		return null;
	}

	public static long currentTimeInMicros() {
		return CLOCK.currentTimeInMicros();
	}

	public static MomentInterval includeNow(MomentInterval g) {
		Moment now = CLOCK.currentTime();
		if (g.contains(now)) {
			return g;
		} else {
			if (g.isAfter(now)) {
				return g.withStart(now);
			} else {
				return g.withEnd(now);
			}
		}
	}
}
