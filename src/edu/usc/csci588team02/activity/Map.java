package edu.usc.csci588team02.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.manager.EventManager;
import edu.usc.csci588team02.maps.ItemizedOverlay;
import edu.usc.csci588team02.maps.RouteInformation;
import edu.usc.csci588team02.model.EventEntry;

/**
 * @author Stephanie Trudeau
 */
public class Map extends MapActivity implements Refreshable
{
	private class MyLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(final Location location)
		{
			if (location != null)
			{
				/*
				 * Toast.makeText( getBaseContext(), "Location changed : Lat: "
				 * + location.getLatitude() + " Lng: " +
				 * location.getLongitude(), Toast.LENGTH_SHORT).show();
				 */
				final GeoPoint point = new GeoPoint(
						(int) (location.getLatitude() * 1000000),
						(int) (location.getLongitude() * 1000000));
				final OverlayItem overlayitem = new OverlayItem(point, "", "");
				itemizedOverlay.addOverlay(overlayitem);
			}
		}

		@Override
		public void onProviderDisabled(final String provider)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(final String provider)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(final String provider, final int status,
				final Bundle extras)
		{
			// TODO Auto-generated method stub
		}
	}

	// Used for managing the list of events on the map
	private static EventManager eventManager = new EventManager();
	private static final int MENU_LOGOUT = 1;
	private static final int MENU_PREFERENCES = 2;
	private static final int MENU_VIEW_CALENDARS = 0;
	// Holds the list of all the events currently displayed on the map
	private final ArrayList<EventEntry> eventList = new ArrayList<EventEntry>();
	// The markers on the map
	Drawable greenSquare;
	Drawable greenSquare1;
	Drawable greenSquare2;
	Drawable greenSquare3;
	//
	ItemizedOverlay itemizedOverlay;
	LinearLayout linearLayout;
	private LocationManager lm;
	private LocationListener locationListener;
	// List of all overlays on the map
	List<Overlay> mapOverlays;
	MapView mapView;
	Drawable orangeSquare;
	Drawable orangeSquare1;
	Drawable orangeSquare2;
	Drawable orangeSquare3;
	Drawable redSquare;
	Drawable redSquare1;
	Drawable redSquare2;
	Drawable redSquare3;

	private void generateDrawables()
	{
		greenSquare = getResources().getDrawable(R.drawable.ic_green_square);
		greenSquare1 = getResources().getDrawable(R.drawable.ic_green_square_1);
		greenSquare2 = getResources().getDrawable(R.drawable.ic_green_square_2);
		greenSquare3 = getResources().getDrawable(R.drawable.ic_green_square_3);
		orangeSquare = getResources().getDrawable(R.drawable.ic_orange_square);
		orangeSquare1 = getResources().getDrawable(
				R.drawable.ic_orange_square_1);
		orangeSquare2 = getResources().getDrawable(
				R.drawable.ic_orange_square_2);
		orangeSquare3 = getResources().getDrawable(
				R.drawable.ic_orange_square_3);
		redSquare = getResources().getDrawable(R.drawable.ic_red_square);
		redSquare1 = getResources().getDrawable(R.drawable.ic_red_square_1);
		redSquare2 = getResources().getDrawable(R.drawable.ic_red_square_2);
		redSquare3 = getResources().getDrawable(R.drawable.ic_red_square_3);
		greenSquare.setBounds(0, 0, 36, 36);
		greenSquare1.setBounds(0, 0, 36, 36);
		greenSquare2.setBounds(0, 0, 36, 36);
		greenSquare3.setBounds(0, 0, 36, 36);
		orangeSquare.setBounds(0, 0, 36, 36);
		orangeSquare1.setBounds(0, 0, 36, 36);
		orangeSquare2.setBounds(0, 0, 36, 36);
		orangeSquare3.setBounds(0, 0, 36, 36);
		redSquare.setBounds(0, 0, 36, 36);
		redSquare1.setBounds(0, 0, 36, 36);
		redSquare2.setBounds(0, 0, 36, 36);
		redSquare3.setBounds(0, 0, 36, 36);
	}

	/**
	 * Gains authentication to allow you to access the user's Google Calendar
	 * events.
	 */
	private void getAuthentication()
	{
		final SharedPreferences settings = getSharedPreferences("MyPrefs", 0);
		final String authToken = settings.getString("authToken", null);
		eventManager.setAuthToken(authToken);
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

	/**
	 * Loads all of today's events on the user's calendar and plots them on the
	 * map.
	 */
	private void loadEventLocations()
	{
		String[] calendarEvents;
		try
		{
			final Calendar calendarToday = Calendar.getInstance();
			calendarToday.add(Calendar.DATE, 1);
			getAuthentication();
			final Set<EventEntry> events = eventManager
					.getEventsStartingNow(calendarToday.getTime());
			int h = 0;
			calendarEvents = new String[events.size()];
			System.out.println("size of events = " + events.size());
			GeoPoint lastAdded = null;
			for (final EventEntry event : events)
			{
				calendarEvents[h++] = event.title;
				if (event.where != null && event.where.valueString != null
						&& !event.where.valueString.equals(""))
				{
					calendarEvents[h - 1] = calendarEvents[h - 1] + " at "
							+ event.where.valueString;
					switch (h - 1)
					{
						case 0:
							lastAdded = plotEvent(greenSquare1,
									event.where.valueString);
						case 1:
							lastAdded = plotEvent(greenSquare2,
									event.where.valueString);
						case 2:
							lastAdded = plotEvent(greenSquare3,
									event.where.valueString);
						default:
							lastAdded = plotEvent(greenSquare,
									event.where.valueString);
					}
				}
			}
			if (lastAdded != null)
				zoomTo(lastAdded);
			eventList.clear();
			eventList.addAll(events);
		} catch (final IOException e)
		{
			e.printStackTrace();
			calendarEvents = new String[] { e.getMessage() };
		}
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
		loadEventLocations();
		// ---use the LocationManager class to obtain GPS locations---
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyLocationListener();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				locationListener);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, MENU_VIEW_CALENDARS, 0, "View Calendars");
		menu.add(0, MENU_LOGOUT, 0, "Logout");
		menu.add(0, MENU_PREFERENCES, 0, "Preferences");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_VIEW_CALENDARS:
				final Intent i = new Intent(this, Calendars.class);
				startActivity(i);
				return true;
			case MENU_LOGOUT:
				startActivityForResult(new Intent(this, Logout.class),
						Logout.REQUEST_LOGOUT);
				return true;
			case MENU_PREFERENCES:
				final Intent j = new Intent(this, Preferences.class);
				startActivity(j);
				return true;
		}
		return false;
	}

	/**
	 * Given the address of an event, this method plots it on the map.
	 * 
	 * @param icon
	 *            The Marker that will represent the event on the map
	 * @param eventLocation
	 *            The address of the event to be plotted
	 * @return the point just plotted
	 */
	private GeoPoint plotEvent(final Drawable icon, final String eventLocation)
	{
		// Obtain the latitude and longitude
		final GeoPoint geoPoint = getLatLon(eventLocation);
		// Create a marker for the point
		itemizedOverlay = new ItemizedOverlay(icon);
		final OverlayItem overlayItem = new OverlayItem(geoPoint, "1",
				"Appointment 1");
		overlayItem.setMarker(icon);
		// Add the point to the map
		itemizedOverlay.addOverlay(overlayItem);
		mapOverlays.add(itemizedOverlay);
		return geoPoint;
	}

	@Override
	public void refreshData()
	{
		// TODO Auto-generated method stub
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