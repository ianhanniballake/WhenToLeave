package edu.usc.csci588team02.manager;

import java.io.IOException;
import java.util.Calendar;
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
import edu.usc.csci588team02.model.EventUrl;

public class EventManager extends Manager
{
	public EventEntry getEvent(final String eventUrl) throws IOException
	{
		final EventEntry event = EventEntry.executeGet(getTransport(),
				new EventUrl(eventUrl));
		return event;
	}

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

	/**
	 * Finds the next event (chronologically) that has a location. Searches in
	 * an exponentially larger date range until it finds an event (first 1 day,
	 * then 2, then 4, etc)
	 * 
	 * @return the next event that has a location, null if no events with a
	 *         location are found
	 * @throws IOException
	 *             on error
	 */
	public EventEntry getNextEventWithLocation() throws IOException
	{
		Calendar queryFrom = Calendar.getInstance();
		final Calendar queryTo = Calendar.getInstance();
		queryTo.add(Calendar.DATE, 1);
		int daysToAdd = 2;
		while (daysToAdd < 2048)
		{
			final Set<EventEntry> events = getEvents(queryFrom.getTime(),
					queryTo.getTime());
			for (final EventEntry event : events)
				if (event.where != null && event.where.valueString != null)
					return event;
			queryFrom = queryTo;
			queryTo.add(Calendar.DATE, daysToAdd);
			daysToAdd *= 2;
		}
		return null;
	}
}
