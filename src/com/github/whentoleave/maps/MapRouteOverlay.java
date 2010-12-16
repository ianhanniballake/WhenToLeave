package com.github.whentoleave.maps;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Overlay representing a route to a destination
 */
public class MapRouteOverlay extends Overlay
{
	/**
	 * The route to draw
	 */
	private Route mRoute = null;

	/**
	 * Base constructor for a null route
	 */
	public MapRouteOverlay()
	{
	}

	/**
	 * Basic constructor for drawing a given route onto a Google Map
	 * 
	 * @param route
	 *            The route to draw
	 */
	public MapRouteOverlay(final Route route)
	{
		mRoute = route;
	}

	/**
	 * Draws the route
	 */
	@Override
	public boolean draw(final Canvas canvas, final MapView mv,
			final boolean shadow, final long when)
	{
		super.draw(canvas, mv, shadow);
		if (mRoute != null && !mRoute.mRoute.isEmpty())
		{
			int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
			final Paint paint = new Paint();
			paint.setColor(0xff48b4fe); // @color/blue with extra 0xff for alpha
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(4);
			for (int i = 0; i < mRoute.mRoute.size(); i++)
			{
				final Point point = new Point();
				mv.getProjection().toPixels(mRoute.mRoute.get(i), point);
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

	/**
	 * Set the route to be drawn
	 * 
	 * @param route
	 *            route to draw
	 */
	public void setRoute(final Route route)
	{
		mRoute = route;
	}
}