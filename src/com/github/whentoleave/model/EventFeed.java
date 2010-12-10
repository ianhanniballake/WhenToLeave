package com.github.whentoleave.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

/**
 * Represents a Google Calendar Event Feed<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-oauth-sample/src/com/google/api/client/sample/calendar/v2/model/EventFeed.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public class EventFeed extends Feed
{
	/**
	 * Builds and executes the HTTP request to get the Google Calendar Event
	 * feed
	 * 
	 * @param transport
	 *            the authorized HttpTransport to use
	 * @param url
	 *            URL of the EventFeed
	 * @return the EventFeed associated with the given URL
	 * @throws IOException
	 *             on IO error
	 */
	public static EventFeed executeGet(final HttpTransport transport,
			final GoogleUrl url) throws IOException
	{
		return (EventFeed) Feed.executeGet(transport, url, EventFeed.class);
	}

	/**
	 * List of events, auto filled from 'entry' field
	 */
	@Key("entry")
	public List<EventEntry> events = new ArrayList<EventEntry>();

	@Override
	public List<EventEntry> getEntries()
	{
		return events;
	}
}