package edu.usc.csci588team02.widget;

import java.io.IOException;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.activity.LocationAware;
import edu.usc.csci588team02.activity.Refreshable;
import edu.usc.csci588team02.activity.TabbedInterface;
import edu.usc.csci588team02.maps.RouteInformation;
import edu.usc.csci588team02.maps.RouteInformation.TravelType;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.service.AppService;
import edu.usc.csci588team02.service.AppServiceConnection;

public class WidgetUpdateService extends Service implements LocationAware,
		Refreshable
{
	private static final String PREF = "MyPrefs";
	private static final String TAG = "WidgetUpdateService";
	private Location currentLocation = null;
	private final AppServiceConnection service = new AppServiceConnection(this,
			this);

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
	public void onLocationChanged(final Location location)
	{
		currentLocation = location;
		refreshData();
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
			refreshData();
		return START_STICKY;
	}

	@Override
	public void refreshData()
	{
		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getApplicationContext());
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
		if (!service.isAuthenticated())
		{
			views.setTextViewText(R.id.widgetLeaveInText,
					"Click here to log in");
			views.setTextViewText(R.id.widgetEventDetail,
					"to your Google Account");
			views.setTextViewText(R.id.widgetEventTime, "");
			updateAllWidgets(appWidgetManager, views);
			return;
		}
		try
		{
			final EventEntry nextEvent = service.getNextEventWithLocation();
			if (nextEvent == null)
			{
				views.setTextViewText(R.id.widgetLeaveInText, "");
				views.setTextViewText(R.id.widgetEventDetail,
						"No upcoming events");
				views.setTextViewText(R.id.widgetEventTime, "");
				updateAllWidgets(appWidgetManager, views);
				return;
			}
			final CharSequence leaveIn;
			if (currentLocation == null)
				leaveIn = "Needs GPS";
			else
			{
				final SharedPreferences settings = getSharedPreferences(PREF, 0);
				TravelType travelType = TravelType.DRIVING;
				final String travelTypePref = settings.getString(
						"TransportPreference", "DRIVING");
				if (travelTypePref.equals("BICYCLING"))
					travelType = TravelType.BICYCLING;
				else if (travelTypePref.equals("WALKING"))
					travelType = TravelType.WALKING;
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
				final String formattedTime = EventEntry
						.formatWhenToLeave(leaveInMinutes);
				if (leaveInMinutes < 0)
					leaveIn = "Running " + formattedTime
							+ " behind - Leave now!";
				else
					leaveIn = "Leave in " + formattedTime;
			}
			final CharSequence eventTitle = nextEvent.title;
			final String truncatedWhere = nextEvent.where.valueString.length() > 13 ? nextEvent.where.valueString
					.substring(0, 10) + "..."
					: nextEvent.where.valueString;
			final CharSequence eventTime = android.text.format.DateFormat
					.format("hh:mma", nextEvent.when.startTime.value)
					+ " @ "
					+ truncatedWhere;
			views.setTextViewText(R.id.widgetLeaveInText, leaveIn);
			views.setTextViewText(R.id.widgetEventDetail, eventTitle);
			views.setTextViewText(R.id.widgetEventTime, eventTime);
			final Intent nav = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("google.navigation:q="
							+ RouteInformation
									.formatAddress(nextEvent.where.valueString)));
			final PendingIntent launchNav = PendingIntent.getActivity(
					getBaseContext(), 0, nav, 0);
			views.setOnClickPendingIntent(R.id.widgetNavigationButton,
					launchNav);
		
		} catch (final IllegalStateException e)
		{
			Log.e(TAG, "Error getting next event in the widget update service", e);
		} catch (final IOException e)
		{
			Log.e(TAG, "refreshData Error", e);
			views.setTextViewText(R.id.widgetLeaveInText, "");
			views.setTextViewText(R.id.widgetEventDetail,
					"Error reading in next event");
			views.setTextViewText(R.id.widgetEventTime, e.toString());
			views.setOnClickPendingIntent(R.id.widgetNavigationButton, null);
		} finally
		{
			updateAllWidgets(appWidgetManager, views);
		}
	}

	private void updateAllWidgets(final AppWidgetManager appWidgetManager,
			final RemoteViews views)
	{
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
