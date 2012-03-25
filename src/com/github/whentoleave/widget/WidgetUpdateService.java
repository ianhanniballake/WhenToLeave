package com.github.whentoleave.widget;

import java.util.Calendar;
import java.util.Date;

import android.app.LoaderManager.LoaderCallbacks;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.RemoteViews;

import com.github.whentoleave.BuildConfig;
import com.github.whentoleave.R;
import com.github.whentoleave.maps.RouteInformation;
import com.github.whentoleave.service.LocationService;
import com.github.whentoleave.service.LocationServiceConnection;
import com.github.whentoleave.ui.MainActivity;

/**
 * Service used to update all Widgets.
 */
public class WidgetUpdateService extends Service implements
		LoaderCallbacks<Cursor>, Handler.Callback
{
	/**
	 * Preferences name to load settings from
	 */
	private static final String PREF = "MyPrefs";
	/**
	 * Logging tag
	 */
	private static final String TAG = "WidgetUpdateService";

	/**
	 * Formats the given number of minutes into usable String. For <60 minutes,
	 * returns "MMm", with no leading 0 (i.e., 6m or 15m). For >=60 minutes,
	 * returns "HH:MMh" with no leading hour 0 (i.e., 1:04h or 11:15h)
	 * 
	 * @param leaveInMinutes
	 *            the number of minutes to be formatted
	 * @return a formatted string representing the given leaveInMinutes in "MMm"
	 *         (<60) or "HH:MMh" (>=60)
	 */
	private static String formatWhenToLeave(final long leaveInMinutes)
	{
		final long hoursToGo = Math.abs(leaveInMinutes) / 60;
		final long minutesToGo = Math.abs(leaveInMinutes) % 60;
		final StringBuffer formattedTime = new StringBuffer();
		if (hoursToGo > 0)
		{
			formattedTime.append(hoursToGo);
			formattedTime.append(":");
			if (minutesToGo < 10)
				formattedTime.append("0");
			formattedTime.append(minutesToGo);
			formattedTime.append("h");
		}
		else
		{
			formattedTime.append(minutesToGo);
			formattedTime.append("m");
		}
		return formattedTime.toString();
	}

	/**
	 * Adapter to the retrieved data
	 */
	private CursorAdapter adapter;
	/**
	 * Current location of the device
	 */
	private Location currentLocation = null;
	/**
	 * Connection to the AppService
	 */
	private final LocationServiceConnection service = new LocationServiceConnection(
			new Handler(this));

	/**
	 * Gets a 'base' remote view as all widgets contain the same type of
	 * information
	 * 
	 * @return a basic RemoteViews object for all widgets
	 */
	private RemoteViews getBaseRemoteViews()
	{
		// As all widgets will contain the same information, we create the
		// RemoteView here, reducing unnecessary work on the system
		final RemoteViews views = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.widget_provider);
		final Intent app = new Intent(getBaseContext(), MainActivity.class);
		final PendingIntent launchApp = PendingIntent.getActivity(
				getBaseContext(), 0,
				app.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widgetClickableLayout, launchApp);
		return views;
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		if (msg.what == LocationService.MSG_LOCATION_UPDATE && msg.obj != null)
		{
			if (BuildConfig.DEBUG)
				Log.d(WidgetUpdateService.TAG, "onLocationChanged");
			currentLocation = (Location) msg.obj;
			update();
			return true;
		}
		return false;
	}

	@Override
	public IBinder onBind(final Intent intent)
	{
		return null;
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		final Calendar twoWeeksFromNow = Calendar.getInstance();
		twoWeeksFromNow.add(Calendar.DATE, 14);
		final String selection = CalendarContract.Events.DTSTART + ">=? AND "
				+ CalendarContract.Events.DTEND + "<? AND "
				+ CalendarContract.Events.EVENT_LOCATION + " IS NOT NULL";
		final String selectionArgs[] = {
				Long.toString(Calendar.getInstance().getTimeInMillis()),
				Long.toString(twoWeeksFromNow.getTimeInMillis()) };
		final String[] projection = { BaseColumns._ID,
				CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART,
				CalendarContract.Events.EVENT_LOCATION };
		return new CursorLoader(this, CalendarContract.Events.CONTENT_URI,
				projection, selection, selectionArgs,
				CalendarContract.Events.DTSTART);
	}

	@Override
	public void onDestroy()
	{
		if (BuildConfig.DEBUG)
			Log.d(WidgetUpdateService.TAG, "onDestroy");
		service.unregister();
		unbindService(service);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		adapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		adapter.swapCursor(data);
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId)
	{
		if (!service.isConnected())
		{
			if (BuildConfig.DEBUG)
				Log.d(WidgetUpdateService.TAG, "Connecting to AppService");
			bindService(new Intent(this, LocationService.class), service,
					Context.BIND_AUTO_CREATE);
			// updateAppWidgets will be called by refreshData once we are
			// connected
		}
		return Service.START_STICKY;
	}

	/**
	 * Build the RemoteViews with the most up to date information
	 */
	private void update()
	{
		final RemoteViews views = getBaseRemoteViews();
		final Cursor data = adapter.getCursor();
		if (data == null || !data.moveToFirst())
		{
			views.setTextViewText(R.id.widgetLeaveInText, "");
			views.setTextViewText(R.id.widgetEventDetail, "No upcoming events");
			views.setTextViewText(R.id.widgetEventTime, "");
			updateAllWidgets(views);
			return;
		}
		final CharSequence leaveIn;
		if (currentLocation == null)
			leaveIn = "Needs GPS";
		else
		{
			final SharedPreferences settings = getSharedPreferences(
					WidgetUpdateService.PREF, 0);
			final String travelType = settings.getString("TransportPreference",
					"driving");
			final int locationColumnIndex = data
					.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
			final String location = data.getString(locationColumnIndex);
			final int startTimeColumnIndex = data
					.getColumnIndex(CalendarContract.Events.DTSTART);
			final long startTime = data.getLong(startTimeColumnIndex);
			final int travelTime = RouteInformation.getDuration(
					currentLocation, location, travelType);
			final long minutesUntilEvent = (startTime - new Date().getTime()) / 60000;
			final long leaveInMinutes = minutesUntilEvent - travelTime;
			final int notifyTimeInMin = settings.getInt("NotifyTime", 3600) / 60;
			if (leaveInMinutes < notifyTimeInMin * .33333)
				views.setInt(R.id.widgetBackgroudLayout,
						"setBackgroundResource",
						R.drawable.custom_widget_background_red);
			else if (leaveInMinutes < notifyTimeInMin * .6666)
				views.setInt(R.id.widgetBackgroudLayout,
						"setBackgroundResource",
						R.drawable.custom_widget_background_orange);
			else
				views.setInt(R.id.widgetBackgroudLayout,
						"setBackgroundResource",
						R.drawable.custom_widget_background_green);
			if (leaveInMinutes < 0)
				leaveIn = "Leave now!";
			else
				leaveIn = "Leave in "
						+ WidgetUpdateService.formatWhenToLeave(leaveInMinutes);
		}
		final int titleColumnIndex = data
				.getColumnIndex(CalendarContract.Events.TITLE);
		final String title = data.getString(titleColumnIndex);
		final int locationColumnIndex = data
				.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
		final String location = data.getString(locationColumnIndex);
		final String truncatedWhere = location.length() > 13 ? location
				.substring(0, 10) + "..." : location;
		final int startTimeColumnIndex = data
				.getColumnIndex(CalendarContract.Events.DTSTART);
		final long startTime = data.getLong(startTimeColumnIndex);
		final CharSequence eventTime = DateFormat.format("hh:mma", startTime)
				+ " @ " + truncatedWhere;
		views.setTextViewText(R.id.widgetLeaveInText, leaveIn);
		views.setTextViewText(R.id.widgetEventDetail, title);
		views.setTextViewText(R.id.widgetEventTime, eventTime);
		final Intent nav = new Intent(Intent.ACTION_VIEW,
				Uri.parse("google.navigation:q="
						+ RouteInformation.formatAddress(location)));
		final PendingIntent launchNav = PendingIntent.getActivity(
				getBaseContext(), 0, nav, 0);
		views.setOnClickPendingIntent(R.id.widgetNavigationButton, launchNav);
		updateAllWidgets(views);
	}

	/**
	 * Updates all widgets with the given views
	 * 
	 * @param views
	 *            RemoteViews to set to update all widgets with
	 */
	private void updateAllWidgets(final RemoteViews views)
	{
		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getApplicationContext());
		final ComponentName thisAppWidget = new ComponentName(
				getApplicationContext().getPackageName(),
				WidgetProvider.class.getName());
		final int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(thisAppWidget);
		// Tell the widget manager
		for (final int appWidgetId : appWidgetIds)
			appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}
