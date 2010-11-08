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
import java.util.List;

import com.google.api.client.googleapis.xml.atom.AtomPatchRelativeToOriginalContent;
import com.google.api.client.googleapis.xml.atom.GData;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DataUtil;
import com.google.api.client.util.Key;
import com.google.api.client.xml.atom.AtomContent;

/**
 * @author Yaniv Inbar
 */
public class Entry implements Cloneable
{
	static Entry executeGet(final HttpTransport transport, final EventUrl url,
			final Class<? extends Entry> entryClass) throws IOException
	{
		url.fields = GData.getFieldsFor(entryClass);
		final HttpRequest request = transport.buildGetRequest();
		request.url = url;
		return RedirectHandler.execute(request).parseAs(entryClass);
	}

	@Key("link")
	public List<Link> links;
	@Key
	public String summary;
	@Key
	public String title;
	@Key
	public String updated;

	@Override
	protected Entry clone()
	{
		return DataUtil.clone(this);
	}

	public void executeDelete(final HttpTransport transport) throws IOException
	{
		final HttpRequest request = transport.buildDeleteRequest();
		request.setUrl(getEditLink());
		RedirectHandler.execute(request).ignore();
	}

	Entry executeInsert(final HttpTransport transport, final CalendarUrl url)
			throws IOException
	{
		final HttpRequest request = transport.buildPostRequest();
		request.url = url;
		final AtomContent content = new AtomContent();
		content.namespaceDictionary = Namespace.DICTIONARY;
		content.entry = this;
		request.content = content;
		return RedirectHandler.execute(request).parseAs(getClass());
	}

	Entry executePatchRelativeToOriginal(final HttpTransport transport,
			final Entry original) throws IOException
	{
		final HttpRequest request = transport.buildPatchRequest();
		request.setUrl(getEditLink());
		final AtomPatchRelativeToOriginalContent content = new AtomPatchRelativeToOriginalContent();
		content.namespaceDictionary = Namespace.DICTIONARY;
		content.originalEntry = original;
		content.patchedEntry = this;
		request.content = content;
		return RedirectHandler.execute(request).parseAs(getClass());
	}

	private String getEditLink()
	{
		return Link.find(links, "edit");
	}

	public String getSelfLink()
	{
		return Link.find(links, "self");
	}
}