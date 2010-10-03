package edu.usc.csci588team02;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import edu.usc.csci588team02.maps.ItemizedOverlay;

/**
 * @author Stephanie Trudeau
 */
public class MapScreen extends MapActivity
{
	LinearLayout linearLayout;
	MapView mapView;
	
	// List of all overlays on the map
	List<Overlay> mapOverlays;
	// The marker on the map
	Drawable drawable;
	// 
	ItemizedOverlay itemizedOverlay;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_map);
		
		// Add the ability to zoom in and out on the map
		this.mapView = (MapView)findViewById(R.id.mapview);
		this.mapView.setBuiltInZoomControls(true);
		
		// Initialize overlay variables
		this.mapOverlays = this.mapView.getOverlays();
		this.drawable = this.getResources().getDrawable(R.drawable.icon);
		this.itemizedOverlay = new ItemizedOverlay(this.drawable);
		
		// Create Mexico City, Mexico
		GeoPoint point = new GeoPoint(19240000,-99120000);
		OverlayItem overlayitem = new OverlayItem(point, "", "");
		
		// Create Tokyo, Japan
		GeoPoint point2 = new GeoPoint(35410000, 139460000);
		OverlayItem overlayitem2 = new OverlayItem(point2, "", "");
		
		// Add the points to the map
		this.itemizedOverlay.addOverlay(overlayitem);
		this.itemizedOverlay.addOverlay(overlayitem2);
		this.mapOverlays.add(this.itemizedOverlay);
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
}