package edu.usc.csci588team02.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.usc.csci588team02.R;

public class Event extends Activity
{
	public enum EventActionType { 
		MAP_LAUNCHER, 
		NAV_LAUNCHER, 
		EVENT_DETAIL,
		EVENT_RIGHT, 
		EVENT_LEFT
	}
	private static final int MENU_PREFERENCES = 2;
	private static final int MENU_LOGOUT = 1;
	
	private void launch(final EventActionType action)
	{
		//Gives user a choice between Browser and Maps
		/*Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
				Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
				startActivity(intent);*/

		switch(action)
		{
		case MAP_LAUNCHER:
		    Intent map = new Intent(Intent.ACTION_VIEW,
		    	    Uri.parse("geo:0,0?q=" + getResources().getString(R.string.eventLocation)));
		    	    startActivity(map);
			break;
		case NAV_LAUNCHER:
		    Intent nav = new Intent(Intent.ACTION_VIEW,
		    	    Uri.parse("google.navigation:q=" + getResources().getString(R.string.eventLocation)));
		    	    startActivity(nav);
			break;
		default:
			break;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event);

		//Setup Listeners for the ActionBar Buttons		
		Button mapButton = (Button)this.findViewById(R.id.mapButton);
		Button navButton = (Button)this.findViewById(R.id.navButton);
		
	    mapButton.setOnClickListener(new OnClickListener()
		{
			//@Override
			public void onClick(final View view)
			{
				launch(EventActionType.MAP_LAUNCHER);
			}
		});
	    navButton.setOnClickListener(new OnClickListener()
		{
			//@Override
			public void onClick(final View view)
			{
				launch(EventActionType.NAV_LAUNCHER);	
			}
		});
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