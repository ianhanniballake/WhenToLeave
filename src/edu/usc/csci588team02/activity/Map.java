package edu.usc.csci588team02.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.maps.ItemizedOverlay;
import edu.usc.csci588team02.maps.MapRouteOverlay;
import edu.usc.csci588team02.maps.Road;
import edu.usc.csci588team02.maps.RoadProvider;
import edu.usc.csci588team02.maps.RouteInformation;
import edu.usc.csci588team02.maps.RouteInformation.TravelType;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.service.AppService;
import edu.usc.csci588team02.service.AppServiceConnection;

/**
 * @author Stephanie Trudeau
 */
public class Map extends MapActivity implements Refreshable, LocationAware
{
	public enum COLOR {
		GREEN, GREY, ORANGE, RED
	}

	private static final String PREF = "MyPrefs";
	private static final String TAG = "MapActivity";
	// Holds the list of all the events currently displayed on the map
	private final ArrayList<EventEntry> eventList = new ArrayList<EventEntry>();
	// Place markers in memory
	Drawable gpsLocationIcon;
	Drawable greenSquare;
	Drawable greenSquare1;
	Drawable greenSquare10;
	Drawable greenSquare2;
	Drawable greenSquare3;
	Drawable greenSquare4;
	Drawable greenSquare5;
	Drawable greenSquare6;
	Drawable greenSquare7;
	Drawable greenSquare8;
	Drawable greenSquare9;
	Drawable greySquare;
	Drawable greySquare1;
	Drawable greySquare10;
	Drawable greySquare2;
	Drawable greySquare3;
	Drawable greySquare4;
	Drawable greySquare5;
	Drawable greySquare6;
	Drawable greySquare7;
	Drawable greySquare8;
	Drawable greySquare9;
	private ItemizedOverlay itemizedOverlay;
	// List of all overlays on the map
	private List<Overlay> mapOverlays;
	private MapView mapView;
	Location mGpsLocation;
	GeoPoint mGpsLocationPoint;
	// Global references to the GPS location overlay and it's GeoPoint
	OverlayItem mGpsOverlayItem;
	Road mRoad = null;
	// Nory's route/road provider
	RoadProvider mRoadProvider;
	Drawable orangeSquare;
	Drawable orangeSquare1;
	Drawable orangeSquare10;
	Drawable orangeSquare2;
	Drawable orangeSquare3;
	Drawable orangeSquare4;
	Drawable orangeSquare5;
	Drawable orangeSquare6;
	Drawable orangeSquare7;
	Drawable orangeSquare8;
	Drawable orangeSquare9;
	Drawable redSquare;
	Drawable redSquare1;
	Drawable redSquare10;
	Drawable redSquare2;
	Drawable redSquare3;
	Drawable redSquare4;
	Drawable redSquare5;
	Drawable redSquare6;
	Drawable redSquare7;
	Drawable redSquare8;
	Drawable redSquare9;
	// Connection to the persistent service
	private final AppServiceConnection service = new AppServiceConnection(this,
			this);

