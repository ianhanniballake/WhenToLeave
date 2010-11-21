package edu.usc.csci588team02.activity;

//modified from http://code.google.com/p/j2memaprouteprovider/source/browse/#svn/trunk/J2MEMapRouteAndroidEx
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.maps.Road;
import edu.usc.csci588team02.maps.RoadProvider;
import edu.usc.csci588team02.maps.MapRouteOverlay;

public class MapRouteActivity extends MapActivity
{
	LinearLayout linearLayout;
	MapView mapView;
	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(final android.os.Message msg)
		{
			final TextView textView = (TextView) findViewById(R.id.mapdescription);
			textView.setText(mRoad.mName + " " + mRoad.mDescription);
			System.out.println(mRoad.mName + " " + mRoad.mDescription);
			final MapRouteOverlay mapOverlay = new MapRouteOverlay(mRoad, mapView);
			final List<Overlay> listOfOverlays = mapView.getOverlays();
			listOfOverlays.clear();
			listOfOverlays.add(mapOverlay);
			mapView.invalidate();
		}
	};
	private Road mRoad;

	private InputStream getConnection(final String url)
	{
		InputStream is = null;
		try
		{
			final URLConnection conn = new URL(url).openConnection();
			is = conn.getInputStream();
		} catch (final MalformedURLException e)
		{
			e.printStackTrace();
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
		return is;
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		new Thread()
		{
			@Override
			public void run()
			{
				// double fromLat = 49.85, fromLon = 24.016667, toLat = 50.45,
				// toLon = 30.523333;
				// String url = RoadProvider.getUrlFromLatLong(fromLat, fromLon,
				// toLat, toLon);
				final String url = RoadProvider.getUrlFromString("seattle",
						"portland");
				System.out.println(url);
				final InputStream is = getConnection(url);
				mRoad = RoadProvider.getRoute(is);
				mHandler.sendEmptyMessage(0);
			}
		}.start();
	}
}
