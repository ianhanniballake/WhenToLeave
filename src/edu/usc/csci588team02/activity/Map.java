package edu.usc.csci588team02.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.maps.ItemizedOverlay;

/**
 * @author Stephanie Trudeau
 */
public class Map extends MapActivity
{
	private class MyLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(final Location location)
		{
			if (location != null)
			{
				Toast.makeText(
						getBaseContext(),
						"Location changed : Lat: " + location.getLatitude()
								+ " Lng: " + location.getLongitude(),
						Toast.LENGTH_SHORT).show();
				final GeoPoint point = new GeoPoint(
						(int) (location.getLatitude() * 1000000),
						(int) (location.getLongitude() * 1000000));
				final OverlayItem overlayitem = new OverlayItem(point, "", "");
				itemizedOverlay.addOverlay(overlayitem);
			}
		}

		@Override
		public void onProviderDisabled(final String provider)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(final String provider)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(final String provider, final int status,
				final Bundle extras)
		{
			// TODO Auto-generated method stub
		}
	}

	private static final int MENU_LOGOUT = 1;
	private static final int MENU_PREFERENCES = 2;
	// The marker on the map
	Drawable drawable;
	Drawable drawable1;
	Drawable drawable2;
	//
	ItemizedOverlay itemizedOverlay;
	LinearLayout linearLayout;
	private LocationManager lm;
	private LocationListener locationListener;
	// List of all overlays on the map
	List<Overlay> mapOverlays;
	MapView mapView;

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		// Add the ability to zoom in and out on the map
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		// Initialize overlay variables
		mapOverlays = mapView.getOverlays();
		drawable = getResources().getDrawable(R.drawable.ic_grey_square);
		drawable1 = getResources().getDrawable(R.drawable.ic_red_square_1);
		drawable1.setBounds(0, 0, 36, 36);
		drawable2 = getResources().getDrawable(R.drawable.ic_grey_square_2);
		drawable2.setBounds(0, 0, 36, 36);
		itemizedOverlay = new ItemizedOverlay(drawable);
		// Create Mexico City, Mexico
		final GeoPoint point = new GeoPoint(19240000, -99120000);
		final OverlayItem overlayitem = new OverlayItem(point, "1",
				"Appointment 1");
		overlayitem.setMarker(drawable1);
		// Create Tokyo, Japan
		final GeoPoint point2 = new GeoPoint(35410000, 139460000);
		final OverlayItem overlayitem2 = new OverlayItem(point2, "2",
				"Appointment 2");
		// overlayitem2.setMarker(drawable2);
		// Add the points to the map
		itemizedOverlay.addOverlay(overlayitem);
		itemizedOverlay.addOverlay(overlayitem2);
		mapOverlays.add(itemizedOverlay);
		// Seattle
		final GeoPoint point3 = new GeoPoint(47607428, -122327271);
		final OverlayItem overlayitem3 = new OverlayItem(point3, "3", "3");
		itemizedOverlay.addOverlay(overlayitem3);
		overlayitem3.setMarker(drawable2);
		// ---use the LocationManager class to obtain GPS locations---
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyLocationListener();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				locationListener);
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