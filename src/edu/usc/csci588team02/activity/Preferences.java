package edu.usc.csci588team02.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import edu.usc.csci588team02.R;
import edu.usc.csci588team02.service.AppService;

public class Preferences extends Activity
{
	protected final boolean DEBUG = false;
    protected int mPos;
    private static final String TAG = "PreferencesActivity";
    protected ArrayAdapter<CharSequence> mAdapter;
    protected static final String PREF = "MyPrefs";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		Resources r = getResources();
    	int[] iValues = r.getIntArray(R.array.interval_values);

		//Get current preferences
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		int interval = settings.getInt("RefreshInterval", 5);
		String actionBarPref = settings.getString("ActionBarPreference", "EventDetails");
		
		//Setup spinner data and callback
		Spinner refreshTime = (Spinner)  findViewById(R.id.spinnerRefreshTime);
		mAdapter = ArrayAdapter.createFromResource( this, R.array.intervals,
				android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		refreshTime.setAdapter(mAdapter);
        OnItemSelectedListener spinnerListener = new refreshTimeListener(this,this.mAdapter);
        refreshTime.setOnItemSelectedListener(spinnerListener);

        //set initial value to current preference for spinner
        refreshTime.setSelection(java.util.Arrays.binarySearch(iValues, interval), false);
        
        //Setup radio button data and callbacks
        RadioButton r1 = (RadioButton) findViewById(R.id.rbActionButtonPrefDetails);
        r1.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				SharedPreferences settings = getSharedPreferences(PREF, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("ActionBarPreference", "EventDetails");
				editor.commit();
				if (DEBUG)
					Log.d(TAG, "Should have commit r1: " + settings.getString("ActionBarPreference", "EventDetails"));
			}
		});
        
        RadioButton r2 = (RadioButton) findViewById(R.id.rbActionButtonPrefMap);
        r2.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				SharedPreferences settings = getSharedPreferences(PREF, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("ActionBarPreference", "Map");
				editor.commit();
				if (DEBUG)
					Log.d(TAG, "Should have commit r2: " + settings.getString("ActionBarPreference", "Map"));
			}
		});
        
        RadioButton r3 = (RadioButton) findViewById(R.id.rbActionButtonPrefNav);
        r3.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				SharedPreferences settings = getSharedPreferences(PREF, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("ActionBarPreference", "Navigate");
				editor.commit();
				if (DEBUG)
					Log.d(TAG, "Should have commit r3: " + settings.getString("ActionBarPreference", "Navigate"));
			}
		});
        
        if (DEBUG)
        {
	        Log.d(TAG, "Creating Preferences Activity, and interval is: " + interval);
	        Log.d(TAG, "Creating Preferences Activity, and pref is: " + actionBarPref);
        }
        
        //Setup radio button initial configuration
        if (actionBarPref.equals("EventDetails"))
        	r1.setChecked(true);
        else if (actionBarPref.equals("Map"))
        	r2.setChecked(true);
        else if (actionBarPref.equals("Navigate"))
        	r3.setChecked(true);
        else
        	r1.setChecked(true);    
	}
	
	 public class refreshTimeListener implements OnItemSelectedListener {
		    ArrayAdapter<CharSequence> mLocalAdapter;
	        Activity mLocalContext;

	        /**
	         *  Constructor
	         *  @param c - The activity that displays the Spinner.
	         *  @param ad - The Adapter view that
	         *    controls the Spinner.
	         *  Instantiate a new listener object.
	         */
	        public refreshTimeListener(Activity c, ArrayAdapter<CharSequence> ad) {

	          this.mLocalContext = c;
	          this.mLocalAdapter = ad;
	        }

	        /**
	         * When the user selects an item in the spinner, this method is invoked by the callback
	         * chain. Android calls the item selected listener for the spinner, which invokes the
	         * onItemSelected method.
	         *
	         * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(
	         *  android.widget.AdapterView, android.view.View, int, long)
	         * @param parent - the AdapterView for this listener
	         * @param v - the View for this listener
	         * @param pos - the 0-based position of the selection in the mLocalAdapter
	         * @param row - the 0-based row number of the selection in the View
	         */
	        public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {
	        	SharedPreferences settings = getSharedPreferences(PREF, 0);
				SharedPreferences.Editor editor = settings.edit();
	        	Resources r = getResources();
	        	int[] iValues = r.getIntArray(R.array.interval_values);

	        	int interval = 5;
	        	if (pos > 0 && (pos <= iValues.length))
	        		interval = iValues[pos];
	        	
	        	editor.putInt("RefreshInterval", interval);
	        	editor.commit();
				//AppService.this.setIntervalTime(interval);
	        	if (DEBUG)
	        	{
		        	Log.d(TAG, "Clicked on: " + interval);
		        	Log.d(TAG, "Committed: " + settings.getInt("RefreshInterval", 0));
	        	}
	        }
	        public void onNothingSelected(AdapterView<?> parent) {        }
	 }
}