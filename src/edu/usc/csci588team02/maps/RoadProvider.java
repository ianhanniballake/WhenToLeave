package edu.usc.csci588team02.maps;

//modified from http://code.google.com/p/j2memaprouteprovider/source/browse/#svn/trunk/J2MEMapRouteAndroidEx
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class KMLHandler extends DefaultHandler
{
	boolean isItemIcon;
	boolean isPlacemark;
	boolean isRoute;
	private final Stack<String> mCurrentElement = new Stack<String>();
	Road mRoad;
	private String mString;

	public KMLHandler()
	{
		mRoad = new Road();
	}

	public Point[] addPoint(final Point[] points)
	{
		final Point[] result = new Point[points.length + 1];
		for (int i = 0; i < points.length; i++)
			result[i] = points[i];
		result[points.length] = new Point();
		return result;
	}

	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException
	{
		final String chars = new String(ch, start, length).trim();
		mString = mString.concat(chars);
	}

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
		if (mString.length() > 0)
			if (localName.equalsIgnoreCase("name"))
			{
				if (isPlacemark)
				{
					isRoute = mString.equalsIgnoreCase("Route");
					if (!isRoute)
						mRoad.mPoints[mRoad.mPoints.length - 1].mName = mString;
				}
				else
					mRoad.mName = mString;
			}
			else if (localName.equalsIgnoreCase("color") && !isPlacemark)
				mRoad.mColor = Integer.parseInt(mString, 16);
			else if (localName.equalsIgnoreCase("width") && !isPlacemark)
				mRoad.mWidth = Integer.parseInt(mString);
			else if (localName.equalsIgnoreCase("description"))
			{
				if (isPlacemark)
				{
					final String description = cleanup(mString);
					if (!isRoute)
						mRoad.mPoints[mRoad.mPoints.length - 1].mDescription = description;
					else
						mRoad.mDescription = description;
				}
			}
			else if (localName.equalsIgnoreCase("href"))
			{
				if (isItemIcon)
					mRoad.mPoints[mRoad.mPoints.length - 1].mIconUrl = mString;
			}
			else if (localName.equalsIgnoreCase("coordinates"))
				if (isPlacemark)
					if (!isRoute)
					{
						final String[] xyParsed = mString.split(",");
						final double lon = Double.parseDouble(xyParsed[0]);
						final double lat = Double.parseDouble(xyParsed[1]);
						mRoad.mPoints[mRoad.mPoints.length - 1].mLatitude = lat;
						mRoad.mPoints[mRoad.mPoints.length - 1].mLongitude = lon;
					}
					else
					{
						final String[] coodrinatesParsed = mString.split(" ");
						mRoad.mRoute = new double[coodrinatesParsed.length][2];
						for (int i = 0; i < coodrinatesParsed.length; i++)
						{
							final String[] xyParsed = coodrinatesParsed[i]
									.split(",");
							for (int j = 0; j < 2 && j < xyParsed.length; j++)
								mRoad.mRoute[i][j] = Double
										.parseDouble(xyParsed[j]);
						}
					}
		mCurrentElement.pop();
		if (localName.equalsIgnoreCase("Placemark"))
		{
			isPlacemark = false;
			if (isRoute)
				isRoute = false;
		}
		else if (localName.equalsIgnoreCase("ItemIcon"))
			if (isItemIcon)
				isItemIcon = false;
	}

	@Override
	public void startElement(final String uri, final String localName,
			final String name, final Attributes attributes) throws SAXException
	{
		mCurrentElement.push(localName);
		if (localName.equalsIgnoreCase("Placemark"))
		{
			isPlacemark = true;
			mRoad.mPoints = addPoint(mRoad.mPoints);
		}
		else if (localName.equalsIgnoreCase("ItemIcon"))
			if (isPlacemark)
				isItemIcon = true;
		mString = new String();
	}
}

public class RoadProvider
{
	public static Road getRoute(final InputStream is)
	{
		final KMLHandler handler = new KMLHandler();
		try
		{
			final SAXParser parser = SAXParserFactory.newInstance()
					.newSAXParser();
			parser.parse(is, handler);
		} catch (final ParserConfigurationException e)
		{
			e.printStackTrace();
		} catch (final SAXException e)
		{
			e.printStackTrace();
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
		return handler.mRoad;
	}

	public static String getUrlFromLatLong(final double fromLat,
			final double fromLon, final double toLat, final double toLon)
	{// connect to map web service
		final StringBuffer urlString = new StringBuffer();
		urlString.append("http://maps.google.com/maps?f=d&hl=en");
		urlString.append("&saddr=");// from
		urlString.append(Double.toString(fromLat));
		urlString.append(",");
		urlString.append(Double.toString(fromLon));
		urlString.append("&daddr=");// to
		urlString.append(Double.toString(toLat));
		urlString.append(",");
		urlString.append(Double.toString(toLon));
		urlString.append("&ie=UTF8&0&om=0&output=kml");
		return urlString.toString();
	}

	public static String getUrlFromString(final String from, final String to)
	{// connect to map web service
		final StringBuffer urlString = new StringBuffer();
		urlString.append("http://maps.google.com/maps?f=d&hl=en");
		urlString.append("&saddr=");// from
		urlString.append(from);
		urlString.append("&daddr=");// to
		urlString.append(to);
		urlString.append("&ie=UTF8&0&om=0&output=kml");
		return urlString.toString();
	}
}
