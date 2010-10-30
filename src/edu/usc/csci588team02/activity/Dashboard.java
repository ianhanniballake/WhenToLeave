package edu.usc.csci588team02.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.model.DashboardEntry;

public class Dashboard extends Activity
{
	private static final int MENU_PREFERENCES = 2;
	private static final int MENU_LOGOUT = 1;
	
	private class DashboardList extends BaseAdapter
	{
		private final Context context;

		public DashboardList(final Context context)
		{
			this.context = context;
		}

		@Override
		public int getCount()
		{
			return dashboardEntries.size();
		}

		@Override
		public Object getItem(final int position)
		{
			return null;
		}

		@Override
		public long getItemId(final int position)
		{
			return 0;
		}

		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent)
		{
			final DashboardEntry currentEntry = dashboardEntries.get(position);
			Button dashboardButton;
			if (convertView == null)
			{
				dashboardButton = new Button(context);
				dashboardButton.setLayoutParams(new GridView.LayoutParams(150,
						150));
				dashboardButton.setPadding(10, 10, 10, 10);
				dashboardButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View v)
					{
						launch(currentEntry);
					}
				});
			}
			else
				dashboardButton = (Button) convertView;
			dashboardButton.setText(currentEntry.getLabel());
			return dashboardButton;
		}
	}

	private final static ArrayList<DashboardEntry> dashboardEntries = new ArrayList<DashboardEntry>();
	static
	{
		//dashboardEntries.add(new DashboardEntry("Agenda", Agenda.class));
		//dashboardEntries.add(new DashboardEntry("Map", Map.class));
	}

	private void launch(final DashboardEntry entry)
	{
		final Intent i = new Intent(this, entry.getClassName());
		startActivity(i);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		final GridView dashboardGrid = (GridView) findViewById(R.id.dashboardGrid);
		dashboardGrid.setAdapter(new DashboardList(this));
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, MENU_LOGOUT, 0, "Logout");
		menu.add(0, MENU_PREFERENCES, 0, "Preferences");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_LOGOUT:
				startActivityForResult(new Intent(this, Logout.class),
						Logout.REQUEST_LOGOUT);
				return true;
			case MENU_PREFERENCES:
				final Intent i = new Intent(this, Preferences.class);
				startActivity(i);
				return true;
		}
		return false;
	}
}