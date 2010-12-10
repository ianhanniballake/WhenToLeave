package com.github.whentoleave.model;

import java.util.Map;

import com.google.api.client.xml.XmlNamespaceDictionary;

/**
 * XML Namespace Dictionary Storage<br />
 * <br />
 * 
 * Modified from <a href=
 * "http://code.google.com/p/google-api-java-client/source/browse/calendar-v2-atom-android-sample/src/com/google/api/client/sample/calendar/android/model/Namespace.java?repo=samples"
 * >the google-api-java-client sample</a> by Yaniv Inbar
 */
public class Namespace
{
	/**
	 * XML namespaces
	 */
	public static final XmlNamespaceDictionary DICTIONARY = new XmlNamespaceDictionary();
	static
	{
		final Map<String, String> map = DICTIONARY.namespaceAliasToUriMap;
		map.put("", "http://www.w3.org/2005/Atom");
		map.put("atom", "http://www.w3.org/2005/Atom");
		map.put("batch", "http://schemas.google.com/gdata/batch");
		map.put("gAcl", "http://schemas.google.com/acl/2007");
		map.put("gCal", "http://schemas.google.com/gCal/2005");
		map.put("gd", "http://schemas.google.com/g/2005");
		map.put("georss", "http://www.georss.org/georss");
		map.put("gml", "http://www.opengis.net/gml");
		map.put("openSearch", "http://a9.com/-/spec/opensearch/1.1/");
		map.put("xml", "http://www.w3.org/XML/1998/namespace");
	}
}