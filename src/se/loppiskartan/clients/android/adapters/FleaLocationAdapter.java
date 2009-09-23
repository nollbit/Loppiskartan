package se.loppiskartan.clients.android.adapters;

import java.util.ArrayList;

import se.loppiskartan.clients.android.R;
import se.loppiskartan.clients.android.gateway.FleaLocation;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class FleaLocationAdapter extends ArrayAdapter<FleaLocation> {

    private ArrayList<FleaLocation> items;
	private Context context;

    public FleaLocationAdapter(Context context, int textViewResourceId, ArrayList<FleaLocation> items) {
            super(context, textViewResourceId, items);
            this.context = context;
            this.items = items;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.result_row, null);
            }
            FleaLocation o = items.get(position);
            if (o != null) {
                    TextView textTitle = (TextView) v.findViewById(R.id.row_title);
                    TextView textDistance = (TextView) v.findViewById(R.id.row_distance);
                    TextView textCity = (TextView) v.findViewById(R.id.row_city);
                   // RatingBar ratingBar = (RatingBar) v.findViewById(R.id.row_ratingbar);
                    
                    //ratingBar.setRating((float)o.rating);
                    
                    if (textTitle != null) {
                    	textTitle.setText(o.title);
                    }
                    if(textDistance != null) {
                    	textDistance.setText(getDistanceInWords(o.distance));
                    }
                    if(textCity != null) {
                    	textCity.setText(o.city);
                    }
            }
            return v;
    }

	private CharSequence getDistanceInWords(int distance) {
		return Distance.getDistanceInWords(distance);
	}
}
