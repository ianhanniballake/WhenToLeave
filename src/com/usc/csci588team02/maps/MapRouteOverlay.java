package edu.usc.csci588team02.maps;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Overlay representing a route to a destination
 */
public class MapRouteOverlay extends Overlay
{
	/**
	 * Logging tag
	 */
	private static final String TAG = "MapRouteOverlay";
	/**
	 * The route to draw
	 */
	private final Road mRoad;

	/**
	 * Basic constructor for drawing a given road onto a Google Map
	 * 
	 * @param road
	 *            The route to draw
	 */
	public MapRouteOverlay(final Road road)
	{
		mRoad = road;
		Log.v(TAG, "Route Length " + road.mRoute.size());
	}

	/**
	 * Draws the route
	 */
	@Override
	public boolean draw(final Canvas canvas, final MapView mv,
			final boolean shadow, final long when)
	{
		super.draw(canvas, mv, shadow);
		if (!mRoad.mRoute.isEmpty())
		{
			int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
			final Paint paint = new Paint();
			paint.setColor(0xff48b4fe); // @color/blue with extra 0xff for alpha
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(4);
			for (int i = 0; i < mRoad.mRoute.size(); i++)
			{
				final Point point = new Point();
				mv.getProjection().toPixels(mRoad.mRoute.get(i), point);
				x2 = point.x;
				y2 = point.y;
				if (i > 0)
					canvas.drawLine(x1, y1, x2, y2, paint);
				x1 = x2;
				y1 = y2;
			}
		}
		return true;
	}
}