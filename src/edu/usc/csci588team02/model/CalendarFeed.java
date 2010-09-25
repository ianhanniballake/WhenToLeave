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
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.googleapis.xml.atom.GData;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

/**
 * @author Yaniv Inbar
 */
public class CalendarFeed
{
	public static CalendarFeed executeGet(final HttpTransport transport,
			final CalendarUrl url) throws IOException
	{
		url.fields = GData.getFieldsFor(CalendarFeed.class);
		final HttpRequest request = transport.buildGetRequest();
		request.url = url;
		return RedirectHandler.execute(request).parseAs(CalendarFeed.class);
	}

	@Key("entry")
	public List<CalendarEntry> calendars = new ArrayList<CalendarEntry>();
	@Key("link")
	public List<Link> links;

	public String getBatchLink()
	{
		return Link.find(links, "http://schemas.google.com/g/2005#batch");
	}

	public String getNextLink()
	{
		return Link.find(links, "next");
	}
}