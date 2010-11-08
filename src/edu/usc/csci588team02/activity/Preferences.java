package edu.usc.csci588team02.activity;

import android.app.Activity;
import android.os.Bundle;
import edu.usc.csci588team02.R;

public class Preferences extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
	}
}