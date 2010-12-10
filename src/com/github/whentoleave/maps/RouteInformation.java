package com.github.whentoleave.maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;

/**
 * Helper class providing route and address information
 */
public class RouteInformation
{
	/**
	 * Logging tag
	 */
	private static final String TAG = "RouteInformation";

	/**
	 * Formats the given address, replacing whitespace with plus signs, allowing
	 * the address to be used in web queries and external intent launches (such
	 * as Maps or Navigation)
	 * 
	 * @param address
	 *            address to format
	 * @return formatted address
	 */
	public static String formatAddress(final String address)
	{
		String formattedAddress = address;
		// remove spaces
		while (formattedAddress.indexOf("  ") != -1)
			formattedAddress = formattedAddress.replace("  ", " ");
		formattedAddress = formattedAddress.replace(" ", "+");
		return formattedAddress;
	}

	/**
	 * Gets a URLConnection to the given url. Note that it is up to the caller
	 * to close the stream!
	 * 
	 * @param url
	 *            URL to connect to
	 * @return An open input stream to the url
	 */
	private static InputStream getConnection(final String url)
	{
		InputStream is = null;
		try
		{
			final URLConnection conn = new URL(url).openConnection();
			is = conn.getInputStream();
		} catch (final MalformedURLException e)
		{
			Log.e(TAG, "getConnection: Invalid URL", e);
		} catch (final IOException e)
		{
			Log.e(TAG, "getConnection: IO Error", e);
		}
		return is;
	}

	/**
	 * Gets the travel time from the current location to the destination address
	 * with a given travel type
	 * 
	 * @param location
	 *            current starting location
	 * @param destination
	 *            destination address
	 * @param travelType
	 *            transportation mode to use
	 * @return travel time to the destination in minutes
	 */
	public static int getDuration(final Location location,
			final String destination, final String travelType)
	{
		InputStream is = null;
		int durationSec = 0;
		try
		{
			final String locationString = location.getLatitude() + ","
					+ location.getLongitude();
			final String from = formatAddress(locationString);
			final String to = formatAddress(destination);
			final StringBuffer urlString = new StringBuffer();
			urlString
					.append("http://maps.googleapis.com/maps/api/directions/json");
			urlString.append("?origin=");// from
			urlString.append(from);
			urlString.append("&destination=");// to
			urlString.append(to);
			urlString.append("&sensor=true&mode=");
			urlString.append(travelType);
			final String url = urlString.toString();
			Log.v(TAG, "getDuration URL: " + url);
			is = getConnection(url);
			if (is == null)
				return 0;
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					is));
			final StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null)
				sb.append(line + "\n");
			br.close();
			final String jsontext = new String(sb.toString());
			final JSONObject googleMapJSONEntireObject = (JSONObject) new JSONTokener(
					jsontext).nextValue();
			final JSONArray googleMapJSONRoutes = googleMapJSONEntireObject
					.getJSONArray("routes");
			if (googleMapJSONRoutes.length() <= 0)
				return 0;
			// use the first route
			final JSONObject googleMapJSONRoute = googleMapJSONRoutes
					.getJSONObject(0);
			final JSONArray googleMapJSONLegs = googleMapJSONRoute
					.getJSONArray("legs");
			for (int i = 0; i < googleMapJSONLegs.length(); i++)
			{
				final JSONObject googleMapJSONLegObject = googleMapJSONLegs
						.getJSONObject(i);
				final JSONObject googleMapJSONDurationObject = googleMapJSONLegObject
						.getJSONObject("duration");
				durationSec += Integer.parseInt(googleMapJSONDurationObject
						.getString("value"));
			}
		} catch (final Exception je)
		{
			Log.e(TAG, "getDuration Error " + je.getMessage(), je);
		} finally
		{
			if (is != null)
				try
				{
					is.close();
				} catch (final IOException e)
				{
					Log.w(TAG, "Error closing InputStream", e);
				}
		}
		return durationSec / 60;
	}

	/**
	 * 'Geocodes' the given address, producing a GeoPoint at the best guess
	 * location
	 * 
	 * @param address
	 *            address to geocode
	 * @return GeoPoint representing the best guess lat/long for the address
	 */
	public static GeoPoint getLocation(final String address)
	{
		InputStream is = null;
		try
		{
			final String formattedAddress = formatAddress(address);
			final StringBuffer urlString = new StringBuffer();
			urlString
					.append("http://maps.googleapis.com/maps/api/geocode/json");
			urlString.append("?address=");
			urlString.append(formattedAddress);
			urlString.append("&sensor=false");
			Log.v(TAG, "getLocation URL: " + urlString.toString());
			final String url = urlString.toString();
			is = getConnection(url);
			if (is == null)
				return null;
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					is));
			final StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null)
				sb.append(line + "\n");
			br.close();
			final String jsontext = new String(sb.toString());
			final JSONObject googleMapJSONEntireObject = (JSONObject) new JSONTokener(
					jsontext).nextValue();
			final JSONArray googleMapJSONResultArray = (JSONArray) googleMapJSONEntireObject
					.get("results");
			if (googleMapJSONResultArray.length() == 0)
			{
				Log.v(TAG, "No location found for " + address);
				return null;
			}
			final JSONObject googleMapJSONLocation = googleMapJSONResultArray
					.getJSONObject(0).getJSONObject("geometry")
					.getJSONObject("location");
			final double lat = googleMapJSONLocation.getDouble("lat");
			final double lng = googleMapJSONLocation.getDouble("lng");
			return new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		} catch (final Exception e)
		{
			Log.e(TAG, "getLocation Error", e);
		} finally
		{
			if (is != null)
				try
				{
					is.close();
				} catch (final IOException e)
				{
					Log.w(TAG, "Error closing InputStream", e);
				}
		}
		return null;
	}
}
