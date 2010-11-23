package edu.usc.csci588team02.widget;

import java.util.Date;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.activity.LocationAware;
import edu.usc.csci588team02.activity.Refreshable;
import edu.usc.csci588team02.service.AppService;
import edu.usc.csci588team02.service.AppServiceConnection;

public class WidgetUpdateService extends Service implements LocationAware,
		Refreshable
{
	private static final String TAG = "WidgetUpdateService";
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
		final ComponentName thisAppWidget = new ComponentName(
				getApplicationContext().getPackageName(),
				WidgetProvider.class.getName());
		final int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(thisAppWidget);
		for (final int appWidgetId : appWidgetIds)
			updateAppWidget(getApplicationContext(), appWidgetManager,
					appWidgetId);
	}

	private void updateAppWidget(final Context context,
			final AppWidgetManager appWidgetManager, final int appWidgetId)
	{
		Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId);
		final CharSequence leaveIn = "Leave in 1:04h";
		final CharSequence eventTitle = "Event Title";
		final CharSequence eventTime = android.text.format.DateFormat.format(
				"hh:mma", new Date()) + " @ Event Location";
		// Construct the RemoteViews object. It takes the package name (in our
		// case, it's our package, but it needs this because on the other side
		// it's the widget host inflating the layout from our package).
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_provider);
		views.setTextViewText(R.id.widgetLeaveInText, leaveIn);
		views.setTextViewText(R.id.widgetEventDetail, eventTitle);
		views.setTextViewText(R.id.widgetEventTime, eventTime);
		// Button transportButton = (Button)
		// findViewById(R.id.widgetEventDetailButton);
		// Tell the widget manager
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}
