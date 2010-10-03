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
public class ItemizedOverlay extends com.google.android.maps.ItemizedOverlay
{
	// Holds each of the OverlayItems objects that we want on our map
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	/**
	 * Defines the default marker to be used on each of the {@link OverlayItem}.
	 * Sets the center-point at the bottom of the image to be the point at which
	 * it's attached to the map coordinates
	 * 
	 * @param defaultMarker
	 *            Marker to be used for each {@link OverlayItem}
	 */
	public ItemizedOverlay(Drawable defaultMarker)
	{
		super(boundCenterBottom(defaultMarker));
	}

	/**
	 * Adds new {@link OverlayItem} to our {@link ArrayList}.
	 * 
	 * @param overlay
	 *            Overlay item to be added
	 */
	public void addOverlay(OverlayItem overlay)
	{
		mOverlays.add(overlay);
		populate();
	}

	/**
	 * Redefined to read from our {@link ArrayList}.
	 */
	@Override
	protected OverlayItem createItem(int i)
	{
		return mOverlays.get(i);
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
