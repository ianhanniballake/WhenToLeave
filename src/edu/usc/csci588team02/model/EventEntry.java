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

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

/**
 * @author Yaniv Inbar
 */
public class EventEntry extends Entry
{
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
}