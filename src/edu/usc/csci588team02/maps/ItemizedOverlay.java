package edu.usc.csci588team02.maps;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.OverlayItem;

/**
 * Manages a set of {@link OverlayItem}s, which can be added to a map. <br>
 * <br>
 * Note: This class was based on the tutorial found at:
 * <ul>
 * http://developer.android.com/guide/tutorials/views/hello-mapview.html
 * </ul>
 * 
 * @author Stephanie Trudeau
 */
public class ItemizedOverlay extends
		com.google.android.maps.ItemizedOverlay<OverlayItem>
{
	// Holds each of the OverlayItems objects that we want on our map
	private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	/**
	 * Defines the default marker to be used on each of the {@link OverlayItem}.
	 * Sets the center-point at the bottom of the image to be the point at which
	 * it's attached to the map coordinates
	 * 
	 * @param defaultMarker
	 *            Marker to be used for each {@link OverlayItem}
	 */
	public ItemizedOverlay(final Drawable defaultMarker)
	{
		super(boundCenterBottom(defaultMarker));
	}

	/**
	 * Adds new {@link OverlayItem} to our {@link ArrayList}.
	 * 
	 * @param overlay
	 *            Overlay item to be added
	 */
	public void addOverlay(final OverlayItem overlay)
	{
		mOverlays.add(overlay);
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
	protected boolean onTap(final int index)
	{
		// final Intent detailsIntent = new Intent(Map.this,
		// EventDetails.class);
		// detailsIntent.putExtra("eventUrl", eventList.get(position)
		// .getSelfLink());
		// startActivity(detailsIntent);
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
