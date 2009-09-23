package se.loppiskartan.clients.android;

import java.util.ArrayList;
import java.util.List;

import se.loppiskartan.clients.android.adapters.FleaLocationAdapter;
import se.loppiskartan.clients.android.gateway.FleaLocation;
import se.loppiskartan.clients.android.storage.FleaStorage;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LoppisResults extends ListActivity {

	ArrayList<FleaLocation> fleas;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

        fleas = FleaStorage.getInstance().getSearchResults();
        
        if (fleas == null)
        {
        	startActivity(new Intent(this, LoppisMain.class));
        }
         
        setListAdapter(
        		new FleaLocationAdapter(
        				this,
        				R.id.row_title,
        				fleas
        		)
        	);
        
        this.getListView().setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onLocationClick(position);
			}
        	
        });
        
    }

	protected void onLocationClick(int position) {
		Intent i = new Intent();
		i.setClass(this,LoppisDetails.class);
		i.putExtra("fleaId", fleas.get(position).id);
		startActivity(i);
		
	}
}
