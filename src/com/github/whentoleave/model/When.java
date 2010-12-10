package com.github.whentoleave.model;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

/**
 * Represents when an Event occurs, including both start and end time<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-oauth-sample/src/com/google/api/client/sample/calendar/v2/model/When.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
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