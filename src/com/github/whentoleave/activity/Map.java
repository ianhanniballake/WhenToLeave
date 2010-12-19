package com.github.whentoleave.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.github.whentoleave.R;
import com.github.whentoleave.maps.ItemizedOverlay;
import com.github.whentoleave.maps.MapRouteOverlay;
import com.github.whentoleave.maps.Route;
import com.github.whentoleave.maps.RouteInformation;
import com.github.whentoleave.maps.RouteProvider;
import com.github.whentoleave.model.EventEntry;
import com.github.whentoleave.service.AppService;
import com.github.whentoleave.service.AppServiceConnection;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * Activity showing a map of all of the current day's events with locations. If
 * a GPS location is available, shows the current location and a route to the
 * next event. Works optimally as a tab for TabbedInterface.
 * 
 * @see TabbedInterface
 */
public class Map extends MapActivity implements Handler.Callback
{
	/**
	 * Possible Event Icon colors
	 */
	public enum COLOR {
		/**
		 * Green = Greater than 66% of Notify Time preference remaining for next
		 * upcoming event
		 */
		GREEN, /**
		 * Grey = Color for any other event
		 */
		GREY, /**
		 * Orange = 33% - 66% of Notify Time preference remaining for next
		 * upcoming event
		 */
		ORANGE, /**
		 * Red = <33% of Notify Time preference remaining for next
		 * upcoming event
		 */
		RED
	}

	/**
	 * Preferences name to load settings from
	 */
	private static final String PREF = "MyPrefs";
	/**
	 * Logging activity
	 */
	private static final String TAG = "MapActivity";
	/**
	 * Holds the list of all the events currently displayed on the map
	 */
	private final ArrayList<EventEntry> eventList = new ArrayList<EventEntry>();
	/**
	 * Overlay for the day's events
	 */
	private ItemizedOverlay eventOverlay;
	/**
	 * Icon representing the current GPS location
	 */
	private Drawable gpsLocationIcon;
	/**
	 * Default (non-numbered) green square
	 */
	private Drawable greenSquareDefault;
	/**
	 * Numbered green squares where their number = index+1 (i.e., [0] == number
	 * 1)
	 */
	private Drawable greenSquaresNumbered[];
	/**
	 * Default (non-numbered) grey square
	 */
	private Drawable greySquareDefault;
	/**
	 * Numbered grey squares where their number = index+1 (i.e., [0] == number
	 * 1)
	 */
	private Drawable greySquaresNumbered[];
	/**
	 * Overlay for the GPS location
	 */
	private ItemizedOverlay locationOverlay;
	/**
	 * Overlay for the route t the next event
	 */
	private final MapRouteOverlay mapRouteOverlay = new MapRouteOverlay();
	/**
	 * The Map View that constitutes this activity
	 */
	private MapView mapView;
	/**
	 * Current location of the device
	 */
	private Location mGpsLocation;
	/**
	 * Route to our next destination from our current location, if it exists
	 */
	private Route mRoute = null;
	private EventEntry nextEvent = null;
	/**
	 * Default (non-numbered) orange square
	 */
	private Drawable orangeSquareDefault;
	/**
	 * Numbered orange squares where their number = index+1 (i.e., [0] == number
	 * 1)
	 */
	private Drawable orangeSquaresNumbered[];
	/**
	 * Default (non-numbered) red square
	 */
	private Drawable redSquareDefault;
	/**
	 * Numbered red squares where their number = index+1 (i.e., [0] == number 1)
	 */
	private Drawable redSquaresNumbered[];
	/**
	 * Connection to the persistent, authorized service
	 */
	private final AppServiceConnection service = new AppServiceConnection(
			new Handler(this), false, true);

