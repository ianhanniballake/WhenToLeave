package com.github.whentoleave.model;

import java.util.List;

import com.google.api.client.util.Key;

/**
 * Represents a link from one Google object to another<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-android-sample/src/com/google/api/client/sample/calendar/android/model/Link.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public class Link
{
	/**
	 * Searches through the given list of links for the given link name
	 * 
	 * @param links
	 *            links to search through
	 * @param rel
	 *            link to find
	 * @return the URL associated with the link or null if it wasn't found
	 */
	public static String find(final List<Link> links, final String rel)
	{
		if (links != null)
			for (final Link link : links)
				if (rel.equals(link.rel))
					return link.href;
		return null;
	}

	/**
	 * URL of the link
	 */
	@Key("@href")
	public String href;
	/**
	 * Name of the link
	 */
	@Key("@rel")
	public String rel;
}