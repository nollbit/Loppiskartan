package se.loppiskartan.clients.android;

import java.net.URI;

import se.loppiskartan.clients.android.gateway.FleaLocation;
import se.loppiskartan.clients.android.storage.FleaStorage;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class LoppisDetails extends Activity {
	
	final static String EXTRA_FLEA_ID = "fleaId";
	final static String EXTRA_MY_LAT = "my_lat";
	final static String EXTRA_MY_LONG = "my_long";

	final static int OPTIONS_MENU_SHOW_ON_MAP = R.id.details_menu_show_on_map;
	final static int OPTIONS_MENU_CALL = R.id.details_menu_call;
	final static int OPTIONS_MENU_SEND_EMAIL = R.id.details_menu_send_email;
	final static int OPTIONS_MENU_OPEN_WEB = R.id.details_menu_open_web;
	
	FleaLocation flea;
	boolean fleaHasLink = false;
	boolean fleaHasPhone = false;
	boolean fleaHasEmail = false;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);
        
        if (!this.getIntent().hasExtra(EXTRA_FLEA_ID))
        {
        	startActivity(new Intent(this, LoppisMain.class));
        }
        
        int fleaId = this.getIntent().getIntExtra(EXTRA_FLEA_ID, -1);
        flea = FleaStorage.getInstance().getFleaById(fleaId);
        
    	TextView detailsLink = ((TextView)findViewById(R.id.details_link));
    	TextView detailsPhone = ((TextView)findViewById(R.id.details_phone));
    	TextView detailsEmail = ((TextView)findViewById(R.id.details_email));

    	if (flea.link.length() > 0)
    		fleaHasLink = true;

    	if (flea.phone.length() > 0)
    		fleaHasPhone = true;

    	if (flea.email.length() > 0)
    		fleaHasEmail = true;
    	
        ((TextView)findViewById(R.id.details_title)).setText(flea.title);
        ((TextView)findViewById(R.id.details_description)).setText(flea.description);

        
        if (fleaHasLink)
        {
        	// valid uri?
        	try {
        		detailsLink.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
		        		openWebPage();
					}
        		});
        	}
        	catch(Exception e)
        	{}
        	detailsLink.setText(flea.link);
        }
        else
        {
        	detailsLink.setText(getText(R.string.flea_details_na));
        }
        
        
        
        
        if (fleaHasEmail)
        {
        	try {
        		detailsEmail.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						sendEmail();
					}
        		});
        	}
        	catch(Exception e)
        	{}
        	detailsEmail.setText(flea.email.replaceAll("\\(at\\)", "@"));
        }
        else
        {
        	detailsEmail.setText(getText(R.string.flea_details_na));
        }

        if (fleaHasPhone)
        {
        	try {
        		detailsPhone.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
		        		call();
					}
        			
        		});
        	}
        	catch(Exception e)
        	{}
        	detailsPhone.setText(flea.phone);
        }
        else
        {
        	detailsPhone.setText(getText(R.string.flea_details_na));
        }

        
        // add handler for address + city on click
        OnClickListener addressOnClickListener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				showOnMap();
			}
        	
        };

        if (flea.city.length() > 0)
        {
        	((TextView)findViewById(R.id.details_city)).setOnClickListener(addressOnClickListener);
        	((TextView)findViewById(R.id.details_city)).setText(flea.city);
        }
        else
        {
        	((TextView)findViewById(R.id.details_city)).setText(getText(R.string.flea_details_na));
        }

        if (flea.street.length() > 0)
        {
        	((TextView)findViewById(R.id.details_street)).setOnClickListener(addressOnClickListener);
        	((TextView)findViewById(R.id.details_street)).setText(flea.street);
        }
        else
        {
        	((TextView)findViewById(R.id.details_street)).setText(getText(R.string.flea_details_na));
        }

        
        
        ((RatingBar)findViewById(R.id.details_rating)).setRating((float)flea.rating);
    }
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.details_options, menu);
	    
    	MenuItem callMenuItem = (MenuItem)menu.findItem(OPTIONS_MENU_CALL);
    	MenuItem emailMenuItem = (MenuItem)menu.findItem(OPTIONS_MENU_SEND_EMAIL);
    	MenuItem openWebMenuItem = (MenuItem)menu.findItem(OPTIONS_MENU_OPEN_WEB);

    	callMenuItem.setEnabled(true);
    	emailMenuItem.setEnabled(true);
    	openWebMenuItem.setEnabled(true);
    	
    	if (!fleaHasLink)
    	{
        	openWebMenuItem.setEnabled(false);
    	}
    	if (!fleaHasPhone)
    	{
        	callMenuItem.setEnabled(false);
    	}
    	if (!fleaHasEmail)
    	{
        	emailMenuItem.setEnabled(false);
    	}
	    
	    return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case OPTIONS_MENU_SHOW_ON_MAP:
				showOnMap();
				return true;
			case OPTIONS_MENU_CALL:
				call();
				return true;
			case OPTIONS_MENU_SEND_EMAIL:
				sendEmail();
				return true;
			case OPTIONS_MENU_OPEN_WEB:
				openWebPage();
				return true;
				
		}
		return false;
	}
	
	private void sendEmail() {
		Intent i = new Intent(Intent.ACTION_SEND);
		String[] r = {flea.email};
		i.putExtra(Intent.EXTRA_EMAIL, r);
		i.putExtra(Intent.EXTRA_SUBJECT, flea.title);
		startActivity(Intent.createChooser(i, getText(R.string.choose_email)));
	}

	private void showOnMap() {
		Intent i = new Intent(this, LoppisDetailsMap.class);
		i.putExtra(LoppisDetailsMap.MAP_EXTRA_FLEA_ID, flea.id);
		startActivity(i);
	}

	private void call() {
		final Uri uri = Uri.parse("tel:" + flea.phone);
		Intent i = new Intent(Intent.ACTION_DIAL, uri);
		startActivity(i);
	}

	private void openWebPage() {
		final Uri uri = Uri.parse(flea.link);
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(i);
	}

}
