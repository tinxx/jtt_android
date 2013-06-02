package com.aragaer.jtt;

import java.util.Collections;
import java.util.LinkedList;
import java.util.TimeZone;

import com.aragaer.jtt.core.Calculator;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/* A single item in TodayList */
abstract class TodayItem {
	public final long time;
	abstract public View toView(Context c);
	public TodayItem(long t) {
		time = t;
	}
}

/* Hour item in TodayList */
class HourItem extends TodayItem {
	public static int current;
	public static long next_transition;
	public final int hnum;

	public HourItem(long t, int h) {
		super(t);
		hnum = h % 12;
	}

	static String[] extras = null;

	public View toView(Context c) {
		if (extras == null) {
			extras = new String[] { c.getString(R.string.sunset), "", "",
					c.getString(R.string.midnight), "", "",
					c.getString(R.string.sunrise), "", "",
					c.getString(R.string.midday), "", "" };

		}
		View v = View.inflate(c, R.layout.today_item, null);

		/* no need to check for previous transition */
		boolean is_current = hnum == current
				&& time < next_transition;

		final StringResources sr = RuntimeResources.get(c).getInstance(StringResources.class);

		JTTUtil.t(v, R.id.time, sr.format_time(time));
		JTTUtil.t(v, R.id.glyph, JTTHour.Glyphs[hnum]);
		JTTUtil.t(v, R.id.name, sr.getHrOf(hnum));
		JTTUtil.t(v, R.id.extra, extras[hnum]);
		JTTUtil.t(v, R.id.curr, is_current ? "▶" : "");

		return v;
	}
}

/* "DayName" item in Today List */
class DayItem extends TodayItem {
	public DayItem(long t) {
		super(t);
	}

	public View toView(Context c) {
		View v = View.inflate(c, android.R.layout.preference_category, null);
		JTTUtil.t(v, android.R.id.title, dateToString(time, c));

		return v;
	}

	/* returns strings like "today" or "2 days ago" etc */
	static String[] daynames = null;
	private String dateToString(long date, Context c) {
		if (daynames == null)
			daynames = new String[] { c.getString(R.string.day_next),
				c.getString(R.string.day_curr),
				c.getString(R.string.day_prev) };

		final long now = System.currentTimeMillis();
		final int ddiff = (int) (ms_to_day(now) - ms_to_day(date));
		if (ddiff < 2 && ddiff > -2)
			return daynames[ddiff+1];
		return c.getResources().getQuantityString(ddiff > 0
				? R.plurals.days_past
				: R.plurals.days_future, ddiff, ddiff);
	}

	/* helper function
	 * converts ms timestamp to day number
	 * not useful by itself but can be used to find difference between days
	 */
	private static final long ms_to_day(long t) {
		t += TodayAdapter.tz.getOffset(t);
		return t / Calculator.ms_per_day;
	}
}

public class TodayAdapter extends ArrayAdapter<TodayItem> implements
		StringResources.StringResourceChangeListener {
	private static final String TAG = "TODAY";
	private boolean is_day;
	private final long transitions[] = new long[4];
	static TimeZone tz = TimeZone.getDefault();

	public TodayAdapter(Context c, int layout_id) {
		super(c, layout_id);
		RuntimeResources.get(c).getInstance(StringResources.class)
				.registerStringResourceChangeListener(this,
						StringResources.TYPE_HOUR_NAME | StringResources.TYPE_TIME_FORMAT);
		HourItem.extras = DayItem.daynames = null;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		return getItem(position).toView(parent.getContext());
	}

	/* helper function
	 * accepts a time stamp
	 * returns a time stamp for the same time but next day
	 *
	 * simply adding 24 hours does not always work
	 */
	private static final long add24h(long t) {
		t += tz.getOffset(t);
		t += Calculator.ms_per_day;
		return t - tz.getOffset(t);
	}

	/* takes a sublist of hours
	 * creates a list to display by adding day names
	 */
	void buildItems() {
		clear();

		/* time stamp for 00:00:00 */
		long start_of_day = transitions[0];

		/* "aligning" code */
		start_of_day += tz.getOffset(start_of_day);
		start_of_day -= start_of_day % Calculator.ms_per_day;
		start_of_day -= tz.getOffset(start_of_day);

		add(new DayItem(start_of_day)); // List should start with one
		start_of_day = add24h(start_of_day);

		int h_add = is_day ? 0 : 6;

		/* start with first transition */
		add(new HourItem(transitions[0], h_add));
		for (int i = 1; i < transitions.length; i++) {
			long start = transitions[i - 1];
			long diff = transitions[i] - start;
			for (int j = 1; j <= 6; j++) {
				long t = start + j * diff / 6;
				if (t >= start_of_day) {
					add(new DayItem(start_of_day));
					start_of_day = add24h(start_of_day);
				}
				add(new HourItem(t, h_add + j));
			}
			h_add = 6 - h_add;
		}
	}

	public void setCurrent(int cur) {
		long now = System.currentTimeMillis();
		HourItem.current = cur;

		/* Proper code to update timezone when it changed is quite complex
		 * The event itself is pretty rare
		 * Even if it breaks, restarting the app will fix that
		 */
		tz = TimeZone.getDefault();

		if (now >= transitions[1] && now < transitions[2]) {
			notifyDataSetChanged();
			return;
		}

		final long tr[] = new long[2];
		is_day = Calculator.getSurroundingTransitions(getContext(), now, tr);
		transitions[1] = tr[0];
		transitions[2] = tr[1];
		Calculator.getSurroundingTransitions(getContext(), transitions[1] - 1, tr);
		transitions[0] = tr[0];
		Calculator.getSurroundingTransitions(getContext(), transitions[2] + 1, tr);
		transitions[3] = tr[1];

		HourItem.next_transition = transitions[2];

		buildItems();
	}


	@Override
	public boolean isEnabled(int pos) {
		return false;
	}

	public void onStringResourcesChanged(int changes) {
		notifyDataSetChanged();
	}
}
