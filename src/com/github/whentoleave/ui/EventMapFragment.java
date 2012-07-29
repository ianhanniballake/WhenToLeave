package com.github.whentoleave.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.github.whentoleave.BuildConfig;
import com.github.whentoleave.R;
import com.github.whentoleave.maps.ItemizedOverlay;
import com.github.whentoleave.maps.MapRouteOverlay;
import com.github.whentoleave.maps.Route;
import com.github.whentoleave.maps.RouteInformation;
import com.github.whentoleave.service.LocationService;
import com.github.whentoleave.service.LocationServiceConnection;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * Fragment showing a map of all of the current day's events with locations. If
 * a GPS location is available, shows the current location and a route to the
 * next event.
 */
public class EventMapFragment extends Fragment implements
		LoaderCallbacks<Cursor>, Handler.Callback
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
	 * Adapter to display the list's data
	 */
	private CursorAdapter adapter;
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
	
	private ViewGroup mapContainer;
	/**
	 * Current location of the device
	 */
	private Location mGpsLocation;
	/**
	 * Route to our next destination from our current location, if it exists
	 */
	private final Route mRoute = null;
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
	private final LocationServiceConnection service = new LocationServiceConnection(
			new Handler(this));

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

	@Override
	public boolean handleMessage(final Message msg)
	{
		if (msg.what == LocationService.MSG_LOCATION_UPDATE && msg.obj != null)
		{
			if (BuildConfig.DEBUG)
				Log.d(EventMapFragment.TAG, "onLocationChanged");
			mGpsLocation = (Location) msg.obj;
			final GeoPoint gpsLocationPoint = new GeoPoint(
					(int) (mGpsLocation.getLatitude() * 1000000),
					(int) (mGpsLocation.getLongitude() * 1000000));
			final OverlayItem mGpsOverlayItem = new OverlayItem(
					gpsLocationPoint, "", "");
			mGpsOverlayItem.setMarker(gpsLocationIcon);
			locationOverlay.clearOverlay();
			locationOverlay.addOverlay(mGpsOverlayItem, -1);
			return true;
		}
		return false;
	}

	/**
	 * Whether a route from the current location to the next event is displayed
	 * 
	 * @return Whether a route from the current location to the next event is
	 *         displayed
	 */
	public boolean isRouteDisplayed()
	{
		return mRoute != null;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		// Get MapView reference from Main Activity.  Do not create within fragment
		mapView = ((MainActivity) getActivity()).getMapView();
		
		// Initialize overlay variables
		final List<Overlay> mapOverlays = mapView.getOverlays();
		generateDrawables();
		mapOverlays.add(mapRouteOverlay);
		locationOverlay = new ItemizedOverlay(gpsLocationIcon, getActivity());
		mapOverlays.add(locationOverlay);
		eventOverlay = new ItemizedOverlay(greySquareDefault, getActivity());
		mapOverlays.add(eventOverlay);
		((ViewGroup) getView().findViewById(R.id.mapview_holder))
				.addView(mapView);
		
		// Data adapter
		adapter = new CursorAdapter(getActivity(), null, 0)
		{
			@Override
			public void bindView(final View view, final Context context,
					final Cursor cursor)
			{
				// Nothing to do
			}

			@Override
			public View newView(final Context context, final Cursor cursor,
					final ViewGroup parent)
			{
				return null;
			}
		};
		getActivity().bindService(
				new Intent(getActivity(), LocationService.class), service,
				Context.BIND_AUTO_CREATE);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		// Create time window between midnight of this day and midnight
		// of next day
		final Calendar calendarToday = Calendar.getInstance();
		calendarToday.add(Calendar.HOUR_OF_DAY, -calendarToday.getTime()
				.getHours());
		final Calendar calendarLaterToday = Calendar.getInstance();
		calendarLaterToday.add(Calendar.HOUR_OF_DAY, 24 - calendarLaterToday
				.getTime().getHours());
		final String selection = CalendarContract.Events.DTSTART + ">=? AND "
				+ CalendarContract.Events.DTEND + "<?";
		final String selectionArgs[] = {
				Long.toString(calendarToday.getTimeInMillis()),
				Long.toString(calendarLaterToday.getTimeInMillis()) };
		final String[] projection = { BaseColumns._ID,
				CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART,
				CalendarContract.Events.EVENT_LOCATION };
		return new CursorLoader(getActivity(),
				CalendarContract.Events.CONTENT_URI, projection, selection,
				selectionArgs, CalendarContract.Events.DTSTART);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState)
	{
		View root =inflater.inflate(R.layout.map, container, false); 
        mapContainer = (ViewGroup) root.findViewById(R.id.mapview_holder);

		return root;
	}

	@Override
	public void onDestroy()
	{	
		super.onDestroy();
		service.unregister();
		getActivity().unbindService(service);
	}
	
	@Override
	public void onDestroyView()
	{
	    super.onDestroyView();
	    mapContainer.removeView(mapView);
	}
	
	@Override
	public void onPause()
	{
	    super.onPause();
	    mapContainer.removeView(mapView);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		adapter.swapCursor(null);
		eventOverlay.clearOverlay();
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		adapter.swapCursor(data);
		final SharedPreferences settings = getActivity().getSharedPreferences(
				EventMapFragment.PREF, 0);
		final String travelType = settings.getString("TransportPreference",
				"driving");
		final int notifyTimeInMin = settings.getInt("NotifyTime", 3600) / 60;
		long nextEventWithLocationId = -1;
		int h = 1;
		Log.v(EventMapFragment.TAG,
				"refreshData: size of events = " + data.getCount());
		COLOR iconColor = COLOR.GREEN;
		GeoPoint nextEventPoint = null;
		eventOverlay.clearOverlay();
		while (data.moveToNext())
		{
			final int locationColumnIndex = data
					.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
			final String location = data.getString(locationColumnIndex);
			// Skip events without a location
			if (location.equals(""))
				continue;
			final int idColumnIndex = data.getColumnIndex(BaseColumns._ID);
			final long currentId = data.getLong(idColumnIndex);
			if (nextEventWithLocationId == -1)
			{
				nextEventWithLocationId = currentId;
				long leaveInMinutes = 0;
				if (mGpsLocation != null)
				{
					final int startTimeColumnIndex = data
							.getColumnIndex(CalendarContract.Events.DTSTART);
					final long startTime = data.getLong(startTimeColumnIndex);
					final int travelTime = RouteInformation.getDuration(
							mGpsLocation, location, travelType);
					final long minutesUntilEvent = (startTime - new Date()
							.getTime()) / 60000;
					leaveInMinutes = minutesUntilEvent - travelTime;
					if (BuildConfig.DEBUG)
						Log.d(EventMapFragment.TAG, "getting leaveInMinutes: "
								+ leaveInMinutes);
				}
				if (leaveInMinutes < notifyTimeInMin * .33333)
					iconColor = COLOR.RED;
				else if (leaveInMinutes < notifyTimeInMin * .6666)
					iconColor = COLOR.ORANGE;
				else
					iconColor = COLOR.GREEN;
				if (BuildConfig.DEBUG)
					Log.d(EventMapFragment.TAG, "next event found - leavin: "
							+ leaveInMinutes);
			}
			else
				iconColor = COLOR.GREY;
			if (h <= 10)
				switch (iconColor)
				{
					case GREEN:
						nextEventPoint = plotEvent(data,
								greenSquaresNumbered[h - 1]);
						break;
					case ORANGE:
						nextEventPoint = plotEvent(data,
								orangeSquaresNumbered[h - 1]);
						break;
					case RED:
						nextEventPoint = plotEvent(data,
								redSquaresNumbered[h - 1]);
						break;
					default:
						plotEvent(data, greySquaresNumbered[h - 1]);
						break;
				}
			else
				switch (iconColor)
				{
					case GREEN:
						nextEventPoint = plotEvent(data, greenSquareDefault);
						break;
					case ORANGE:
						nextEventPoint = plotEvent(data, orangeSquareDefault);
						break;
					case RED:
						nextEventPoint = plotEvent(data, redSquareDefault);
						break;
					default:
						plotEvent(data, greySquareDefault);
						break;
				}
			Log.v(EventMapFragment.TAG, "refreshData: Plotting Event: " + h++);
		}
		if (nextEventPoint != null)
			zoomTo(nextEventPoint);
	}

	/**
	 * Plots a given event on the map.
	 * 
	 * @param data
	 *            cursor pointing to the event to plot
	 * 
	 * @param icon
	 *            The Marker that will represent the event on the map
	 * @return the point just plotted
	 */
	private GeoPoint plotEvent(final Cursor data, final Drawable icon)
	{
		final int locationColumnIndex = data
				.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
		final String location = data.getString(locationColumnIndex);
		// Obtain the latitude and longitude
		final GeoPoint geoPoint = RouteInformation.getLocation(location);
		if (geoPoint != null)
		{
			// Create a marker for the point
			final int titleColumnIndex = data
					.getColumnIndex(CalendarContract.Events.TITLE);
			final String title = data.getString(titleColumnIndex);
			final int startTimeColumnIndex = data
					.getColumnIndex(CalendarContract.Events.DTSTART);
			final long startTime = data.getLong(startTimeColumnIndex);
			final String formattedStartTime = DateFormat.format(
					"hh:mma 'on' EEEE, MMM dd", new Date(startTime)).toString();
			final OverlayItem overlayItem = new OverlayItem(geoPoint, title,
					formattedStartTime);
			overlayItem.setMarker(icon);
			// Add the point to the map
			final int idColumnIndex = data.getColumnIndex(BaseColumns._ID);
			final long eventId = data.getLong(idColumnIndex);
			eventOverlay.addOverlay(overlayItem, eventId);
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
		final MapController mapController = ((MainActivity)getActivity()).getMapView().getController();
		mapController.animateTo(geoPoint);
		mapController.setZoom(12);
	}
}