package edu.usc.csci588team02.manager;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.api.client.util.DateTime;

import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.model.CalendarUrl;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.model.EventEntryComparator;
import edu.usc.csci588team02.model.EventFeed;

public class EventManager extends Manager
{
	public Set<EventEntry> getEvents(final Date start, final Date end)
			throws IOException
	{
		final TreeSet<EventEntry> events = new TreeSet<EventEntry>(
				new EventEntryComparator());
		final List<CalendarEntry> calendars = new CalendarManager(
				getTransport()).getCalendars();
		for (final CalendarEntry calendar : calendars)
		{
			final CalendarUrl eventFeedUrl = new CalendarUrl(
					calendar.getEventFeedLink() + "?start-min="
							+ new DateTime(start) + "&start-max="
							+ new DateTime(end) + "&orderby=starttime"
							+ "&singleevents=true");
			final EventFeed eventFeed = EventFeed.executeGet(getTransport(),
					eventFeedUrl);
			events.addAll(eventFeed.getEntries());
		}
		return events;
	}

	public Set<EventEntry> getEventsStartingNow(final Date end)
			throws IOException
	{
		return getEvents(new Date(), end);
	}
}
