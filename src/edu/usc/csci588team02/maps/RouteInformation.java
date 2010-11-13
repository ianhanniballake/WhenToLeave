package edu.usc.csci588team02.maps;

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

import com.google.android.maps.GeoPoint;

public class RouteInformation
{
	public enum TravelType {
		BICYCLING, DRIVING, WALKING
	}

	public static String formatAddress(final String address)
	{
		String formattedAddress = address;
		// remove spaces
		while (formattedAddress.indexOf("  ") != -1)
			formattedAddress = formattedAddress.replace("  ", " ");
		formattedAddress = formattedAddress.replace(" ", "+");
		return formattedAddress;
	}

	static private InputStream getConnection(final String url)
	{
		InputStream is = null;
		try
		{
			final URLConnection conn = new URL(url).openConnection();
			is = conn.getInputStream();
		} catch (final MalformedURLException e)
		{
			e.printStackTrace();
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
		return is;
	}

	public static int getDuration(final String fromAddress,
			final String toAddress, final TravelType travelType)
	{
		int durationSec = 0;
		try
		{
			final String from = formatAddress(fromAddress);
			final String to = formatAddress(toAddress);
			final StringBuffer urlString = new StringBuffer();
			urlString
					.append("http://maps.googleapis.com/maps/api/directions/json");
			urlString.append("?origin=");// from
			urlString.append(from);
			urlString.append("&destination=");// to
			urlString.append(to);
			urlString.append("&sensor=false");
			if (travelType == TravelType.BICYCLING)
				urlString.append("&mode=bicycling");
			else if (travelType == TravelType.WALKING)
				urlString.append("&mode=walking");
			else
				urlString.append("&mode=driving");
			final String url = urlString.toString();
			System.out.println("URL: " + url);
			final InputStream is = getConnection(url);
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					is));
			final StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null)
				sb.append(line + "\n");
			br.close();
			final String jsontext = new String(sb.toString());
			// System.out.println(jsontext);
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
			System.out.println("Error w/file: " + je.getMessage());
			je.printStackTrace();
		}
		return durationSec / 60;
	}

	public static GeoPoint getLocation(final String address)
	{
		try
		{
			final String formattedAddress = formatAddress(address);
			final StringBuffer urlString = new StringBuffer();
			urlString
					.append("http://maps.googleapis.com/maps/api/geocode/json");
			urlString.append("?address=");
			urlString.append(formattedAddress);
			urlString.append("&sensor=false");
			System.out.println("URL: " + urlString.toString());
			final String url = urlString.toString();
			final InputStream is = getConnection(url);
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
			final JSONObject googleMapJSONLocation = ((JSONArray) googleMapJSONEntireObject
					.get("results")).getJSONObject(0).getJSONObject("geometry")
					.getJSONObject("location");
			final double lat = googleMapJSONLocation.getDouble("lat");
			final double lng = googleMapJSONLocation.getDouble("lng");
			return new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		} catch (final Exception e)
		{
			System.out.println("Error w/file: " + e.getMessage());
			e.printStackTrace();
		}
		return new GeoPoint(0, 0);
	}
}
