package com.github.whentoleave.maps;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.github.whentoleave.ui.EventDetailsFragment;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * Manages a set of {@link OverlayItem}s, which can be added to a map. <br />
 * <br />
 * Note: This class was based on the tutorial found at: <a href=
 * "http://developer.android.com/guide/tutorials/views/hello-mapview.html"</a>
 */
public class ItemizedOverlay extends
		com.google.android.maps.ItemizedOverlay<OverlayItem>
{
	/**
	 * Context to launch activities
	 */
	private final Context mContext;
	/**
	 * Event URLs corresponding with each OverlayItem
	 */
	private final ArrayList<Long> mEventIds = new ArrayList<Long>();
	/**
	 * Holds each of the OverlayItems objects that we want on our map
	 */
	private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	/**
	 * Defines the default marker to be used on each of the {@link OverlayItem}.
	 * Sets the center-point at the bottom of the image to be the point at which
	 * it's attached to the map coordinates
	 * 
	 * @param defaultMarker
	 *            Marker to be used for each {@link OverlayItem}
	 * @param context
	 *            context to launch activities from
	 */
	public ItemizedOverlay(final Drawable defaultMarker, final Context context)
	{
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		populate();
	}

	/**
	 * Adds new {@link OverlayItem} to our {@link ArrayList}.
	 * 
	 * @param overlay
	 *            Overlay item to be added
	 * @param eventId
	 *            A unique ID specific to this event
	 */
	public void addOverlay(final OverlayItem overlay, final long eventId)
	{
		mOverlays.add(overlay);
		mEventIds.add(eventId);
		populate();
	}

	/**
	 * Clears this ItemizedOverlay
	 */
	public void clearOverlay()
	{
		mOverlays.clear();
		mEventIds.clear();
		populate();
	}

	/**
	 * Redefined to read from our {@link ArrayList}.
	 */
	@Override
	protected OverlayItem createItem(final int i)
	{
		return mOverlays.get(i);
	}

	@Override
	public void draw(final Canvas pCanvas, final MapView pMapView,
			final boolean pShadow)
	{
		super.draw(pCanvas, pMapView, false);
	}

	/**
	 * Launches the EventDetails activity for the tapped event
	 */
	@Override
	protected boolean onTap(final int index)
	{
		final Intent detailsIntent = new Intent(mContext,
				EventDetailsFragment.class);
		if (mEventIds.get(index) != -1)
		{
			detailsIntent.putExtra("eventId", mEventIds.get(index));
			mContext.startActivity(detailsIntent);
		}
		return true;
	}

	/**
	 * Sets the Drawable marker for an {@link OverlayItem} i.
	 * 
	 * @param i
	 *            Overlay item index
	 * @param marker
	 *            new marker drawable
	 */
	public void setMarker(final int i, final Drawable marker)
	{
		mOverlays.get(i).setMarker(marker);
		populate();
	}

	/**
	 * Redefined to request the size of our {@link ArrayList}.
	 */
	@Override
	public int size()
	{
		return mOverlays.size();
	}
}
