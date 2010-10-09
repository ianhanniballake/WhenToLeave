package edu.usc.csci588team02.activity;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
	// The marker on the map
	Drawable drawable;
	//
	ItemizedOverlay itemizedOverlay;
	LinearLayout linearLayout;
	// List of all overlays on the map
	List<Overlay> mapOverlays;
	MapView mapView;
    private LocationManager lm;
    private LocationListener locationListener;

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
		drawable = getResources().getDrawable(R.drawable.icon);
		itemizedOverlay = new ItemizedOverlay(drawable);
		// Create Mexico City, Mexico
		final GeoPoint point = new GeoPoint(19240000, -99120000);
		final OverlayItem overlayitem = new OverlayItem(point, "", "");
		// Create Tokyo, Japan
		final GeoPoint point2 = new GeoPoint(35410000, 139460000);
		final OverlayItem overlayitem2 = new OverlayItem(point2, "", "");
		// Add the points to the map
		itemizedOverlay.addOverlay(overlayitem);
		itemizedOverlay.addOverlay(overlayitem2);
		mapOverlays.add(itemizedOverlay);
		
		//seattle
		final GeoPoint point3 = new GeoPoint(47607428, -122327271);
		final OverlayItem overlayitem3 = new OverlayItem(point3, "3", "3");
		itemizedOverlay.addOverlay(overlayitem3);
		
        //---use the LocationManager class to obtain GPS locations---
        lm = (LocationManager) 
            getSystemService(Context.LOCATION_SERVICE);    
        
        locationListener = new MyLocationListener();
        
        lm.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 
            0, 
            0, 
            locationListener);  
	}
	
	
    private class MyLocationListener implements LocationListener 
    {

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, 
            Bundle extras) {
            // TODO Auto-generated method stub
        }

		@Override
		public void onLocationChanged(Location location) {
            if (location != null) {
                Toast.makeText(getBaseContext(), 
                    "Location changed : Lat: " + location.getLatitude() + 
                    " Lng: " + location.getLongitude(), 
                    Toast.LENGTH_SHORT).show();
            
            
            
        		GeoPoint point = new GeoPoint((int)(location.getLatitude()*1000000), (int)(location.getLongitude()*1000000));
        		OverlayItem overlayitem = new OverlayItem(point, "", "");
        		itemizedOverlay.addOverlay(overlayitem);
            }
			
		}
    } 
}