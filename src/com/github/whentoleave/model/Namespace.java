package com.github.whentoleave.model;

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
		DICTIONARY.set("", "http://www.w3.org/2005/Atom");
		DICTIONARY.set("batch", "http://schemas.google.com/gdata/batch");
		DICTIONARY.set("gAcl", "http://schemas.google.com/acl/2007");
		DICTIONARY.set("gCal", "http://schemas.google.com/gCal/2005");
		DICTIONARY.set("gd", "http://schemas.google.com/g/2005");
		DICTIONARY.set("georss", "http://www.georss.org/georss");
		DICTIONARY.set("gml", "http://www.opengis.net/gml");
		DICTIONARY.set("openSearch", "http://a9.com/-/spec/opensearch/1.1/");
		DICTIONARY.set("xml", "http://www.w3.org/XML/1998/namespace");
	}
}