	/**
	 * Generates all of the icons that could be used in this
	 */
	private void generateDrawables()
	{
		final Resources resources = getResources();
		// GPS and Colored Square Resources
		gpsLocationIcon = resources.getDrawable(R.drawable.ic_gps_location);
		// Green Numbered Square Resources
		greenSquareDefault = resources.getDrawable(R.drawable.ic_green_square);
		greenSquaresNumbered = new Drawable[10];
		greenSquaresNumbered[0] = resources
				.getDrawable(R.drawable.ic_green_square_1);
		greenSquaresNumbered[1] = resources
				.getDrawable(R.drawable.ic_green_square_2);
		greenSquaresNumbered[2] = resources
				.getDrawable(R.drawable.ic_green_square_3);
		greenSquaresNumbered[3] = resources
				.getDrawable(R.drawable.ic_green_square_4);
		greenSquaresNumbered[4] = resources
				.getDrawable(R.drawable.ic_green_square_5);
		greenSquaresNumbered[5] = resources
				.getDrawable(R.drawable.ic_green_square_6);
		greenSquaresNumbered[6] = resources
				.getDrawable(R.drawable.ic_green_square_7);
		greenSquaresNumbered[7] = resources
				.getDrawable(R.drawable.ic_green_square_8);
		greenSquaresNumbered[8] = resources
				.getDrawable(R.drawable.ic_green_square_9);
		greenSquaresNumbered[9] = resources
				.getDrawable(R.drawable.ic_green_square_10);
		// Orange Numbered Square Resources
		orangeSquareDefault = resources
				.getDrawable(R.drawable.ic_orange_square);
		orangeSquaresNumbered = new Drawable[10];
		orangeSquaresNumbered[0] = resources
				.getDrawable(R.drawable.ic_orange_square_1);
		orangeSquaresNumbered[1] = resources
				.getDrawable(R.drawable.ic_orange_square_2);
		orangeSquaresNumbered[2] = resources
				.getDrawable(R.drawable.ic_orange_square_3);
		orangeSquaresNumbered[3] = resources
				.getDrawable(R.drawable.ic_orange_square_4);
		orangeSquaresNumbered[4] = resources
				.getDrawable(R.drawable.ic_orange_square_5);
		orangeSquaresNumbered[5] = resources
				.getDrawable(R.drawable.ic_orange_square_6);
		orangeSquaresNumbered[6] = resources
				.getDrawable(R.drawable.ic_orange_square_7);
		orangeSquaresNumbered[7] = resources
				.getDrawable(R.drawable.ic_orange_square_8);
		orangeSquaresNumbered[8] = resources
				.getDrawable(R.drawable.ic_orange_square_9);
		orangeSquaresNumbered[9] = resources
				.getDrawable(R.drawable.ic_orange_square_10);
		// Red Numbered Square Resources
		redSquareDefault = resources.getDrawable(R.drawable.ic_red_square);
		redSquaresNumbered = new Drawable[10];
		redSquaresNumbered[0] = resources
				.getDrawable(R.drawable.ic_red_square_1);
		redSquaresNumbered[1] = resources
				.getDrawable(R.drawable.ic_red_square_2);
		redSquaresNumbered[2] = resources
				.getDrawable(R.drawable.ic_red_square_3);
		redSquaresNumbered[3] = resources
				.getDrawable(R.drawable.ic_red_square_4);
		redSquaresNumbered[4] = resources
				.getDrawable(R.drawable.ic_red_square_5);
		redSquaresNumbered[5] = resources
				.getDrawable(R.drawable.ic_red_square_6);
		redSquaresNumbered[6] = resources
				.getDrawable(R.drawable.ic_red_square_7);
		redSquaresNumbered[7] = resources
				.getDrawable(R.drawable.ic_red_square_8);
		redSquaresNumbered[8] = resources
				.getDrawable(R.drawable.ic_red_square_9);
		redSquaresNumbered[9] = resources
				.getDrawable(R.drawable.ic_red_square_10);
		// Grey Numbered Square Resources
		greySquareDefault = resources.getDrawable(R.drawable.ic_grey_square);
		greySquaresNumbered = new Drawable[10];
		greySquaresNumbered[0] = resources
				.getDrawable(R.drawable.ic_grey_square_1);
		greySquaresNumbered[1] = resources
				.getDrawable(R.drawable.ic_grey_square_2);
		greySquaresNumbered[2] = resources
				.getDrawable(R.drawable.ic_grey_square_3);
		greySquaresNumbered[3] = resources
				.getDrawable(R.drawable.ic_grey_square_4);
		greySquaresNumbered[4] = resources
				.getDrawable(R.drawable.ic_grey_square_5);
		greySquaresNumbered[5] = resources
				.getDrawable(R.drawable.ic_grey_square_6);
		greySquaresNumbered[6] = resources
				.getDrawable(R.drawable.ic_grey_square_7);
		greySquaresNumbered[7] = resources
				.getDrawable(R.drawable.ic_grey_square_8);
		greySquaresNumbered[8] = resources
				.getDrawable(R.drawable.ic_grey_square_9);
		greySquaresNumbered[9] = resources
				.getDrawable(R.drawable.ic_grey_square_10);
		// Set bounds for the icons since mapview doesn't like to place them
		// without explicit bounds
		gpsLocationIcon.setBounds(0, 0, 36, 36);
		greenSquareDefault.setBounds(0, 0, 36, 36);
		for (final Drawable greenSquare : greenSquaresNumbered)
			greenSquare.setBounds(0, 0, 36, 36);
		orangeSquareDefault.setBounds(0, 0, 36, 36);
		for (final Drawable orangeSquare : orangeSquaresNumbered)
			orangeSquare.setBounds(0, 0, 36, 36);
		redSquareDefault.setBounds(0, 0, 36, 36);
		for (final Drawable redSquare : redSquaresNumbered)
			redSquare.setBounds(0, 0, 36, 36);
		greySquareDefault.setBounds(0, 0, 36, 36);
		for (final Drawable greySquare : greySquaresNumbered)
			greySquare.setBounds(0, 0, 36, 36);
	}

