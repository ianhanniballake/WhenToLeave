package edu.usc.csci588team02.activity;

import java.io.IOException;
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
 * Activity showing a map of all of the current day's events with locations. If
 * a GPS location is available, shows the current location and a route to the
 * next event. Works optimally as a tab for TabbedInterface.
 * 
 * @see TabbedInterface
 */
public class Map extends MapActivity implements Refreshable, LocationAware
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
	 * List of all overlays on the map
	 */
	private List<Overlay> mapOverlays;
	/**
	 * The Map View that constitutes this activity
	 */
	private MapView mapView;
	/**
	 * Current location of the device
	 */
	private Location mGpsLocation;
	/**
	 * Global references to the GPS location overlay and it's GeoPoint
	 */
	private OverlayItem mGpsOverlayItem;
	/**
	 * Route to our next destination from our current location, if it exists
	 */
	private Road mRoad = null;
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
	private final AppServiceConnection service = new AppServiceConnection(this,
			this);

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
		mGpsLocation = null;
		mGpsOverlayItem = null;
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return mRoad != null;
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
			Log.d(TAG, "onLocationChanged");
			mGpsLocation = location;
			try
			{
				final EventEntry ee = service.getNextEventWithLocation();
				if (ee != null)
					mRoad = RoadProvider.getRoute(location,
							ee.where.valueString);
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
		final GeoPoint geoPoint = RouteInformation.getLocation(eventLocation);
		if (geoPoint != null)
		{
			// Create a marker for the point
			// TODO Move this to only create one ItemizedOverlay
			final ItemizedOverlay itemizedOverlay = new ItemizedOverlay(icon,
					this);
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
			final EventEntry nextEvent = service.getNextEventWithLocation();
			long leaveInMinutes = 0;
			final int notifyTimeInMin = settings.getInt("NotifyTime", 3600) / 60;
			COLOR iconColor = COLOR.GREEN;
			if (mGpsLocation != null && nextEvent != null)
			{
				final String travelType = settings.getString(
						"TransportPreference", "driving");
				leaveInMinutes = nextEvent.getWhenToLeaveInMinutes(
						mGpsLocation, travelType);
				Log.d(TAG, "getting leaveInMinutes: " + leaveInMinutes);
			}
			// Plot events for the day
			// Create time window between midnight of this day and midnight
			// of next day
			final Calendar calendarToday = Calendar.getInstance();
			calendarToday.add(Calendar.HOUR_OF_DAY, -calendarToday.getTime()
					.getHours());
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
								nextEventPoint = plotEvent(event,
										redSquareDefault);
								break;
							default:
								plotEvent(event, greySquareDefault);
								break;
						}
					Log.v(TAG, "refreshData: Plotting Event: " + h);
				}
			}
			if (nextEventPoint != null)
				zoomTo(nextEventPoint);
			eventList.clear();
			eventList.addAll(events);
			// Add GPS location overlay if we have our location set
			if (mGpsLocation != null)
			{
				final GeoPoint gpsLocationPoint = new GeoPoint(
						(int) (mGpsLocation.getLatitude() * 1000000),
						(int) (mGpsLocation.getLongitude() * 1000000));
				mGpsOverlayItem = new OverlayItem(gpsLocationPoint, "", "");
				mGpsOverlayItem.setMarker(gpsLocationIcon);
				final ItemizedOverlay itemizedOverlay = new ItemizedOverlay(
						gpsLocationIcon, this);
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