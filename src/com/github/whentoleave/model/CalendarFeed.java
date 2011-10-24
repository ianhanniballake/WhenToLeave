package com.github.whentoleave.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.util.Key;

/**
 * Represents a Google Calendar Feed<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-android-sample/src/com/google/api/client/sample/calendar/android/model/CalendarFeed.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public class CalendarFeed extends Feed
{
	/**
	 * Builds and executes the HTTP request to get the Google Calendar feed
	 * 
	 * @param factory
	 *            the authorized HttpRequestFactory to use
	 * @param url
	 *            URL of the Calendar
	 * @return the CalendarFeed associated with the given URL
	 * @throws IOException
	 *             on IO error
	 */
	public static CalendarFeed executeGet(final HttpRequestFactory factory,
			final CalendarUrl url) throws IOException
	{
		return (CalendarFeed) Feed.executeGet(factory, url, CalendarFeed.class);
	}

	/**
	 * List of calendars, auto filled from 'entry' field
	 */
	@Key("entry")
	public List<CalendarEntry> calendars = new ArrayList<CalendarEntry>();

	@Override
	public List<CalendarEntry> getEntries()
	{
		return calendars;
	}

	/**
	 * Gets the URL of the next Calendar in the feed
	 * 
	 * @return the URL of the next Calendar in the feed
	 */
	public String getNextLink()
	{
		return Link.find(links, "next");
	}
}