package com.github.whentoleave.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * WidgetProvider used to handle all Widget updates. Creates a
 * WidgetUpdateService to handle every minute updates to the widget(s) (as
 * AppWidgetProviders cannot bind to services such as our AppService). Note that
 * all widgets will show the same information
 */
public class WidgetProvider extends AppWidgetProvider
{
	/**
	 * AlarmManager used to update widget
	 */
	private static AlarmManager alarmManager;
	/**
	 * PendingIntent triggered by the alarm manager
	 */
	private static PendingIntent pendingIntent;
	/**
	 * Logging tag
	 */
	private static final String TAG = "WidgetProvider";
	/**
	 * Action used to distinguish alarm actions from other intents received
	 */
	private static final String WIDGET_UPDATE_ACTION = "WHENTOLEAVE_WIDGET_UPDATE_ACTION";

	@Override
	public void onDisabled(final Context context)
	{
		super.onDisabled(context);
		Log.d(TAG, "onDisabled");
		if (alarmManager != null)
			alarmManager.cancel(pendingIntent);
		context.stopService(new Intent(context, WidgetUpdateService.class));
	}

	@Override
	public void onEnabled(final Context context)
	{
		super.onEnabled(context);
		Log.d(TAG, "onEnabled");
		alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		final Intent alarmIntent = new Intent(WIDGET_UPDATE_ACTION);
		pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
		// Set up the alarm to trigger every minute
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60000,
				pendingIntent);
	}

	/**
	 * If the intent's action is WIDGET_UPDATE_ACTION, then call the onUpdate
	 * method
	 */
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		super.onReceive(context, intent);
		if (WIDGET_UPDATE_ACTION.equals(intent.getAction()))
		{
			Log.d(TAG, "onReceive " + WIDGET_UPDATE_ACTION);
			final AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			final ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(), WidgetProvider.class.getName());
			final int[] appWidgetIds = appWidgetManager
					.getAppWidgetIds(thisAppWidget);
			onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}

	/**
	 * Starts the WidgetUpdateService to handle the widget update
	 */
	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
	{
		Log.d(TAG, "onUpdate");
		context.startService(new Intent(context, WidgetUpdateService.class));
	}
}