	private void generateDrawables()
	{
		// GPS and Colored Square Resources
		gpsLocationIcon = getResources()
				.getDrawable(R.drawable.ic_gps_location);
		// Green Numbered Square Resources
		greenSquare = getResources().getDrawable(R.drawable.ic_green_square);
		greenSquare1 = getResources().getDrawable(R.drawable.ic_green_square_1);
		greenSquare2 = getResources().getDrawable(R.drawable.ic_green_square_2);
		greenSquare3 = getResources().getDrawable(R.drawable.ic_green_square_3);
		greenSquare4 = getResources().getDrawable(R.drawable.ic_green_square_4);
		greenSquare5 = getResources().getDrawable(R.drawable.ic_green_square_5);
		greenSquare6 = getResources().getDrawable(R.drawable.ic_green_square_6);
		greenSquare7 = getResources().getDrawable(R.drawable.ic_green_square_7);
		greenSquare8 = getResources().getDrawable(R.drawable.ic_green_square_8);
		greenSquare9 = getResources().getDrawable(R.drawable.ic_green_square_9);
		greenSquare10 = getResources().getDrawable(
				R.drawable.ic_green_square_10);
		// Orange Numbered Square Resources
		orangeSquare = getResources().getDrawable(R.drawable.ic_orange_square);
		orangeSquare1 = getResources().getDrawable(
				R.drawable.ic_orange_square_1);
		orangeSquare1 = getResources().getDrawable(
				R.drawable.ic_orange_square_1);
		orangeSquare2 = getResources().getDrawable(
				R.drawable.ic_orange_square_2);
		orangeSquare3 = getResources().getDrawable(
				R.drawable.ic_orange_square_3);
		orangeSquare4 = getResources().getDrawable(
				R.drawable.ic_orange_square_4);
		orangeSquare5 = getResources().getDrawable(
				R.drawable.ic_orange_square_5);
		orangeSquare6 = getResources().getDrawable(
				R.drawable.ic_orange_square_6);
		orangeSquare7 = getResources().getDrawable(
				R.drawable.ic_orange_square_7);
		orangeSquare8 = getResources().getDrawable(
				R.drawable.ic_orange_square_8);
		orangeSquare9 = getResources().getDrawable(
				R.drawable.ic_orange_square_9);
		orangeSquare10 = getResources().getDrawable(
				R.drawable.ic_orange_square_10);
		// Red Numbered Square Resources
		redSquare = getResources().getDrawable(R.drawable.ic_red_square);
		redSquare1 = getResources().getDrawable(R.drawable.ic_red_square_1);
		redSquare2 = getResources().getDrawable(R.drawable.ic_red_square_2);
		redSquare3 = getResources().getDrawable(R.drawable.ic_red_square_3);
		redSquare4 = getResources().getDrawable(R.drawable.ic_red_square_4);
		redSquare5 = getResources().getDrawable(R.drawable.ic_red_square_5);
		redSquare6 = getResources().getDrawable(R.drawable.ic_red_square_6);
		redSquare7 = getResources().getDrawable(R.drawable.ic_red_square_7);
		redSquare8 = getResources().getDrawable(R.drawable.ic_red_square_8);
		redSquare9 = getResources().getDrawable(R.drawable.ic_red_square_9);
		redSquare10 = getResources().getDrawable(R.drawable.ic_red_square_10);
		// Grey Numbered Square Resources
		greySquare = getResources().getDrawable(R.drawable.ic_grey_square);
		greySquare1 = getResources().getDrawable(R.drawable.ic_grey_square_1);
		greySquare2 = getResources().getDrawable(R.drawable.ic_grey_square_2);
		greySquare3 = getResources().getDrawable(R.drawable.ic_grey_square_3);
		greySquare4 = getResources().getDrawable(R.drawable.ic_grey_square_4);
		greySquare5 = getResources().getDrawable(R.drawable.ic_grey_square_5);
		greySquare6 = getResources().getDrawable(R.drawable.ic_grey_square_6);
		greySquare7 = getResources().getDrawable(R.drawable.ic_grey_square_7);
		greySquare8 = getResources().getDrawable(R.drawable.ic_grey_square_8);
		greySquare9 = getResources().getDrawable(R.drawable.ic_grey_square_9);
		greySquare10 = getResources().getDrawable(R.drawable.ic_grey_square_10);
		// Set bounds for the icons since mapview doesn't like to place them
		// without explicit bounds
		gpsLocationIcon.setBounds(0, 0, 36, 36);
		greenSquare.setBounds(0, 0, 36, 36);
		greenSquare1.setBounds(0, 0, 36, 36);
		greenSquare2.setBounds(0, 0, 36, 36);
		greenSquare3.setBounds(0, 0, 36, 36);
		greenSquare4.setBounds(0, 0, 36, 36);
		greenSquare5.setBounds(0, 0, 36, 36);
		greenSquare6.setBounds(0, 0, 36, 36);
		greenSquare7.setBounds(0, 0, 36, 36);
		greenSquare8.setBounds(0, 0, 36, 36);
		greenSquare9.setBounds(0, 0, 36, 36);
		greenSquare10.setBounds(0, 0, 36, 36);
		orangeSquare.setBounds(0, 0, 36, 36);
		orangeSquare1.setBounds(0, 0, 36, 36);
		orangeSquare2.setBounds(0, 0, 36, 36);
		orangeSquare3.setBounds(0, 0, 36, 36);
		orangeSquare4.setBounds(0, 0, 36, 36);
		orangeSquare5.setBounds(0, 0, 36, 36);
		orangeSquare6.setBounds(0, 0, 36, 36);
		orangeSquare7.setBounds(0, 0, 36, 36);
		orangeSquare8.setBounds(0, 0, 36, 36);
		orangeSquare9.setBounds(0, 0, 36, 36);
		orangeSquare10.setBounds(0, 0, 36, 36);
		redSquare.setBounds(0, 0, 36, 36);
		redSquare1.setBounds(0, 0, 36, 36);
		redSquare2.setBounds(0, 0, 36, 36);
		redSquare3.setBounds(0, 0, 36, 36);
		redSquare4.setBounds(0, 0, 36, 36);
		redSquare5.setBounds(0, 0, 36, 36);
		redSquare6.setBounds(0, 0, 36, 36);
		redSquare7.setBounds(0, 0, 36, 36);
		redSquare8.setBounds(0, 0, 36, 36);
		redSquare9.setBounds(0, 0, 36, 36);
		redSquare10.setBounds(0, 0, 36, 36);
		greySquare1.setBounds(0, 0, 36, 36);
		greySquare2.setBounds(0, 0, 36, 36);
		greySquare3.setBounds(0, 0, 36, 36);
		greySquare4.setBounds(0, 0, 36, 36);
		greySquare5.setBounds(0, 0, 36, 36);
		greySquare6.setBounds(0, 0, 36, 36);
		greySquare7.setBounds(0, 0, 36, 36);
		greySquare8.setBounds(0, 0, 36, 36);
		greySquare9.setBounds(0, 0, 36, 36);
		greySquare10.setBounds(0, 0, 36, 36);
		mGpsLocationPoint = null;
		mGpsLocation = null;
		mGpsOverlayItem = null;
	}

