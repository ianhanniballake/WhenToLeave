package edu.usc.csci588team02.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import edu.usc.csci588team02.R;

public class Preferences extends Activity
{
	private static final int BACK = 0;
	
	public Preferences()
	{
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, BACK, 0, "Back");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case BACK:
				final Intent i = new Intent(this, TabbedInterface.class);
				startActivity(i);
				return true;
		}
		return false;
	}
}