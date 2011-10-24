package com.github.whentoleave.model;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.xml.atom.GoogleAtom;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.util.Data;
import com.google.api.client.util.Key;

/**
 * Abstract superclass for any Google API entry<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-android-sample/src/com/google/api/client/sample/calendar/android/model/Entry.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public abstract class Entry implements Cloneable
{
	/**
	 * Builds and executes the HTTP request to get an Entry
	 * 
	 * @param factory
	 *            the authorized HttpRequestFactory to use
	 * @param url
	 *            URL of the Entry
	 * @param entryClass
	 *            the class of the Entry that is being requested
	 * @return the Entry associated with the given URL
	 * @throws IOException
	 *             on IO error
	 */
	protected static Entry executeGet(final HttpRequestFactory factory,
			final GoogleUrl url, final Class<? extends Entry> entryClass)
			throws IOException
	{
		url.fields = GoogleAtom.getFieldsFor(entryClass);
		final HttpRequest request = factory.buildDeleteRequest(url);
		return request.execute().parseAs(entryClass);
	}

	/**
	 * The content of the Entry (long description, etc)
	 */
	@Key
	public String content;
	/**
	 * List of associated links (self, edit, etc)
	 */
	@Key("link")
	public List<Link> links;
	/**
	 * Title of the Entry
	 */
	@Key
	public String title;

	/**
	 * Clones this Entry
	 */
	@Override
	protected Entry clone()
	{
		return Data.clone(this);
	}

	/**
	 * Gets the unique URL associated with this Entry, which can be used to
	 * re-query for this Entry if needed
	 * 
	 * @return the unique URL associated with this Entry
	 */
	public String getSelfLink()
	{
		return Link.find(links, "self");
	}
}