package edu.usc.csci588team02.maps;

//modified from http://code.google.com/p/j2memaprouteprovider/source/browse/#svn/trunk/J2MEMapRouteAndroidEx
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;

/**
 * Handles the parsing of a KML file representing a route
 */
class KMLHandler extends DefaultHandler
{
	/**
	 * Whether we are currently in a 'Placemark' tag
	 */
	private boolean isPlacemark;
	/**
	 * Whether we are currently looking at a Route
	 */
	private boolean isRoute;
	/**
	 * The content of the current element
	 */
	private String mElementContent;
	/**
	 * Road which serves as the output of this Handler
	 */
	final Road mRoad = new Road();

	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException
	{
		final String chars = new String(ch, start, length).trim();
		mElementContent = mElementContent.concat(chars);
	}

	/**
	 * Formats and cleans up a description, removing select HTML elements.
	 * 
	 * @param value
	 *            string to clean up
	 * @return cleaned up string
	 */
	private String cleanup(final String value)
	{
		String newValue = value;
		String remove = "<br/>";
		int index = newValue.indexOf(remove);
		if (index != -1)
			newValue = newValue.substring(0, index);
		remove = "&#160;";
		index = newValue.indexOf(remove);
		final int len = remove.length();
		while (index != -1)
		{
			newValue = newValue.substring(0, index).concat(
					newValue.substring(index + len, newValue.length()));
			index = newValue.indexOf(remove);
		}
		return newValue;
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String name) throws SAXException
	{
		if (mElementContent.length() > 0)
			if (localName.equalsIgnoreCase("name"))
			{
				if (isPlacemark)
					isRoute = mElementContent.equalsIgnoreCase("Route");
				else
					mRoad.mName = mElementContent;
			}
			else if (localName.equalsIgnoreCase("description") && isPlacemark
					&& isRoute)
				mRoad.mDescription = cleanup(mElementContent);
			else if (localName.equalsIgnoreCase("coordinates") && isPlacemark
					&& isRoute)
			{
				final String[] coordinatesParsed = mElementContent.split(" ");
				for (final String coordinatePair : coordinatesParsed)
				{
					final String[] xyParsed = coordinatePair.split(",");
					// Convert the lat and long to Microseconds format, used by
					// GeoPoint
					final int lonE6 = (int) (Double.parseDouble(xyParsed[0]) * 1e6);
					final int latE6 = (int) (Double.parseDouble(xyParsed[1]) * 1e6);
					mRoad.mRoute.add(new GeoPoint(latE6, lonE6));
				}
			}
		if (localName.equalsIgnoreCase("Placemark"))
		{
			isPlacemark = false;
			isRoute = false;
		}
	}

	@Override
	public void startElement(final String uri, final String localName,
			final String name, final Attributes attributes) throws SAXException
	{
		if (localName.equalsIgnoreCase("Placemark"))
			isPlacemark = true;
		mElementContent = new String();
	}
}

/**
 * Provides the mechanism to get directions from our current location to a
 * destination
 */
public class RoadProvider
{
	/**
	 * Logging tag
	 */
	private static final String TAG = "RoadProvider";

	/**
	 * Finds a route from the start location to the destination
	 * 
	 * @param start
	 *            location to start from
	 * @param destination
	 *            destination address
	 * @return the route or null if no route was found
	 */
	public static Road getRoute(final Location start, final String destination)
	{
		final StringBuffer urlString = new StringBuffer();
		urlString.append("http://maps.google.com/maps?f=d&hl=en");
		urlString.append("&saddr=");// from
		urlString.append(Double.toString(start.getLatitude()));
		urlString.append(",");
		urlString.append(Double.toString(start.getLongitude()));
		urlString.append("&daddr=");// to
		urlString.append(RouteInformation.formatAddress(destination));
		urlString.append("&ie=UTF8&0&om=0&output=kml");
		final String url = urlString.toString();
		Log.v(TAG, "URL: " + url);
		InputStream is = null;
		try
		{
			final URLConnection conn = new URL(url).openConnection();
			is = conn.getInputStream();
		} catch (final MalformedURLException e)
		{
			Log.e(TAG, "getConnection: Invalid URL", e);
			return null;
		} catch (final IOException e)
		{
			Log.e(TAG, "getConnection: IO Error", e);
			return null;
		}
		final KMLHandler handler = new KMLHandler();
		try
		{
			final SAXParser parser = SAXParserFactory.newInstance()
					.newSAXParser();
			parser.parse(is, handler);
		} catch (final ParserConfigurationException e)
		{
			Log.e(TAG, "SAX Configuration Error", e);
			return null;
		} catch (final SAXException e)
		{
			Log.e(TAG, "SAX Error", e);
			return null;
		} catch (final IOException e)
		{
			Log.e(TAG, "IO Error", e);
			return null;
		} finally
		{
			try
			{
				is.close();
			} catch (final IOException e)
			{
				Log.w(TAG, "Error closing InputStream", e);
			}
		}
		return handler.mRoad;
	}
}
