package com.github.whentoleave.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.whentoleave.R;
import com.github.whentoleave.activity.TabbedInterface;
import com.github.whentoleave.maps.RouteInformation;
import com.github.whentoleave.model.EventEntry;
import com.github.whentoleave.service.AppService;
import com.github.whentoleave.service.AppServiceConnection;

/**
 * Service used to update all Widgets.
 */
public class WidgetUpdateService extends Service implements Handler.Callback
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
	 * Current location of the device
	 */
	private Location currentLocation = null;
	/**
	 * Connection to the AppService
	 */
	private final AppServiceConnection service = new AppServiceConnection(
			new Handler(this), false, true);

	private RemoteViews getBaseRemoteViews()
	{
		// As all widgets will contain the same information, we create the
		// RemoteView here, reducing unnecessary work on the system
		final RemoteViews views = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.widget_provider);
		final Intent app = new Intent(getBaseContext(), TabbedInterface.class);
		final PendingIntent launchApp = PendingIntent.getActivity(
				getBaseContext(), 0,
				app.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widgetClickableLayout, launchApp);
		return views;
	}

	private void handleError(final String errorMessage)
	{
		final RemoteViews views = getBaseRemoteViews();
		views.setTextViewText(R.id.widgetLeaveInText, "");
		views.setTextViewText(R.id.widgetEventDetail,
				"Error reading in next event");
		views.setTextViewText(R.id.widgetEventTime, errorMessage);
		updateAllWidgets(views);
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what)
		{
			case AppService.MSG_LOCATION_UPDATE:
				final Location location = (Location) msg.obj;
				currentLocation = location;
				handleRefreshData();
				return true;
			case AppService.MSG_REFRESH_DATA:
				handleRefreshData();
				return true;
			case AppService.MSG_GET_NEXT_EVENT_WITH_LOCATION:
				final EventEntry nextEvent = (EventEntry) msg.obj;
				handleNextEvent(nextEvent);
				return true;
			case AppService.MSG_ERROR:
				final String errorMessage = (String) msg.obj;
				handleError(errorMessage);
				return true;
			default:
				return false;
		}
	}

	private void handleNextEvent(final EventEntry nextEvent)
	{
		final RemoteViews views = getBaseRemoteViews();
		if (nextEvent == null)
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
			final SharedPreferences settings = getSharedPreferences(PREF, 0);
			final String travelType = settings.getString("TransportPreference",
					"driving");
			final long leaveInMinutes = nextEvent.getWhenToLeaveInMinutes(
					currentLocation, travelType);
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
						+ EventEntry.formatWhenToLeave(leaveInMinutes);
		}
		final CharSequence eventTitle = nextEvent.title;
		final String truncatedWhere = nextEvent.where.valueString.length() > 13 ? nextEvent.where.valueString
				.substring(0, 10) + "..."
				: nextEvent.where.valueString;
		final CharSequence eventTime = android.text.format.DateFormat.format(
				"hh:mma", nextEvent.when.startTime.value)
				+ " @ "
				+ truncatedWhere;
		views.setTextViewText(R.id.widgetLeaveInText, leaveIn);
		views.setTextViewText(R.id.widgetEventDetail, eventTitle);
		views.setTextViewText(R.id.widgetEventTime, eventTime);
		final Intent nav = new Intent(Intent.ACTION_VIEW,
				Uri.parse("google.navigation:q="
						+ RouteInformation
								.formatAddress(nextEvent.where.valueString)));
		final PendingIntent launchNav = PendingIntent.getActivity(
				getBaseContext(), 0, nav, 0);
		views.setOnClickPendingIntent(R.id.widgetNavigationButton, launchNav);
		updateAllWidgets(views);
	}

	@SuppressWarnings("unused")
	private void handleNotAuthenticated()
	{
		final RemoteViews views = getBaseRemoteViews();
		views.setTextViewText(R.id.widgetLeaveInText, "Click here to log in");
		views.setTextViewText(R.id.widgetEventDetail, "to your Google Account");
		views.setTextViewText(R.id.widgetEventTime, "");
		updateAllWidgets(views);
	}

	private void handleRefreshData()
	{
		service.requestNextEventWithLocation();
	}

	@Override
	public IBinder onBind(final Intent intent)
	{
		return null;
	}

	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy");
		unbindService(service);
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId)
	{
		if (!service.isConnected())
		{
			Log.d(TAG, "Connecting to AppService");
			bindService(new Intent(this, AppService.class), service,
					Context.BIND_AUTO_CREATE);
			// updateAppWidgets will be called by refreshData once we are
			// connected
		}
		else
			handleRefreshData();
		return START_STICKY;
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
