package edu.usc.csci588team02.maps;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class MapRouteOverlay extends com.google.android.maps.Overlay
{
	ArrayList<GeoPoint> mPoints;
	Road mRoad;

	public MapRouteOverlay(final Road road, final MapView mv)
	{
		mRoad = road;
		if (road.mRoute.length > 0)
		{
			mPoints = new ArrayList<GeoPoint>();
			System.out.println("Route Length " + road.mRoute.length);
			for (final double[] element : road.mRoute)
				mPoints.add(new GeoPoint((int) (element[1] * 1000000),
						(int) (element[0] * 1000000)));
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
		if (mRoad.mRoute.length > 0)
		{
			int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
			final Paint paint = new Paint();
			paint.setColor(0xff48b4fe); //@color/blue with extra 0xff for alpha
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(4);
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
}