	private void handleGetEvents(final Set<EventEntry> events)
	{
		final SharedPreferences settings = getSharedPreferences(PREF, 0);
		long leaveInMinutes = 0;
		final int notifyTimeInMin = settings.getInt("NotifyTime", 3600) / 60;
		COLOR iconColor = COLOR.GREEN;
		if (mGpsLocation != null && nextEvent != null)
		{
			final String travelType = settings.getString("TransportPreference",
					"driving");
			leaveInMinutes = nextEvent.getWhenToLeaveInMinutes(mGpsLocation,
					travelType);
			Log.d(TAG, "getting leaveInMinutes: " + leaveInMinutes);
		}
		int h = 1;
		Log.v(TAG, "refreshData: size of events = " + events.size());
		GeoPoint nextEventPoint = null;
		eventOverlay.clearOverlay();
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
					Log.d(TAG, "next event found - leavin: " + leaveInMinutes);
				}
				else
					iconColor = COLOR.GREY;
			}
			else
				iconColor = COLOR.GREY;
			if (event.where != null && event.where.valueString != null
					&& !event.where.valueString.equals(""))
			{
				if (h <= 10)
					switch (iconColor)
					{
						case GREEN:
							nextEventPoint = plotEvent(event,
									greenSquaresNumbered[h - 1]);
							break;
						case ORANGE:
							nextEventPoint = plotEvent(event,
									orangeSquaresNumbered[h - 1]);
							break;
						case RED:
							nextEventPoint = plotEvent(event,
									redSquaresNumbered[h - 1]);
							break;
						default:
							plotEvent(event, greySquaresNumbered[h - 1]);
							break;
					}
				else
					switch (iconColor)
					{
						case GREEN:
							nextEventPoint = plotEvent(event,
									greenSquareDefault);
							break;
						case ORANGE:
							nextEventPoint = plotEvent(event,
									orangeSquareDefault);
							break;
						case RED:
							nextEventPoint = plotEvent(event, redSquareDefault);
							break;
						default:
							plotEvent(event, greySquareDefault);
							break;
					}
				Log.v(TAG, "refreshData: Plotting Event: " + h++);
			}
		}
		if (nextEventPoint != null)
			zoomTo(nextEventPoint);
		eventList.clear();
		eventList.addAll(events);
	}

	private void handleLocationUpdate(final Location location)
	{
		if (location != null)
		{
			Log.d(TAG, "onLocationChanged");
			mGpsLocation = location;
			final GeoPoint gpsLocationPoint = new GeoPoint(
					(int) (mGpsLocation.getLatitude() * 1000000),
					(int) (mGpsLocation.getLongitude() * 1000000));
			final OverlayItem mGpsOverlayItem = new OverlayItem(
					gpsLocationPoint, "", "");
			mGpsOverlayItem.setMarker(gpsLocationIcon);
			locationOverlay.clearOverlay();
			locationOverlay.addOverlay(mGpsOverlayItem, "");
			service.requestNextEventWithLocation();
		}
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what)
		{
			case AppService.MSG_LOCATION_UPDATE:
				final Location location = (Location) msg.obj;
				handleLocationUpdate(location);
				return true;
			case AppService.MSG_REFRESH_DATA:
				handleRefreshData();
				return true;
			case AppService.MSG_GET_NEXT_EVENT_WITH_LOCATION:
				nextEvent = (EventEntry) msg.obj;
				handleNextEvent();
				return true;
			case AppService.MSG_GET_EVENTS:
				@SuppressWarnings("unchecked")
				final Set<EventEntry> events = (Set<EventEntry>) msg.obj;
				handleGetEvents(events);
				return true;
			case AppService.MSG_ERROR:
				final String errorMessage = (String) msg.obj;
				final TextView textView = (TextView) findViewById(R.id.mapdescription);
				textView.setText("Error retrieving data: " + errorMessage);
				return true;
			default:
				return false;
		}
	}

	private void handleNextEvent()
	{
		if (mGpsLocation != null)
		{
			mRoute = RouteProvider.getRoute(mGpsLocation,
					nextEvent.where.valueString);
			mapRouteOverlay.setRoute(mRoute);
			final TextView textView = (TextView) findViewById(R.id.mapdescription);
			textView.setText(mRoute.mName + " " + mRoute.mDescription);
		}
	}

	/**
	 * Loads all of today's events on the user's calendar.
	 */
	public void handleRefreshData()
	{
		service.requestNextEventWithLocation();
		// Create time window between midnight of this day and midnight
		// of next day
		final Calendar calendarToday = Calendar.getInstance();
		calendarToday.add(Calendar.HOUR_OF_DAY, -calendarToday.getTime()
				.getHours());
		final Calendar calendarLaterToday = Calendar.getInstance();
		calendarLaterToday.add(Calendar.HOUR_OF_DAY, 24 - calendarLaterToday
				.getTime().getHours());
		service.requestEvents(calendarToday.getTime(),
				calendarLaterToday.getTime());
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return mRoute != null;
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
		final List<Overlay> mapOverlays = mapView.getOverlays();
		generateDrawables();
		mapOverlays.add(mapRouteOverlay);
		locationOverlay = new ItemizedOverlay(gpsLocationIcon, this);
		mapOverlays.add(locationOverlay);
		eventOverlay = new ItemizedOverlay(greySquareDefault, this);
		mapOverlays.add(eventOverlay);
		// Need to use getApplicationContext as this activity is used as a Tab
		getApplicationContext().bindService(new Intent(this, AppService.class),
				service, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		getApplicationContext().unbindService(service);
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
		final GeoPoint geoPoint = RouteInformation.getLocation(eventLocation);
		if (geoPoint != null)
		{
			// Create a marker for the point
			final OverlayItem overlayItem = new OverlayItem(geoPoint,
					"Appointment", "Appointment");
			overlayItem.setMarker(icon);
			// Add the point to the map
			eventOverlay.addOverlay(overlayItem, event.getSelfLink());
			return geoPoint;
		}
		return null;
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