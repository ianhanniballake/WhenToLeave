package edu.usc.csci588team02.activity;

//modified from http://code.google.com/p/j2memaprouteprovider/source/browse/#svn/trunk/J2MEMapRouteAndroidEx
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.usc.csci588team02.R;
import edu.usc.csci588team02.maps.Road;
import edu.usc.csci588team02.maps.RoadProvider;

class MapOverlay extends com.google.android.maps.Overlay
{
	ArrayList<GeoPoint> mPoints;
	Road mRoad;

	public MapOverlay(final Road road, final MapView mv)
	{
		mRoad = road;
		if (road.mRoute.length > 0)
		{
			mPoints = new ArrayList<GeoPoint>();
			System.out.println("Route Length " + road.mRoute.length);
			for (final double[] element : road.mRoute)
				mPoints.add(new GeoPoint((int) (element[1] * 1000000),
						(int) (element[0] * 1000000)));
			final int moveToLat = mPoints.get(0).getLatitudeE6()
					+ (mPoints.get(mPoints.size() - 1).getLatitudeE6() - mPoints
							.get(0).getLatitudeE6()) / 2;
			final int moveToLong = mPoints.get(0).getLongitudeE6()
					+ (mPoints.get(mPoints.size() - 1).getLongitudeE6() - mPoints
							.get(0).getLongitudeE6()) / 2;
			final GeoPoint moveTo = new GeoPoint(moveToLat, moveToLong);
			final MapController mapController = mv.getController();
			mapController.animateTo(moveTo);
			mapController.setZoom(7);
		}
	}

	@Override
	public boolean draw(final Canvas canvas, final MapView mv,
			final boolean shadow, final long when)
	{
		super.draw(canvas, mv, shadow);
		drawPath(mv, canvas);
		return true;
	}

	public void drawPath(final MapView mv, final Canvas canvas)
	{
		int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
		final Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		for (int i = 0; i < mPoints.size(); i++)
		{
			final Point point = new Point();
			mv.getProjection().toPixels(mPoints.get(i), point);
			x2 = point.x;
			y2 = point.y;
			if (i > 0)
				canvas.drawLine(x1, y1, x2, y2, paint);
			x1 = x2;
			y1 = y2;
		}
	}
}

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
			final MapOverlay mapOverlay = new MapOverlay(mRoad, mapView);
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
