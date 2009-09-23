package se.loppiskartan.clients.android.overlays;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class FleaItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
	
	public FleaItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public void addOverlay(OverlayItem overlay) {
		overlayItems.add(overlay);
	    populate();
	}

	@Override
	protected OverlayItem createItem(int position) {
		return overlayItems.get(position);
	}

	@Override
	public int size() {
		return overlayItems.size();
	}

}
