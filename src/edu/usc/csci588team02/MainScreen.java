package edu.usc.csci588team02;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainScreen extends Activity
{
	/**
	 * List of countries to test out the ListView
	 */
	static final String[] COUNTRIES = new String[] { "Afghanistan", "Albania",
			"Algeria", "American Samoa", "Andorra", "Angola", "Anguilla",
			"Antarctica", "Antigua and Barbuda", "Argentina", "Armenia",
			"Aruba", "Australia", "Austria", "Azerbaijan", "Bahrain",
			"Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin",
			"Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina",
			"Botswana", "Bouvet Island", "Brazil",
			"British Indian Ocean Territory", "British Virgin Islands",
			"Brunei", "Bulgaria", "Burkina Faso", "Burundi", "Cote d'Ivoire",
			"Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands",
			"Central African Republic", "Chad", "Chile", "China",
			"Christmas Island", "Cocos (Keeling) Islands", "Colombia",
			"Comoros", "Congo", "Cook Islands", "Costa Rica", "Croatia",
			"Cuba", "Cyprus", "Czech Republic",
			"Democratic Republic of the Congo", "Denmark", "Djibouti",
			"Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt",
			"El Salvador", "Equatorial Guinea", "Eritrea", "Estonia",
			"Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji",
			"Finland", "Former Yugoslav Republic of Macedonia", "France",
			"French Guiana", "French Polynesia", "French Southern Territories",
			"Gabon", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece",
			"Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala",
			"Guinea", "Guinea-Bissau", "Guyana", "Haiti",
			"Heard Island and McDonald Islands", "Honduras", "Hong Kong",
			"Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq",
			"Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan",
			"Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos",
			"Latvia", "Lebanon", "Lesotho", "Liberia", "Libya",
			"Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Madagascar",
			"Malawi", "Malaysia", "Maldives", "Mali", "Malta",
			"Marshall Islands", "Martinique", "Mauritania", "Mauritius",
			"Mayotte", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia",
			"Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia",
			"Nauru", "Nepal", "Netherlands", "Netherlands Antilles",
			"New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria",
			"Niue", "Norfolk Island", "North Korea", "Northern Marianas",
			"Norway", "Oman", "Pakistan", "Palau", "Panama",
			"Papua New Guinea", "Paraguay", "Peru", "Philippines",
			"Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar",
			"Reunion", "Romania", "Russia", "Rwanda", "Sqo Tome and Principe",
			"Saint Helena", "Saint Kitts and Nevis", "Saint Lucia",
			"Saint Pierre and Miquelon", "Saint Vincent and the Grenadines",
			"Samoa", "San Marino", "Saudi Arabia", "Senegal", "Seychelles",
			"Sierra Leone", "Singapore", "Slovakia", "Slovenia",
			"Solomon Islands", "Somalia", "South Africa",
			"South Georgia and the South Sandwich Islands", "South Korea",
			"Spain", "Sri Lanka", "Sudan", "Suriname",
			"Svalbard and Jan Mayen", "Swaziland", "Sweden", "Switzerland",
			"Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand",
			"The Bahamas", "The Gambia", "Togo", "Tokelau", "Tonga",
			"Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan",
			"Turks and Caicos Islands", "Tuvalu", "Virgin Islands", "Uganda",
			"Ukraine", "United Arab Emirates", "United Kingdom",
			"United States", "United States Minor Outlying Islands", "Uruguay",
			"Uzbekistan", "Vanuatu", "Vatican City", "Venezuela", "Vietnam",
			"Wallis and Futuna", "Western Sahara", "Yemen", "Yugoslavia",
			"Zambia", "Zimbabwe" };
	
	private static final int MENU_VIEW_CALENDARS = 0;
	private static final int MENU_VIEW_MAPS = 1;
	
	private final DateFormat dateFormat = DateFormat
			.getDateInstance(DateFormat.SHORT);
	private final DateFormat timeFormat = DateFormat
			.getTimeInstance(DateFormat.SHORT);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final ListView mainList = (ListView) findViewById(R.id.mainList);
		mainList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id)
			{
				// Make a popup (Toast) until we have a details view activity
				Toast.makeText(getApplicationContext(),
						((TextView) view).getText(), Toast.LENGTH_SHORT).show();
			}
		});
		final Button refreshButton = (Button) findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				refreshData();
			}
		});
		refreshData();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		menu.add(0, MENU_VIEW_CALENDARS, 0, "View Calendars");
		menu.add(0, MENU_VIEW_MAPS, 0, "Map");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_VIEW_CALENDARS:
				final Intent i = new Intent(this, CalendarActivity.class);
				startActivity(i);
				return true;
			case MENU_VIEW_MAPS:
				final Intent i2 = new Intent(this, MapScreen.class);
				startActivity(i2);
				return true;
		}
		return false;
	}

	/**
	 * Refresh the data for the MainScreen activity
	 */
	public void refreshData()
	{
		// TODO Move MainScreen refresh data out of the UI thread
		// Set the last refreshed to a while refreshing text
		final TextView lastRefreshed = (TextView) findViewById(R.id.lastRefreshed);
		lastRefreshed.setText(getText(R.string.whileRefreshing));
		// Load the data
		// If this takes a while, the UI thread will be frozen
		final ListView mainList = (ListView) findViewById(R.id.mainList);
		// TODO Load real data into the MainScreen
		mainList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.main_list_item, COUNTRIES));
		// Update the last refreshed text
		final CharSequence lastRefreshedBase = getText(R.string.lastRefreshedBase);
		final Date currentDate = new Date();
		lastRefreshed.setText(lastRefreshedBase + " "
				+ dateFormat.format(currentDate) + " "
				+ timeFormat.format(currentDate));
	}
}