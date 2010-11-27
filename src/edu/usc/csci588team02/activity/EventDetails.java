package edu.usc.csci588team02.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.maps.RouteInformation;
import edu.usc.csci588team02.model.EventEntry;
import edu.usc.csci588team02.service.AppService;
import edu.usc.csci588team02.service.AppServiceConnection;

public class EventDetails extends Activity implements Refreshable
{
	private static final String TAG = "EventDetails";
	private final AppServiceConnection service = new AppServiceConnection(this);

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
		unbindService(service);
	}

	@Override
	public void refreshData()
	{
		final Bundle passedInValue = getIntent().getExtras();
		final String eventUrl = passedInValue.getString("eventUrl");
		final TextView eventDetailsName = (TextView) findViewById(R.id.eventDetailsName);
		try
		{
			final EventEntry event = service.getEvent(eventUrl);
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
				final CharSequence time = android.text.format.DateFormat
						.format("hh:mma 'on' EEEE, MMM dd",
								event.when.startTime.value);
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
						final String latLng = geoPoint.getLatitudeE6() / 1E6
								+ "," + geoPoint.getLongitudeE6() / 1E6;
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
		} catch (final IOException e)
		{
			Log.e(TAG, "Error while refreshing data", e);
			eventDetailsName.setText(e.toString());
		}
	}
}
