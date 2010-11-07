package edu.usc.csci588team02.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpTransport;

import edu.usc.csci588team02.model.CalendarEntry;
import edu.usc.csci588team02.model.CalendarFeed;
import edu.usc.csci588team02.model.CalendarUrl;

public class CalendarManager extends Manager
{
	public CalendarManager()
	{
		super();
	}

	public CalendarManager(final HttpTransport transport)
	{
		super(transport);
	}

	public List<CalendarEntry> getCalendars() throws IOException
	{
		final ArrayList<CalendarEntry> calendars = new ArrayList<CalendarEntry>();
		final CalendarUrl calFeedUrl = CalendarUrl.forAllCalendarsFeed();
		// page through results
		while (true)
		{
			final CalendarFeed feed = CalendarFeed.executeGet(getTransport(),
					calFeedUrl);
			if (feed.calendars != null)
				calendars.addAll(feed.calendars);
			final String nextLink = feed.getNextLink();
			if (nextLink == null)
				break;
		}
		return calendars;
	}
}
