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
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.service.AppService;
import edu.usc.csci588team02.service.AppServiceConnection;

/**
 * @author Stephanie Trudeau
 */
public class Map extends MapActivity implements Refreshable, LocationAware
{
	private static final String TAG = "MapActivity";
	// Holds the list of all the events currently displayed on the map
	private final ArrayList<EventEntry> eventList = new ArrayList<EventEntry>();
	// The markers on the map
	Drawable gpsLocationIcon;
	Drawable greenSquare;
	Drawable greenSquare1;
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
	GeoPoint mGpsLocationPoint;
	// Global references to the GPS location overlay and it's GeoPoint
	OverlayItem mGpsOverlayItem;
	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(final android.os.Message msg)
		{
			if (mRoad != null)
				refreshData();
		}
	};
	Road mRoad = null;
	// Nory's route/road provider
	RoadProvider mRoadProvider;
	Drawable orangeSquare;
	Drawable orangeSquare1;
	Drawable redSquare;
	Drawable redSquare1;
	// Connection to the persistent service
	private final AppServiceConnection service = new AppServiceConnection(this,
			this);

	private void generateDrawables()
	{
		// GPS and Colored Square Resources
		gpsLocationIcon = getResources()
				.getDrawable(R.drawable.ic_gps_location);
		greenSquare = getResources().getDrawable(R.drawable.ic_green_square);
		greenSquare1 = getResources().getDrawable(R.drawable.ic_green_square_1);
		orangeSquare = getResources().getDrawable(R.drawable.ic_orange_square);
		orangeSquare1 = getResources().getDrawable(
				R.drawable.ic_orange_square_1);
		redSquare = getResources().getDrawable(R.drawable.ic_red_square);
		redSquare1 = getResources().getDrawable(R.drawable.ic_red_square_1);
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
		// GPS and Colored Square Bounds
		gpsLocationIcon.setBounds(0, 0, 36, 36);
		greenSquare.setBounds(0, 0, 36, 36);
		greenSquare1.setBounds(0, 0, 36, 36);
		orangeSquare.setBounds(0, 0, 36, 36);
		orangeSquare1.setBounds(0, 0, 36, 36);
		redSquare.setBounds(0, 0, 36, 36);
		redSquare1.setBounds(0, 0, 36, 36);
		mGpsLocationPoint = null;
		mGpsOverlayItem = new OverlayItem(mGpsLocationPoint, "", "");
		mGpsOverlayItem.setMarker(gpsLocationIcon);
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
								{
									mRoad = RoadProvider.getRoute(is);
									mHandler.sendEmptyMessage(0);
								}
							}
						}.start();
			} catch (final IOException e)
			{
				Log.e(TAG, "onLocationChanged IO Error", e);
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
			// Plot events for the day
			final Calendar calendarToday = Calendar.getInstance();
			calendarToday.add(Calendar.DATE, 1);
			final Set<EventEntry> events = service
					.getEventsStartingNow(calendarToday.getTime());
			int h = 0;
			calendarEvents = new String[events.size()];
			Log.v(TAG, "refreshData: size of events = " + events.size());
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
							lastAdded = plotEvent(event, greenSquare1);
							break;
						case 1:
							lastAdded = plotEvent(event, greySquare2);
							break;
						case 2:
							lastAdded = plotEvent(event, greySquare3);
							break;
						case 3:
							lastAdded = plotEvent(event, greySquare4);
							break;
						case 4:
							lastAdded = plotEvent(event, greySquare5);
							break;
						case 5:
							lastAdded = plotEvent(event, greySquare6);
							break;
						case 6:
							lastAdded = plotEvent(event, greySquare7);
							break;
						case 7:
							lastAdded = plotEvent(event, greySquare8);
							break;
						case 8:
							lastAdded = plotEvent(event, greySquare9);
							break;
						case 9:
							lastAdded = plotEvent(event, greySquare10);
							break;
						default:
							lastAdded = plotEvent(event, greySquare);
							break;
					}
					Log.v(TAG, "refreshData: Plotting Event: " + h);
				}
			}
			if (lastAdded != null)
				zoomTo(lastAdded);
			eventList.clear();
			eventList.addAll(events);
			// Add GPS location overlay if we have our location set
			if (mGpsLocationPoint != null)
			{
				mGpsOverlayItem = new OverlayItem(mGpsLocationPoint, "", "");
				mGpsOverlayItem.setMarker(gpsLocationIcon);
				itemizedOverlay.addOverlay(mGpsOverlayItem, "");
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