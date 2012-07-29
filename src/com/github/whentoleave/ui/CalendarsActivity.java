package com.github.whentoleave.ui;

import android.R;
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
	
    private SimpleCursorAdapter mAdapter;

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getListView().setTextFilterEnabled(true);
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		
		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, null,
				new String[] { CalendarContract.Calendars.CALENDAR_DISPLAY_NAME },
				new int[] { android.R.id.text1 }, 0);
		setListAdapter(mAdapter);
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int arg0, final Bundle arg1)
	{
		final String[] projection = { CalendarContract.Calendars._ID,
			    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
			    CalendarContract.Calendars.ACCOUNT_NAME
				};
		return new CursorLoader(this, CalendarContract.Calendars.CONTENT_URI,
				//projection, null, null, null);
				projection, null, null, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		mAdapter.swapCursor(data);
	}
}
