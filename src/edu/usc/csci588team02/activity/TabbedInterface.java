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
import android.widget.ImageButton;
import android.widget.TabHost;
import edu.usc.csci588team02.R;

public class TabbedInterface extends TabActivity
{
	private static final int DIALOG_TRANSPORTATION = 100;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbed_interface);
		final Resources res = getResources(); // Resource object to get
												// Drawables
		final TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab
		
		startService(new Intent(this, edu.usc.csci588team02.service.AppService.class));
		
		// Old Dashboard Tab
		/*
		 * intent = new Intent().setClass(this, Dashboard.class); spec =
		 * tabHost.newTabSpec("dashboard").setIndicator("",
		 * res.getDrawable(R.drawable.ic_tab_home)) .setContent(intent);
		 * tabHost.addTab(spec);
		 */
		// Event tab
		intent = new Intent().setClass(this, Home.class);
		spec = tabHost.newTabSpec("event")
				.setIndicator("", res.getDrawable(R.drawable.ic_tab_home))
				.setContent(intent);
		tabHost.addTab(spec);
		// Agenda tab
		intent = new Intent().setClass(this, Agenda.class);
		spec = tabHost.newTabSpec("agenda")
				.setIndicator("", res.getDrawable(R.drawable.ic_tab_agenda))
				.setContent(intent);
		tabHost.addTab(spec);
		// Map tab
		intent = new Intent().setClass(this, Map.class);
		spec = tabHost.newTabSpec("map")
				.setIndicator("", res.getDrawable(R.drawable.ic_tab_map))
				.setContent(intent);
		tabHost.addTab(spec);
		
		//TODO: needs a little work before it becomes it's own tab - crashes currently.
		// Event Detail tab
		/*intent = new Intent().setClass(this, EventDetails.class);
		spec = tabHost.newTabSpec("eventdetail")
				.setIndicator("", res.getDrawable(R.drawable.ic_tab_event))
				.setContent(intent);
		tabHost.addTab(spec);*/
		
		// Set default starting tab to Event/Home
		tabHost.setCurrentTab(0);
		// Setup Listeners for the ActionBar Buttons
		final ImageButton transportButton = (ImageButton) findViewById(R.id.transportModeButton);
		transportButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				showDialog(DIALOG_TRANSPORTATION);
			}
		});
		final ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				// Refresh the GPS and the Time to Leave
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
				final Context mContext = getApplicationContext();
				final LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				final View layout = inflater.inflate(
						R.layout.transportation_dialog,
						(ViewGroup) findViewById(R.id.layout_root));
				builder = new AlertDialog.Builder(TabbedInterface.this);
				builder.setView(layout);
				builder.setTitle("Choose Your Mode of Transport");
				transportDialog = builder.create();
				// Setup Custom Dialog Item Listeners and Settings
				final ImageButton carButton = (ImageButton) layout
						.findViewById(R.id.carButton);
				carButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View view)
					{
						// Do whatever when the button is clicked.
						transportDialog.dismiss();
					}
				});
				final ImageButton publicButton = (ImageButton) layout
						.findViewById(R.id.publicButton);
				publicButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View view)
					{
						// Do whatever when the button is clicked.
						transportDialog.dismiss();
					}
				});
				final ImageButton walkButton = (ImageButton) layout
						.findViewById(R.id.walkButton);
				walkButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View view)
					{
						// Do whatever when the button is clicked.
						transportDialog.dismiss();
					}
				});
				return transportDialog;
		}
		return super.onCreateDialog(id);
	}
}