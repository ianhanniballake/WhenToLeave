package com.github.whentoleave.ui;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

/**
 * Retrieves a simple list of all of the user's calendars
 */
public final class CalendarsActivity extends ListActivity implements
		LoaderCallbacks<Cursor>
{
	/**
	 * Adapter to display the list's data
	 */
	private CursorAdapter adapter;

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getListView().setTextFilterEnabled(true);
		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, null,
				new String[] { CalendarContract.Calendars.NAME },
				new int[] { android.R.id.text1 }, 0);
		setListAdapter(adapter);
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int arg0, final Bundle arg1)
	{
		final String[] projection = { CalendarContract.Calendars.NAME };
		return new CursorLoader(this, CalendarContract.Calendars.CONTENT_URI,
				projection, null, null, CalendarContract.Calendars.NAME);
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
	}
}
