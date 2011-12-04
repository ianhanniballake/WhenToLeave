package com.github.whentoleave.ui;

import java.util.Calendar;
import java.util.Date;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.github.whentoleave.R;

/**
 * Fragment which shows the next event with a location, along with quick glance
 * information and buttons to get more details, get a map of the event, and
 * navigate to the event.
 */
public class HomeFragment extends Fragment implements LoaderCallbacks<Cursor>
{
	/**
	 * Adapter to the retrieved data
	 */
	private CursorAdapter adapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		adapter = new CursorAdapter(getActivity(), null, 0)
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
		// Create time window between midnight of this day and midnight
		// of next day
		final Calendar calendarToday = Calendar.getInstance();
		calendarToday.add(Calendar.HOUR_OF_DAY, -calendarToday.getTime()
				.getHours());
		final Calendar calendarLaterToday = Calendar.getInstance();
		calendarLaterToday.add(Calendar.HOUR_OF_DAY, 24 - calendarLaterToday
				.getTime().getHours());
		final String selection = CalendarContract.Events.DTSTART + ">=? AND "
				+ CalendarContract.Events.DTEND + "<? AND "
				+ CalendarContract.Events.EVENT_LOCATION + " IS NOT NULL";
		final String selectionArgs[] = {
				Long.toString(calendarToday.getTimeInMillis()),
				Long.toString(calendarLaterToday.getTimeInMillis()) };
		final String[] projection = { BaseColumns._ID,
				CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART,
				CalendarContract.Events.EVENT_LOCATION };
		return new CursorLoader(getActivity(),
				CalendarContract.Events.CONTENT_URI, projection, selection,
				selectionArgs, CalendarContract.Events.DTSTART);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.home, container, false);
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
		final TextView eventName = (TextView) getView().findViewById(
				R.id.eventName);
		final TextView eventLocation = (TextView) getView().findViewById(
				R.id.eventLocation);
		final TextView eventDescription = (TextView) getView().findViewById(
				R.id.eventDescription);
		final TextView eventWhen = (TextView) getView().findViewById(
				R.id.eventWhen);
		final Button mapButton = (Button) getView()
				.findViewById(R.id.mapButton);
		final Button navButton = (Button) getView()
				.findViewById(R.id.navButton);
		final Button infoButton = (Button) getView().findViewById(
				R.id.infoButton);
		if (!data.moveToFirst())
		{
			eventName.setText("No Events");
			eventLocation.setText("");
			eventDescription.setText("");
			eventWhen.setText("");
			infoButton.setOnClickListener(null);
			infoButton.setEnabled(false);
			mapButton.setOnClickListener(null);
			mapButton.setEnabled(false);
			navButton.setOnClickListener(null);
			navButton.setEnabled(false);
			return;
		}
		// Set the title
		final int titleColumnIndex = data
				.getColumnIndex(CalendarContract.Events.TITLE);
		data.getString(titleColumnIndex);
		final int locationColumnIndex = data
				.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
		final String location = data.getString(locationColumnIndex);
		eventLocation.setText(location);
		final int descriptionColumnIndex = data
				.getColumnIndex(CalendarContract.Events.DESCRIPTION);
		final String description = data.getString(descriptionColumnIndex);
		eventDescription.setText(description);
		final int startTimeColumnIndex = data
				.getColumnIndex(CalendarContract.Events.DTSTART);
		final long startTime = data.getLong(startTimeColumnIndex);
		final String formattedStartTime = DateFormat.format(
				"hh:mma 'on' EEEE, MMM dd", new Date(startTime)).toString();
		eventWhen.setText(formattedStartTime);
		final int idColumnIndex = data.getColumnIndex(BaseColumns._ID);
		final long eventId = data.getLong(idColumnIndex);
		infoButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				final Intent detailsIntent = new Intent(getActivity(),
						EventDetailsFragment.class);
				detailsIntent.putExtra("eventId", eventId);
				startActivity(detailsIntent);
			}
		});
		infoButton.setEnabled(true);
		mapButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				final Intent map = new Intent(Intent.ACTION_VIEW, Uri
						.parse("geo:0,0?q=" + location.replace(' ', '+')));
				startActivity(map);
			}
		});
		mapButton.setEnabled(true);
		navButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				final Intent nav = new Intent(Intent.ACTION_VIEW, Uri
						.parse("google.navigation:q="
								+ location.replace(' ', '+')));
				startActivity(nav);
			}
		});
		navButton.setEnabled(true);
	}
}