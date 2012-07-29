package com.github.whentoleave.ui;

import java.util.Calendar;
import java.util.Date;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.whentoleave.R;

/**
 * Fragment which shows a list of all events in the next two weeks.
 */
public class AgendaFragment extends ListFragment implements
		LoaderCallbacks<Cursor>
{
	/**
	 * Cursor Adapter for creating and binding agenda view items
	 */
	private class AgendaCursorAdapter extends CursorAdapter
	{
		/**
		 * Local reference to the layout inflater service
		 */
		private final LayoutInflater inflater;

		/**
		 * @param context
		 *            The context where the ListView associated with this
		 *            SimpleListItemFactory is running
		 * @param c
		 *            The database cursor. Can be null if the cursor is not
		 *            available yet.
		 * @param flags
		 *            Flags used to determine the behavior of the adapter, as
		 *            per
		 *            {@link CursorAdapter#CursorAdapter(Context, Cursor, int)}.
		 */
		public AgendaCursorAdapter(final Context context, final Cursor c,
				final int flags)
		{
			super(context, c, flags);
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(final View view, final Context context,
				final Cursor cursor)
		{
			// Set the event title
			final int titleColumnIndex = cursor
					.getColumnIndex(CalendarContract.Events.TITLE);
			final String title = cursor.getString(titleColumnIndex);
			final TextView titleView = (TextView) view
					.findViewById(R.id.agendaItemTitle);
			titleView.setText(title);
			// Set the event start time
			final int startTimeColumnIndex = cursor
					.getColumnIndex(CalendarContract.Events.DTSTART);
			final long startTime = cursor.getLong(startTimeColumnIndex);
			final TextView startTimeView = (TextView) view
					.findViewById(R.id.agendaItemWhen);
			startTimeView.setText(DateFormat.format("hh:mma 'on' EEEE, MMM dd",
					new Date(startTime)));
			// Set the event location
			final int locationColumnIndex = cursor
					.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
			final String location = cursor.getString(locationColumnIndex);
			final TextView locationView = (TextView) view
					.findViewById(R.id.agendaItemWhere);
			if (location.equals(""))
				locationView.setText(getText(R.string.event_no_location));
			else
				locationView.setText(location);
		}

		@Override
		public View newView(final Context context, final Cursor cursor,
				final ViewGroup parent)
		{
			View root = inflater.inflate(R.layout.agenda_item, parent, false);
			// TODO -SB
			//setEmptyText(getText(R.string.agenda_loading));
			return root;
		}
	}

	/**
	 * Adapter to display the list's data
	 */
	private CursorAdapter adapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		// TODO -SB
		//setEmptyText(getText(R.string.agenda_loading));
		adapter = new AgendaCursorAdapter(getActivity(), null, 0);
		setListAdapter(adapter);
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int arg0, final Bundle arg1)
	{
		final Calendar twoWeeksFromNow = Calendar.getInstance();
		twoWeeksFromNow.add(Calendar.DATE, 14);
		final String selection = CalendarContract.Events.DTSTART + ">=? AND "
				+ CalendarContract.Events.DTEND + "<?";
		final String selectionArgs[] = {
				Long.toString(Calendar.getInstance().getTimeInMillis()),
				Long.toString(twoWeeksFromNow.getTimeInMillis()) };
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
		return inflater.inflate(R.layout.agenda, container, false);
	}

	@Override
	public void onListItemClick(final ListView l, final View v,
			final int position, final long id)
	{
		final Intent detailsIntent = new Intent(getActivity(),
				EventDetailsFragment.class);
		detailsIntent.putExtra("eventId", adapter.getItemId(position));
		startActivity(detailsIntent);
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
		if (data.getCount() == 0)
			setEmptyText(getText(R.string.agenda_empty));
	}
}