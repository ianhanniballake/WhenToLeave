package com.github.whentoleave.ui;

import java.util.Date;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.github.whentoleave.R;
import com.github.whentoleave.maps.RouteInformation;
import com.google.android.maps.GeoPoint;

/**
 * Activity showing the details of a passed in event (via
 * <code>intent.putExtra("eventId", eventId);</code>) as a custom pop up
 */
public class EventDetailsFragment extends Activity implements
		LoaderCallbacks<Cursor>
{
	/**
	 * Cursor Adapter which holds the latest contraction
	 */
	private CursorAdapter adapter;

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_details);
		adapter = new CursorAdapter(this, null, 0)
		{
			@Override
			public void bindView(final View view, final Context context,
					final Cursor cursor)
			{
			}

			@Override
			public View newView(final Context context, final Cursor cursor,
					final ViewGroup parent)
			{
				return null;
			}
		};
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		final Bundle passedInValue = getIntent().getExtras();
		final long eventId = passedInValue.getLong("eventId");
		final String[] projection = { BaseColumns._ID,
				CalendarContract.Events.TITLE,
				CalendarContract.Events.DESCRIPTION,
				CalendarContract.Events.DTSTART,
				CalendarContract.Events.EVENT_LOCATION };
		return new CursorLoader(this, ContentUris.withAppendedId(
				CalendarContract.Events.CONTENT_URI, eventId), projection,
				null, null, null);
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
		if (!data.moveToFirst())
			return;
		// Set the event title
		final int titleColumnIndex = data
				.getColumnIndex(CalendarContract.Events.TITLE);
		final String title = data.getString(titleColumnIndex);
		final TextView eventDetailsName = (TextView) findViewById(R.id.eventDetailsName);
		eventDetailsName.setText(title);
		// Set the event description
		final int descriptionColumnIndex = data
				.getColumnIndex(CalendarContract.Events.DESCRIPTION);
		final String description = data.getString(descriptionColumnIndex);
		final TextView eventDetailsDescription = (TextView) findViewById(R.id.eventDetailsDescription);
		eventDetailsDescription.setText(description);
		// Set the event start time
		final int startTimeColumnIndex = data
				.getColumnIndex(CalendarContract.Events.DTSTART);
		final long startTime = data.getLong(startTimeColumnIndex);
		final TextView eventDetailsWhen = (TextView) findViewById(R.id.eventDetailsWhen);
		eventDetailsWhen.setText(DateFormat.format("hh:mma 'on' EEEE, MMM dd",
				new Date(startTime)));
		// Set the event location
		final int locationColumnIndex = data
				.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
		final String location = data.getString(locationColumnIndex);
		final TextView eventDetailsLocation = (TextView) findViewById(R.id.eventDetailsLocation);
		eventDetailsLocation.setText(location);
		// Set up the navigate and map buttons
		final Button eventDetailsMapButton = (Button) findViewById(R.id.eventDetailsMapButton);
		final Button eventDetailsNavButton = (Button) findViewById(R.id.eventDetailsNavButton);
		if (location.equals(""))
		{
			eventDetailsMapButton.setVisibility(View.GONE);
			eventDetailsMapButton.setClickable(false);
			eventDetailsNavButton.setVisibility(View.GONE);
			eventDetailsNavButton.setClickable(false);
		}
		else
		{
			eventDetailsMapButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					final GeoPoint geoPoint = RouteInformation
							.getLocation(location);
					final String latLng = geoPoint.getLatitudeE6() / 1E6 + ","
							+ geoPoint.getLongitudeE6() / 1E6;
					final Intent map = new Intent(Intent.ACTION_VIEW, Uri
							.parse("geo:" + latLng + "?z=16&q="
									+ RouteInformation.formatAddress(location)));
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
					final Intent map = new Intent(Intent.ACTION_VIEW, Uri
							.parse("google.navigation:q="
									+ RouteInformation.formatAddress(location)));
					startActivity(map);
				}
			});
			eventDetailsNavButton.setVisibility(View.VISIBLE);
			eventDetailsNavButton.setClickable(true);
		}
	}
}
