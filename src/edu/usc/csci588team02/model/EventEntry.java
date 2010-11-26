/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.usc.csci588team02.model;

import java.io.IOException;
import java.util.Date;

import android.location.Location;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

import edu.usc.csci588team02.maps.RouteInformation;
import edu.usc.csci588team02.maps.RouteInformation.TravelType;

/**
 * @author Yaniv Inbar
 */
public class EventEntry extends Entry
{
	public static EventEntry executeGet(final HttpTransport transport,
			final EventUrl url) throws IOException
	{
		return (EventEntry) Entry.executeGet(transport, url, EventEntry.class);
	}

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

	@Key("gd:when")
	public When when;
	@Key("gd:where")
	public Where where;

	@Override
	public EventEntry clone()
	{
		return (EventEntry) super.clone();
	}

	@Override
	public EventEntry executeInsert(final HttpTransport transport,
			final CalendarUrl url) throws IOException
	{
		return (EventEntry) super.executeInsert(transport, url);
	}

	public long getWhenToLeaveInMinutes(final Location location,
			final TravelType travelType)
	{
		final String locationString = location.getLatitude() + ","
				+ location.getLongitude();
		final int minutesToEvent = RouteInformation.getDuration(locationString,
				where.valueString, travelType);
		final long minutesUntilEvent = (when.startTime.value - new Date()
				.getTime()) / 60000;
		return minutesUntilEvent - minutesToEvent;
	}
}