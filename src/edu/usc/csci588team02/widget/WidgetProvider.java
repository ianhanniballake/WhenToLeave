/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usc.csci588team02.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WidgetProvider extends AppWidgetProvider
{
	private static AlarmManager alarmManager;
	private static PendingIntent pendingIntent;
	// log tag
	private static final String TAG = "WidgetProvider";
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

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
	{
		Log.d(TAG, "onUpdate");
		context.startService(new Intent(context, WidgetUpdateService.class));
	}
}