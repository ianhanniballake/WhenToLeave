package com.github.whentoleave.model;

/**
 * Represents a Google Calendar Entry<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-android-sample/src/com/google/api/client/sample/calendar/android/model/CalendarEntry.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public class CalendarEntry extends Entry
{
	/**
	 * Clones this CalendarEntry
	 */
	@Override
	public CalendarEntry clone()
	{
		return (CalendarEntry) super.clone();
	}

	/**
	 * Gets a link to this calendar's event feed
	 * 
	 * @return the link to this calendar's event feed
	 */
	public String getEventFeedLink()
	{
		return Link
				.find(links, "http://schemas.google.com/gCal/2005#eventFeed");
	}
}