package edu.usc.csci588team02.model;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

/**
 * Represents when an Event occurs, including both start and end time
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-oauth-sample/src/com/google/api/client/sample/calendar/v2/model/When.java?repo=samples"
 * /> by Yaniv Inbar
 */
public class When
{
	/**
	 * The end time of the event
	 */
	@Key("@endTime")
	public DateTime endTime;
	/**
	 * The start time of the event
	 */
	@Key("@startTime")
	public DateTime startTime;
}