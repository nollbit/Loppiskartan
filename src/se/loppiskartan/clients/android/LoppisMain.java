package se.loppiskartan.clients.android;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import se.loppiskartan.clients.android.adapters.Distance;
import se.loppiskartan.clients.android.adapters.Type;
import se.loppiskartan.clients.android.gateway.FleaLocation;
import se.loppiskartan.clients.android.gateway.FleaLocationsXmlParser;
import se.loppiskartan.clients.android.gateway.FleaLocationsGateway;
import se.loppiskartan.clients.android.storage.ApiKeyStorage;
import se.loppiskartan.clients.android.storage.FleaStorage;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class LoppisMain extends Activity {
	final static int MENU_SEARCH_BY_ADDRESS = R.id.main_menu_search_by_address;
	final static int MENU_ABOUT = R.id.main_menu_about;
	
	final static int DIALOG_PROGRESS = 1;
	final static int DIALOG_SERVER_ERROR = 2;
	final static int DIALOG_ENTER_ADDRESS = 3;
	final static int DIALOG_NO_SUCH_ADDRESS = 4;
	
	final static String PREFS_NAME = "prefs";
	final static String PREF_RADIUS = "radius";
	final static String PREF_TYPE = "type";
	
	private final Handler mHandler = new Handler();
	private Spinner typeSpinner;
	private Spinner radiusSpinner;
	
	private SharedPreferences sharedPrefs;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        LinearLayout mainVerticalLayout = (LinearLayout)findViewById(R.id.main_vertical_layout);
        
        if (getResources().getConfiguration().orientation == 
        	Configuration.ORIENTATION_LANDSCAPE )
        {
        	mainVerticalLayout.setOrientation(LinearLayout.HORIZONTAL);
        }
        else
        {
        	mainVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        }
        
        sharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        
        
        ArrayList<Type<String>> types = getTypes();
        typeSpinner = (Spinner) findViewById(R.id.main_type_spinner);
        ArrayAdapter<Type<String>> typeAdapter = new ArrayAdapter<Type<String>>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
        typeSpinner.setAdapter(typeAdapter); 
        typeSpinner.setSelection(getTypePositionByType(types, sharedPrefs.getString(PREF_TYPE, "")));
        
        ArrayList<Distance> distances = new ArrayList<Distance>();
        int[] radii = getResources().getIntArray(R.array.find_radius);
        for(int i=0;i<radii.length;i++)
        {
        	distances.add(new Distance(radii[i]));
        	
        }
        radiusSpinner = (Spinner) findViewById(R.id.main_radius_spinner);
        ArrayAdapter<Distance> radiusAdapter = new ArrayAdapter<Distance>(this, android.R.layout.simple_spinner_item, distances);
        radiusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
        radiusSpinner.setAdapter(radiusAdapter);
        radiusSpinner.setSelection(getRadiusPositionByRadius(distances, sharedPrefs.getInt(PREF_RADIUS, 1)));
        
        Button search = (Button) findViewById(R.id.main_search_button);
        search.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				doSearch();
			}
        	
        });
    }

	private int getRadiusPositionByRadius(ArrayList<Distance> distances,
			int radius) {
		for (int i=0;i<distances.size();i++)
		{
			if (distances.get(i).getDistance() == radius)
			{
				return i;
			}
		}
		return 0;
	}

	private int getTypePositionByType(ArrayList<Type<String>> types, String type) {
		for(int i=0;i<types.size();i++)
		{
			if (types.get(i).getType() == type)
			{
				return i;
			}
		}
		return 0;
	}

	private ArrayList<Type<String>> getTypes() {
		ArrayList<Type<String>> types = new ArrayList<Type<String>>();
        types.add(new Type<String>(FleaLocation.TYPE_ALL, getText(R.string.flea_type_all).toString()));
        types.add(new Type<String>(FleaLocation.TYPE_FLEE, getText(R.string.flea_type_flea).toString()));
        types.add(new Type<String>(FleaLocation.TYPE_AUCTION, getText(R.string.flea_type_auction).toString()));
        types.add(new Type<String>(FleaLocation.TYPE_ANTIQUE, getText(R.string.flea_type_antique).toString()));
        types.add(new Type<String>(FleaLocation.TYPE_SECONDHAND, getText(R.string.flea_type_secondhand).toString()));
		return types;
	}

	protected void savePreferences() {
	      sharedPrefs = getSharedPreferences(PREFS_NAME, 0);
	      SharedPreferences.Editor editor = sharedPrefs.edit();
	      editor.putString(PREF_TYPE, getCurrentType());
	      editor.putInt(PREF_RADIUS, getCurrentRadius());
	      editor.commit();
	}

	@Override
	protected void onStop() {
		super.onStop();
		savePreferences();
	}

	private void doSearch()
	{
		Location loc = getLocation();

		if (loc != null)
		{
			doSearchByLocation(loc);
		}
		else
		{
			showDialog(DIALOG_ENTER_ADDRESS);
		}
	}
	
	protected void doSearchByLocation(final Location loc) {

		final String type = getCurrentType();
		final int radius = getCurrentRadius();

		showDialog(DIALOG_PROGRESS);

		new Thread() {
            public void run() {
                try {
                	final ArrayList<FleaLocation> flees = FleaLocationsGateway.getInstance().search(type, radius, loc.getLatitude(), loc.getLongitude());
                    mHandler.post(new Runnable() {
                        public void run() {
                            onSearchResults(flees);
                        }
                    });
                } catch (final Exception e) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            onSearchError(e);
                        }
                    });
                }
            }
        }.start();		
	}

	protected void showAddressAlternatives(String address) {
        showDialog(DIALOG_PROGRESS);

        Geocoder geoCoder = new Geocoder(this, new Locale("sv", "SE"));
		final List<Address> addresses;
		try {
			addresses = geoCoder.getFromLocationName(address + ", Sweden", 15);
		} catch (IOException e) {
	        dismissDialog(DIALOG_PROGRESS);
			showDialog(DIALOG_SERVER_ERROR);
			return;
		}
		
		if(addresses.size() == 0) {
	        dismissDialog(DIALOG_PROGRESS);
			showDialog(DIALOG_NO_SUCH_ADDRESS);
			return;
		}

		for(int i=0;i<addresses.size();i++) {
			if (!addresses.get(i).hasLatitude() || !addresses.get(i).hasLongitude()) {
				addresses.remove(i);
			}
		}

		final CharSequence[] charAddresses = new CharSequence[addresses.size()];
		for(int i=0;i<addresses.size();i++) {
			Address currentAddress = addresses.get(i);
			StringBuilder stringAddress = new StringBuilder();
			for(int u=0;u<=currentAddress.getMaxAddressLineIndex();u++) {
				if (u > 0) {
					stringAddress.append(", ");
				}
				stringAddress.append(currentAddress.getAddressLine(u));
			}
			charAddresses[i] = stringAddress.toString();
			Log.e("hej", currentAddress.toString());
		}
        dismissDialog(DIALOG_PROGRESS);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getText(R.string.choose_address));
		builder.setItems(charAddresses, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	Location loc = new Location("geocoder");
		    	loc.setLatitude(addresses.get(item).getLatitude());
		    	loc.setLongitude(addresses.get(item).getLongitude());
		    	doSearchByLocation(loc);
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
		
	}
	
	protected void doSearchByAddress(final Address address) {

		if (address == null) {
			return;
		}
		
		final String type = getCurrentType();
		final int radius = getCurrentRadius();

		showDialog(DIALOG_PROGRESS);

		new Thread() {
            public void run() {
                try {
                	final ArrayList<FleaLocation> flees = FleaLocationsGateway.getInstance().searchByAddress(type, radius, address.toString());
                    mHandler.post(new Runnable() {
                        public void run() {
                            onSearchResults(flees);
                        }
                    });
                } catch (final Exception e) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            onSearchError(e);
                        }
                    });
                }
            }
        }.start();		
	}
	
	private int getCurrentRadius() {
		return ((Distance)radiusSpinner.getSelectedItem()).getDistance();
	}

	private String getCurrentType() {
		return ((Type<String>)typeSpinner.getSelectedItem()).getType();
	}

	protected void onSearchError(Throwable e) {
        dismissDialog(DIALOG_PROGRESS);
		showDialog(DIALOG_SERVER_ERROR);
	}

	private void onSearchResults(ArrayList<FleaLocation> flees)
	{
        dismissDialog(DIALOG_PROGRESS);
		FleaStorage.getInstance().setSearchResults(flees);
		
		Intent i = new Intent();
		i.setClass(this, LoppisResults.class);
		startActivity(i);
	}
	
	private Location getLocation() {
		LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		Location loc = null;

		boolean hasGps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean hasNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		if(hasGps && hasNetwork)
		{
			Location locGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Location locNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (locGps.getTime() > locNetwork.getTime())
			{
				loc = locGps;
			}
			else
			{
				loc = locNetwork;
			}
		}
		else if(hasNetwork)
		{
			loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		else if(hasGps)
		{
			loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		
		
		// return null if location fix is less than one hour
		if (loc != null)
		{
			if ((loc.getTime() + 1 * 1000 * 60 * 60) < Calendar.getInstance().getTimeInMillis()) {
				loc = null;
			}
		}
		
		// dummy data
		if (loc == null && false)
		{
			loc = new Location("mock");
			loc.setLatitude(59.3135568);
			loc.setLongitude(18.086472);
		}
		return loc;
	}
	
	protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {
	        case DIALOG_PROGRESS:
	    		ProgressDialog progress = new ProgressDialog(this);
	    		progress.setMessage(getText(R.string.searching));
	    		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    		dialog = progress;
	    		break;
		    case DIALOG_SERVER_ERROR:
	            AlertDialog.Builder errorBuilder = new AlertDialog.Builder(this);
	            dialog = errorBuilder.setTitle(getText(R.string.error))
	                .setMessage(getText(R.string.error_server))
	                .setCancelable(true)
	                .setNeutralButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int id) {
	                        dialog.cancel();
	                   }
	                }).create();
	            break;
		    case DIALOG_NO_SUCH_ADDRESS:
	            AlertDialog.Builder noSuchBuilder = new AlertDialog.Builder(this);
	            dialog = noSuchBuilder.setTitle(getText(R.string.error))
	                .setMessage(getText(R.string.no_such_address))
	                .setCancelable(true)
	                .setNeutralButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int id) {
	                        dialog.cancel();
	                   }
	                }).create();
	            break;

	            
		    case DIALOG_ENTER_ADDRESS:
	            AlertDialog.Builder addressBuilder = new AlertDialog.Builder(this);
	            final EditText addressInput = new EditText(this);
	            dialog = addressBuilder.setTitle(getText(R.string.search))
	                .setMessage(getText(R.string.enter_address))
	                .setView(addressInput)
	                .setCancelable(true)
	                .setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int id) {
	                        dialog.cancel();
	                   }
	                })
	                .setPositiveButton(getText(R.string.search), new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int id) {
	                        dialog.cancel();
	                        showAddressAlternatives(addressInput.getText().toString());
	                    }
	                })
	                
	                .create();	    
	            
	    }
        return dialog;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_options, menu);
	    return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case MENU_SEARCH_BY_ADDRESS:
				showDialog(DIALOG_ENTER_ADDRESS);
				return true;
			case MENU_ABOUT:
				startActivity(new Intent(this, LoppisAbout.class));
				return true;
		}
		return false;
	}
	
	
}