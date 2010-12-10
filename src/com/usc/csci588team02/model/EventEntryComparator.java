package edu.usc.csci588team02.model;

import java.util.Comparator;

/**
 * Comparator to compare EventEntry's first based on start time, breaking ties
 * by title
 */
public class EventEntryComparator implements Comparator<EventEntry>
{
	/**
	 * Compares two EventEntry's, first by start time (when.startTime.value),
	 * breaking ties by title if they have the exact same time or if both have
	 * no start time
	 */
	@Override
	public int compare(final EventEntry event1, final EventEntry event2)
	{
		final boolean event1NullWhen = event1.when == null
				|| event1.when.startTime == null;
		final boolean event2NullWhen = event2.when == null
				|| event2.when.startTime == null;
		if (event1NullWhen && !event2NullWhen)
			return 1;
		else if (!event1NullWhen && event2NullWhen)
			return -1;
		else if (!event1NullWhen && !event2NullWhen)
		{
			final long timeDiff = event1.when.startTime.value
					- event2.when.startTime.value;
			if (timeDiff != 0)
				return timeDiff < 0 ? -1 : 1;
		}
		// Either both are null or they have the same time.
		// In those cases, compare by title
		return event1.title.compareTo(event2.title);
	}
}