	private InputStream getConnection(final String url)
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

	public ArrayList<EventEntry> getEventList()
	{
		return eventList;
	}

	/**
	 * Gets the latitude and longitude based off an address.
	 * 
	 * @param eventLocation
	 *            The address of the event to be plotted
	 * @return the point on the map based on the address
	 */
	private GeoPoint getLatLon(final String eventLocation)
	{
		return RouteInformation.getLocation(eventLocation);
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		// Add the ability to zoom in and out on the map
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		// Initialize overlay variables
		mapOverlays = mapView.getOverlays();
		generateDrawables();
		// Need to use getApplicationContext as this activity is used as a Tab
		getApplicationContext().bindService(new Intent(this, AppService.class),
				service, Context.BIND_AUTO_CREATE);
		mRoadProvider = new RoadProvider();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		getApplicationContext().unbindService(service);
	}

	@Override
	public void onLocationChanged(final Location location)
	{
		if (location != null)
		{
			final GeoPoint point = new GeoPoint(
					(int) (location.getLatitude() * 1000000),
					(int) (location.getLongitude() * 1000000));
			mGpsLocationPoint = point;
			mGpsLocation = location;
			try
			{
				final EventEntry ee = service.getNextEventWithLocation();
				if (ee != null)
					if (ee.where != null)
						new Thread()
						{
							@Override
							public void run()
							{
								final GeoPoint gpCurrentEvent = getLatLon(ee.where.valueString);
								final String url = RoadProvider
										.getUrlFromLatLong(
												location.getLatitude(),
												location.getLongitude(),
												gpCurrentEvent.getLatitudeE6() / 1E6,
												gpCurrentEvent.getLongitudeE6() / 1E6);
								Log.v(TAG, "onLocationChanged URL: " + url);
								final InputStream is = getConnection(url);
								if (is != null)
									mRoad = RoadProvider.getRoute(is);
							}
						}.start();
			} catch (final IOException e)
			{
				Log.e(TAG, "onLocationChanged IO Error", e);
			} catch (final IllegalStateException e)
			{
				Log.e(TAG,
						"Error getting next event in map tab on location change for route information",
						e);
			}
		}
	}

