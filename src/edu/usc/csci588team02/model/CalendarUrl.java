package edu.usc.csci588team02.model;

import com.google.api.client.googleapis.GoogleUrl;

/**
 * Represents the URL of a Google Calendar<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-android-sample/src/com/google/api/client/sample/calendar/android/model/CalendarUrl.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public class CalendarUrl extends GoogleUrl
{
	/**
	 * The root URL of all Google Calendars
	 */
	public static final String ROOT_URL = "https://www.google.com/calendar/feeds";

	/**
	 * Gets the URL representing all of the user's calendars
	 * 
	 * @return the URL representing all of the user's calendars
	 */
	public static CalendarUrl forAllCalendarsFeed()
	{
		final CalendarUrl result = new CalendarUrl(ROOT_URL);
		result.pathParts.add("default");
		result.pathParts.add("allcalendars");
		result.pathParts.add("full");
		return result;
	}

	/**
	 * Constructor for a CalendarURL for a given URL
	 * 
	 * @param url
	 *            the URL to use
	 */
	public CalendarUrl(final String url)
	{
		super(url);
	}
}