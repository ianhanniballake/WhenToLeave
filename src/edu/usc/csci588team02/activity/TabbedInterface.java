package edu.usc.csci588team02.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TabHost;
import edu.usc.csci588team02.R;

public class TabbedInterface extends TabActivity
{
	private static final int DIALOG_TRANSPORTATION = 100;
	
	private class ActionBar extends Button
	{
		public ActionBar(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
	}
	
	//TODO: extend the ActionBar to recieve data for showing time to leave
	//as well as changing color easier
	private ActionBar actionBar;
	
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
	    
	    //Old Dashboard Tab
	    /*intent = new Intent().setClass(this, Dashboard.class);
	    spec = tabHost.newTabSpec("dashboard").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_home))
	                  .setContent(intent);
	    tabHost.addTab(spec);*/

	    //Event tab
	    intent = new Intent().setClass(this, Event.class);
	    spec = tabHost.newTabSpec("event").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_home))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    //Agenda tab
	    intent = new Intent().setClass(this, Agenda.class);
	    spec = tabHost.newTabSpec("agenda").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_agenda))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    //Map tab
	    intent = new Intent().setClass(this, Map.class);
	    spec = tabHost.newTabSpec("map").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_map))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    //Set default starting tab to Event/Home
	    tabHost.setCurrentTab(0);    
	    
	    //Setup Listeners for the ActionBar Buttons
	    ImageButton transportButton = (ImageButton)this.findViewById(R.id.transportModeButton);
	    transportButton.setOnClickListener(new OnClickListener()
		{
			//@Override
			public void onClick(final View view)
			{
				showDialog(DIALOG_TRANSPORTATION);
			}
		});
	    ImageButton refreshButton = (ImageButton)this.findViewById(R.id.refreshButton);
	    refreshButton.setOnClickListener(new OnClickListener()
		{
			//@Override
			public void onClick(final View view)
			{
				//Refresh the GPS and the Time to Leave
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(final int id)
	{
		switch (id)
		{
			case DIALOG_TRANSPORTATION:
				final AlertDialog transportDialog;
				AlertDialog.Builder builder;

				Context mContext = getApplicationContext();
				LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.transportation_dialog,
				   (ViewGroup) findViewById(R.id.layout_root));

				builder = new AlertDialog.Builder(TabbedInterface.this);
				builder.setView(layout);
				builder.setTitle("Choose Your Mode of Transport");

				transportDialog = builder.create();

				//Setup Custom Dialog Item Listeners and Settings
				ImageButton carButton = (ImageButton)layout.findViewById(R.id.carButton);
				carButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						// Do whatever when the button is clicked.
						transportDialog.dismiss();
					}
				});
				ImageButton publicButton = (ImageButton)layout.findViewById(R.id.publicButton);
				publicButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						// Do whatever when the button is clicked.
						transportDialog.dismiss();
					}
				});
				ImageButton walkButton = (ImageButton)layout.findViewById(R.id.walkButton);
				walkButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						// Do whatever when the button is clicked.
						transportDialog.dismiss();
					}
				});
				
				return transportDialog;
		}
		return super.onCreateDialog(id);
	}
}