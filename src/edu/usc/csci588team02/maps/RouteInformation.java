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

public class RouteInformation {

	public enum TravelType { DRIVING, WALKING, BICYCLING}
	
	public static String getUrlFromString(String from, String to, TravelType travelType)
	{// connect to map web service
		
		//remove spaces
		while(from.indexOf("  ") != -1){
			from = from.replace("  "," ");
		}
		while(to.indexOf("  ") != -1){
			to = to.replace("  "," ");
		}
		from = from.replace(" ","+");
		to = to.replace(" ","+");
		
		final StringBuffer urlString = new StringBuffer();
		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
		urlString.append("?origin=");// from
		urlString.append(from);
		urlString.append("&destination=");// to
		urlString.append(to);
		urlString.append("&sensor=false");
		if(travelType == TravelType.BICYCLING){
			urlString.append("&mode=bicycling");
		}
		else if(travelType == TravelType.WALKING){
			urlString.append("&mode=walking");
		}
		else
		{
			urlString.append("&mode=driving");
		}
		System.out.println("URL: " + urlString.toString());
		return urlString.toString();
	}
	
	public static int getDuration(final String from, final String to, TravelType travelType)
    {
	    int durationSec = 0;
        try
        {
        	String url = getUrlFromString(from,to,travelType);
            InputStream is = getConnection(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
            	sb.append(line + "\n");
            }
            br.close();
            	
            String jsontext = new String(sb.toString());
            //System.out.println(jsontext);
            JSONObject googleMapJSONEntireObject = (JSONObject) new JSONTokener(jsontext).nextValue();
            JSONArray googleMapJSONRoutes = googleMapJSONEntireObject.getJSONArray("routes");
            JSONObject googleMapJSONRoute;
            if(googleMapJSONRoutes.length() <= 0){
            	return 0;
            }
            else
            {
            	//use the first route
            	googleMapJSONRoute = googleMapJSONRoutes.getJSONObject(0);
            }
            JSONArray googleMapJSONLegs = googleMapJSONRoute.getJSONArray("legs");
            for(int i = 0; i < googleMapJSONLegs.length();i++){
            	JSONObject googleMapJSONLegObject = googleMapJSONLegs.getJSONObject(i);
            	JSONObject googleMapJSONDurationObject = googleMapJSONLegObject.getJSONObject("duration");
            	durationSec += Integer.parseInt(googleMapJSONDurationObject.getString("value"));
            }
            


        }
        catch (Exception je)
        {
            System.out.println("Error w/file: " + je.getMessage());
            je.printStackTrace();
        }
        
        return durationSec/60; 
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
	

}
