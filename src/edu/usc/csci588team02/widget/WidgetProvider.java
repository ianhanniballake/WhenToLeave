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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import edu.usc.csci588team02.R;

public class WidgetProvider extends AppWidgetProvider
{
	// log tag
	private static final String TAG = "ExampleAppWidgetProvider";

	static void updateAppWidget(final Context context,
			final AppWidgetManager appWidgetManager, final int appWidgetId,
			final String titlePrefix)
	{
		Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId
				+ " titlePrefix=" + titlePrefix);
		// Getting the string this way allows the string to be localized. The
		// format
		// string is filled in using java.util.Formatter-style format strings.
		final CharSequence text = "Test CharSequence";
		// Construct the RemoteViews object. It takes the package name (in our
		// case, it's our
		// package, but it needs this because on the other side it's the widget
		// host inflating
		// the layout from our package).
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_provider);
		views.setTextViewText(R.id.widgetEventDetailButton, text);
		// views.
		// Button transportButton = (Button)
		// findViewById(R.id.widgetEventDetailButton);
		// Tell the widget manager
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	@Override
	public void onDeleted(final Context context, final int[] appWidgetIds)
	{
		Log.d(TAG, "onDeleted");
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++)
		{
			// clean up
		}
	}

	@Override
	public void onDisabled(final Context context)
	{
		// When the first widget is created, stop listening for the
		// TIMEZONE_CHANGED and
		// TIME_CHANGED broadcasts.
		/*
		 * Log.d(TAG, "onDisabled"); PackageManager pm =
		 * context.getPackageManager(); pm.setComponentEnabledSetting( new
		 * ComponentName("com.example.android.apis",
		 * ".appwidget.ExampleBroadcastReceiver"),
		 * PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		 * PackageManager.DONT_KILL_APP);
		 */
	}

	@Override
	public void onEnabled(final Context context)
	{
		Log.d(TAG, "onEnabled");
		// When the first widget is created, register for the TIMEZONE_CHANGED
		// and TIME_CHANGED
		// broadcasts. We don't want to be listening for these if nobody has our
		// widget active.
		// This setting is sticky across reboots, but that doesn't matter,
		// because this will
		// be called after boot if there is a widget instance for this provider.
		/*
		 * PackageManager pm = context.getPackageManager();
		 * pm.setComponentEnabledSetting( new
		 * ComponentName("com.example.android.apis",
		 * ".appwidget.ExampleBroadcastReceiver"),
		 * PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		 * PackageManager.DONT_KILL_APP);
		 */
	}

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
	{
		Log.d(TAG, "onUpdate");
		// For each widget that needs an update:
		// - Create a RemoteViews object for it
		// - Set the text in the RemoteViews object
		// - Tell the AppWidgetManager to show that views object for the widget.
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++)
		{
			final int appWidgetId = appWidgetIds[i];
			final String titlePrefix = "Initial Widget Sketch";
			updateAppWidget(context, appWidgetManager, appWidgetId, titlePrefix);
		}
	}
}