	/**
	 * Given the address of an event, this method plots it on the map.
	 * 
	 * @param event
	 *            the event to plot
	 * 
	 * @param icon
	 *            The Marker that will represent the event on the map
	 * @return the point just plotted
	 */
	private GeoPoint plotEvent(final EventEntry event, final Drawable icon)
	{
		// Obtain the latitude and longitude
		final String eventLocation = event.where.valueString;
		final GeoPoint geoPoint = getLatLon(eventLocation);
		if (geoPoint != null)
		{
			// Create a marker for the point
			// TODO Move this to only create one ItemizedOverlay
			itemizedOverlay = new ItemizedOverlay(icon, mapView.getContext());
			final OverlayItem overlayItem = new OverlayItem(geoPoint,
					"Appointment", "Appointment");
			overlayItem.setMarker(icon);
			// Add the point to the map
			itemizedOverlay.addOverlay(overlayItem, event.getSelfLink());
			mapOverlays.add(itemizedOverlay);
			return geoPoint;
		}
		return null;
	}

	/**
	 * Loads all of today's events on the user's calendar and plots them on the
	 * map.
	 */
	@Override
	public void refreshData()
	{
		mapOverlays.clear();
		String[] calendarEvents;
		try
		{
			// Draw Route
			if (mRoad != null)
			{
				final TextView textView = (TextView) findViewById(R.id.mapdescription);
				textView.setText(mRoad.mName + " " + mRoad.mDescription);
				final MapRouteOverlay mapOverlay = new MapRouteOverlay(mRoad);
				mapOverlays.add(mapOverlay);
			}
			final SharedPreferences settings = getSharedPreferences(PREF, 0);
			EventEntry nextEvent = null;
			try
			{
				nextEvent = service.getNextEventWithLocation();
			} catch (final IllegalStateException e)
			{
				Log.e(TAG, "Error getting next event in map tab", e);
			}
			long leaveInMinutes = 0;
			final int notifyTimeInMin = settings.getInt("NotifyTime", 3600) / 60;
			COLOR iconColor = COLOR.GREEN;
			try
			{
				final Location loc = mGpsLocation;
				if (loc != null && nextEvent != null)
				{
					TravelType travelType = TravelType.DRIVING;
					final String travelTypePref = settings.getString(
							"TransportPreference", "DRIVING");
					if (travelTypePref.equals("BICYCLING"))
						travelType = TravelType.BICYCLING;
					else if (travelTypePref.equals("WALKING"))
						travelType = TravelType.WALKING;
					leaveInMinutes = nextEvent.getWhenToLeaveInMinutes(loc,
							travelType);
					Log.d(TAG, "getting leaveInMinutes: " + leaveInMinutes);
				}
			} catch (final IllegalStateException e)
			{
				Log.e(TAG, "Error checking location in map refresh", e);
			}
			try
			{
				// Plot events for the day
				// Create time window between midnight of this day and midnight
				// of next day
				final Calendar calendarToday = Calendar.getInstance();
				calendarToday.add(Calendar.HOUR_OF_DAY, -calendarToday
						.getTime().getHours());
				final Calendar calendarLaterToday = Calendar.getInstance();
				calendarLaterToday.add(Calendar.HOUR_OF_DAY,
						24 - calendarLaterToday.getTime().getHours());
				final Set<EventEntry> events = service.getEvents(
						calendarToday.getTime(), calendarLaterToday.getTime());
				int h = 0;
				calendarEvents = new String[events.size()];
				Log.v(TAG, "refreshData: size of events = " + events.size());
				GeoPoint nextEventPoint = null;
				for (final EventEntry event : events)
				{
					// Close enough evaluation...
					if (nextEvent != null)
					{
						if (nextEvent.title.equals(event.title)
								&& nextEvent.when.startTime
										.equals(event.when.startTime))
						{
							if (leaveInMinutes < notifyTimeInMin * .33333)
								iconColor = COLOR.RED;
							else if (leaveInMinutes < notifyTimeInMin * .6666)
								iconColor = COLOR.ORANGE;
							Log.d(TAG, "next event found - leavin: "
									+ leaveInMinutes);
						}
						else
							iconColor = COLOR.GREY;
					}
					else
						iconColor = COLOR.GREY;
					calendarEvents[h++] = event.title;
					if (event.where != null && event.where.valueString != null
							&& !event.where.valueString.equals(""))
					{
						calendarEvents[h - 1] = calendarEvents[h - 1] + " at "
								+ event.where.valueString;
						switch (h - 1)
						{
							case 0:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare1);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare1);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare1);
										break;
									default:
										plotEvent(event, greySquare1);
										break;
								}
								break;
							case 1:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare2);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare2);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare2);
										break;
									default:
										plotEvent(event, greySquare2);
										break;
								}
								break;
							case 2:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare3);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare3);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare3);
										break;
									default:
										plotEvent(event, greySquare3);
										break;
								}
								break;
							case 3:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare4);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare4);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare4);
										break;
									default:
										plotEvent(event, greySquare4);
										break;
								}
								break;
							case 4:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare5);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare5);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare5);
										break;
									default:
										plotEvent(event, greySquare5);
										break;
								}
								break;
							case 5:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare6);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare6);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare6);
										break;
									default:
										plotEvent(event, greySquare6);
										break;
								}
								break;
							case 6:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare7);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare7);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare7);
										break;
									default:
										plotEvent(event, greySquare7);
										break;
								}
								break;
							case 7:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare8);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare8);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare8);
										break;
									default:
										plotEvent(event, greySquare8);
										break;
								}
								break;
							case 8:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare9);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare9);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare9);
										break;
									default:
										plotEvent(event, greySquare9);
										break;
								}
								break;
							case 9:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare10);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare10);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare10);
										break;
									default:
										plotEvent(event, greySquare10);
										break;
								}
								break;
							default:
								switch (iconColor)
								{
									case GREEN:
										nextEventPoint = plotEvent(event,
												greenSquare);
										break;
									case ORANGE:
										nextEventPoint = plotEvent(event,
												orangeSquare);
										break;
									case RED:
										nextEventPoint = plotEvent(event,
												redSquare);
										break;
									default:
										plotEvent(event, greySquare);
										break;
								}
								break;
						}
						Log.v(TAG, "refreshData: Plotting Event: " + h);
					}
				}
				if (nextEventPoint != null)
					zoomTo(nextEventPoint);
				eventList.clear();
				eventList.addAll(events);
			} catch (final IllegalStateException e)
			{
				Log.e(TAG, "Error plotting map icons", e);
			}
			// Add GPS location overlay if we have our location set
			if (mGpsLocationPoint != null)
			{
				mGpsOverlayItem = new OverlayItem(mGpsLocationPoint, "", "");
				mGpsOverlayItem.setMarker(gpsLocationIcon);
				itemizedOverlay = new ItemizedOverlay(gpsLocationIcon,
						mapView.getContext());
				itemizedOverlay.addOverlay(mGpsOverlayItem, "");
				mapOverlays.add(itemizedOverlay);
			}
		} catch (final IOException e)
		{
			Log.e(TAG, "Error on refreshData", e);
			calendarEvents = new String[] { e.getMessage() };
		}
	}

	/**
	 * Zooms the map view to the given point.
	 * 
	 * @param geoPoint
	 *            Point that will be centered on the map
	 */
	private void zoomTo(final GeoPoint geoPoint)
	{
		final MapController mapController = mapView.getController();
		mapController.animateTo(geoPoint);
		mapController.setZoom(12);
	}
}