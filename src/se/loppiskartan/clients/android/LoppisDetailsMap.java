package se.loppiskartan.clients.android;

import java.util.List;

import se.loppiskartan.clients.android.gateway.FleaLocation;
import se.loppiskartan.clients.android.overlays.FleaItemizedOverlay;
import se.loppiskartan.clients.android.storage.FleaStorage;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class LoppisDetailsMap extends MapActivity {

	public static final String MAP_EXTRA_FLEA_ID = "flea_id";

	public static final String MAP_EXTRA_LONG = "long";
	public static final String MAP_EXTRA_LAT = "lat";
	
	public static final String MAP_EXTRA_DISTANCE = "distance";

	private static final int OPTIONS_MENU_SHOW_ON_MAP = R.id.details_map_menu_show_on_map;
	private static final int OPTIONS_MENU_SHOW_MY_LOCATION = R.id.details_map_menu_show_my_location;
	
	private MyLocationOverlay myLocationOverlay;
	
	private List<Overlay> mapOverlays;
	private Drawable drawable;
	private FleaItemizedOverlay itemizedOverlay;
	private LinearLayout linearLayout;
	private MapView mapView;
	private ZoomControls mZoom;
	
	private GeoPoint fleaPoint;
	
	public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.details_map);

      if (!this.getIntent().hasExtra(MAP_EXTRA_FLEA_ID))
      {
      	startActivity(new Intent(this, LoppisMain.class));
      }

     
      mapView = (MapView)findViewById(R.id.details_map_mapview);
      linearLayout = (LinearLayout) findViewById(R.id.details_map_zoomview);
      mZoom = (ZoomControls) mapView.getZoomControls();
      linearLayout.addView(mZoom);      
      
      int fleaId = getIntent().getIntExtra(MAP_EXTRA_FLEA_ID, -1);
      FleaLocation flea = FleaStorage.getInstance().getFleaById(fleaId);
      
      Double lngMicroDegrees = flea.longitude * 1e6;
      Double latMicroDegrees = flea.latitude * 1e6;
      
      fleaPoint = new GeoPoint(latMicroDegrees.intValue(), lngMicroDegrees.intValue());

      drawable = this.getResources().getDrawable(R.drawable.loppis_icon);
      itemizedOverlay = new FleaItemizedOverlay(drawable);
      itemizedOverlay.addOverlay(new OverlayItem(fleaPoint, flea.title, flea.description));
      mapView.getOverlays().add(itemizedOverlay);

      myLocationOverlay = new MyLocationOverlay(this, mapView);
      mapView.getOverlays().add(myLocationOverlay);
      
      ((TextView)findViewById(R.id.details_map_title)).setText(flea.title);
      

      
      centerOnFlea();

      mapView.getController().setZoom(getZoomLevelByDistance(flea.distance));

    }

	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
		centerOnMe();
	}

	private int getZoomLevelByDistance(int distance) {
		if (distance < 3000)
		{
			return 17;
		}
		else if (distance < 6000)
		{
			return 15;
		}
		else if (distance < 15000)
		{
			return 12;
		}
		return 10;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.details_map_options, menu);

	    // disable "center on me" if no location fix
	    if (!hasLocationFix())
	    {
	    	((MenuItem)menu.findItem(OPTIONS_MENU_SHOW_MY_LOCATION)).setEnabled(false);
	    }
	    
	    return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case OPTIONS_MENU_SHOW_ON_MAP:
				centerOnFlea();
				return true;
			case OPTIONS_MENU_SHOW_MY_LOCATION:
				centerOnMe();
				return true;
		}
		return false;
	}

	private boolean hasLocationFix() {
		return (myLocationOverlay.getMyLocation() != null);
	}

	private void centerOnMe() {
		if (hasLocationFix())
		{
			mapView.getController().animateTo(myLocationOverlay.getMyLocation());
		}
	}

	private void centerOnFlea() {
		mapView.getController().animateTo(fleaPoint);
	}


}
