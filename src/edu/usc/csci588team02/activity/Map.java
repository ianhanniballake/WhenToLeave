package edu.usc.csci588team02.activity;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.LinearLayout;

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
	}
}