package edu.usc.csci588team02.model;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.xml.atom.GData;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

/**
 * Abstract superclass for any Google API Feed<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-android-sample/src/com/google/api/client/sample/calendar/android/model/Entry.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public abstract class Feed
{
	/**
	 * Builds and executes the HTTP request to get the Google Feed
	 * 
	 * @param transport
	 *            the authorized HttpTransport to use
	 * @param url
	 *            URL of the EventFeed
	 * @param feedClass
	 *            the class of the Feed that is being requested
	 * @return the Feed associated with the given URL
	 * @throws IOException
	 *             on IO error
	 */
	static Feed executeGet(final HttpTransport transport, final GoogleUrl url,
			final Class<? extends Feed> feedClass) throws IOException
	{
		url.fields = GData.getFieldsFor(feedClass);
		final HttpRequest request = transport.buildGetRequest();
		request.url = url;
		return RedirectHandler.execute(request).parseAs(feedClass);
	}

	/**
	 * List of associated links (self, edit, etc)
	 */
	@Key("link")
	public List<Link> links;

	/**
	 * Gets all of the entries associated with this Feed
	 * 
	 * @return the list of entries associated with this Feed
	 */
	public abstract List<? extends Entry> getEntries();
}