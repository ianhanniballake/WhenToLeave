package com.github.whentoleave.model;

import java.io.IOException;
import java.util.Date;

import android.location.Location;

import com.github.whentoleave.maps.RouteInformation;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

/**
 * Represents a Google Calendar Event Entry<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-oauth-sample/src/com/google/api/client/sample/calendar/v2/model/EventEntry.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public class EventEntry extends Entry
{
	/**
	 * Builds and executes the HTTP request to get an EventEntry
	 * 
	 * @param transport
	 *            the authorized HttpTransport to use
	 * @param url
	 *            URL of the EventEntry
	 * @return the EventEntry associated with the given URL
	 * @throws IOException
	 *             on IO error
	 */
	public static EventEntry executeGet(final HttpTransport transport,
			final GoogleUrl url) throws IOException
	{
		return (EventEntry) Entry.executeGet(transport, url, EventEntry.class);
	}

	/**
	 * Formats the given number of minutes into usable String. For <60 minutes,
	 * returns "MMm", with no leading 0 (i.e., 6m or 15m). For >=60 minutes,
	 * returns "HH:MMh" with no leading hour 0 (i.e., 1:04h or 11:15h)
	 * 
	 * @param leaveInMinutes
	 *            the number of minutes to be formatted
	 * @return a formatted string representing the given leaveInMinutes in "MMm"
	 *         (<60) or "HH:MMh" (>=60)
	 */
	public static String formatWhenToLeave(final long leaveInMinutes)
	{
		final long hoursToGo = Math.abs(leaveInMinutes) / 60;
		final long minutesToGo = Math.abs(leaveInMinutes) % 60;
		final StringBuffer formattedTime = new StringBuffer();
		if (hoursToGo > 0)
		{
			formattedTime.append(hoursToGo);
			formattedTime.append(":");
			if (minutesToGo < 10)
				formattedTime.append("0");
			formattedTime.append(minutesToGo);
			formattedTime.append("h");
		}
		else
		{
			formattedTime.append(minutesToGo);
			formattedTime.append("m");
		}
		return formattedTime.toString();
	}

	/**
	 * Represents the time of the event
	 * 
	 * @see When
	 */
	@Key("gd:when")
	public When when;
	/**
	 * Represents the location of the event
	 * 
	 * @see Where
	 */
	@Key("gd:where")
	public Where where;

	/**
	 * Clones this EventEntry
	 */
	@Override
	public EventEntry clone()
	{
		return (EventEntry) super.clone();
	}

	/**
	 * Queries Google Maps for the travel time from the given location to this
	 * EventEntry's location and returns how much time is remaining before one
	 * would need to leave
	 * 
	 * @param location
	 *            where to navigate from
	 * @param travelType
	 *            what mode of transportation to use
	 * @return how much time is remaining before one would need to leave
	 */
	public long getWhenToLeaveInMinutes(final Location location,
			final String travelType)
	{
		final int minutesToEvent = RouteInformation.getDuration(location,
				where.valueString, travelType);
		final long minutesUntilEvent = (when.startTime.value - new Date()
				.getTime()) / 60000;
		return minutesUntilEvent - minutesToEvent;
	}
}