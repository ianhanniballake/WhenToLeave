package edu.usc.csci588team02.activity;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TabHost;
import edu.usc.csci588team02.R;

public class TabbedInterface extends TabActivity
{
	private class ActionBar extends Button
	{
		public ActionBar(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbed_interface);
		
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Reusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, Dashboard.class);
	    spec = tabHost.newTabSpec("dashboard").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_home))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, Agenda.class);
	    spec = tabHost.newTabSpec("agenda").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_home))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, Map.class);
	    spec = tabHost.newTabSpec("map").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_home))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
}