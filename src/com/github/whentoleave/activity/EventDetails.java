package com.github.whentoleave.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.github.whentoleave.R;
import com.github.whentoleave.maps.RouteInformation;
import com.github.whentoleave.model.EventEntry;
import com.github.whentoleave.service.AppService;
import com.github.whentoleave.service.AppServiceConnection;
import com.google.android.maps.GeoPoint;

/**
 * Activity showing the details of a passed in event (via
 * <code>intent.putExtra("eventUrl", event.getSelfLink());</code>) as a custom
 * pop up
 */
public class EventDetails extends Activity implements Handler.Callback
{
	/**
	 * Connection to the persistent, authorized service
	 */
	private final AppServiceConnection service = new AppServiceConnection(
			new Handler(this));

	/**
	 * Handles an error from the AppService
	 * 
	 * @param errorMessage
	 *            the error message to display
	 */
	private void handleError(final String errorMessage)
	{
		final TextView eventDetailsName = (TextView) findViewById(R.id.eventDetailsName);
		eventDetailsName.setText(errorMessage);
	}

	/**
	 * Handles a getEvent event from the AppService
	 * 
	 * @param event
	 *            newly returned event
	 */
	private void handleGetEvent(final EventEntry event)
	{
		final TextView eventDetailsName = (TextView) findViewById(R.id.eventDetailsName);
		eventDetailsName.setText(event.title);
		final TextView eventDetailsLocation = (TextView) findViewById(R.id.eventDetailsLocation);
		final TextView eventDetailsDescription = (TextView) findViewById(R.id.eventDetailsDescription);
		final TextView eventDetailsWhen = (TextView) findViewById(R.id.eventDetailsWhen);
		final Button eventDetailsMapButton = (Button) findViewById(R.id.eventDetailsMapButton);
		final Button eventDetailsNavButton = (Button) findViewById(R.id.eventDetailsNavButton);
		if (event.content != null)
			eventDetailsDescription.setText(event.content);
		if (event.when.startTime != null)
		{
			final CharSequence time = android.text.format.DateFormat.format(
					"hh:mma 'on' EEEE, MMM dd", event.when.startTime.value);
			eventDetailsWhen.setText(time);
		}
		if (event.where != null && event.where.valueString != null)
		{
			eventDetailsLocation.setText(event.where.valueString);
			eventDetailsMapButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					final GeoPoint geoPoint = RouteInformation
							.getLocation(event.where.valueString);
					final String latLng = geoPoint.getLatitudeE6() / 1E6 + ","
							+ geoPoint.getLongitudeE6() / 1E6;
					final Intent map = new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("geo:"
									+ latLng
									+ "?z=16&q="
									+ RouteInformation
											.formatAddress(event.where.valueString)));
					startActivity(map);
				}
			});
			eventDetailsMapButton.setVisibility(View.VISIBLE);
			eventDetailsMapButton.setClickable(true);
			eventDetailsNavButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					final Intent map = new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("google.navigation:q="
									+ RouteInformation
											.formatAddress(event.where.valueString)));
					startActivity(map);
				}
			});
			eventDetailsNavButton.setVisibility(View.VISIBLE);
			eventDetailsNavButton.setClickable(true);
		}
		else
		{
			eventDetailsMapButton.setVisibility(View.GONE);
			eventDetailsMapButton.setClickable(false);
			eventDetailsNavButton.setVisibility(View.GONE);
			eventDetailsNavButton.setClickable(false);
		}
	}

	@Override
	public boolean handleMessage(final Message msg)
	{
		switch (msg.what)
		{
			case AppService.MSG_ERROR:
				final String errorMessage = (String) msg.obj;
				handleError(errorMessage);
				return true;
			case AppService.MSG_GET_EVENT:
				final EventEntry event = (EventEntry) msg.obj;
				handleGetEvent(event);
				return true;
			case AppService.MSG_REFRESH_DATA:
				handleRefreshData();
				return true;
			default:
				return false;
		}
	}

	/**
	 * Handles a refreshData event from the AppService
	 */
	private void handleRefreshData()
	{
		final Bundle passedInValue = getIntent().getExtras();
		final String eventUrl = passedInValue.getString("eventUrl");
		service.requestEvent(eventUrl);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_details);
		bindService(new Intent(this, AppService.class), service,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		service.unregister();
		unbindService(service);
	}
}
