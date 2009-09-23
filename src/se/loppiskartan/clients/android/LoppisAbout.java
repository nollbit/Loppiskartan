package se.loppiskartan.clients.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LoppisAbout extends Activity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
     
        TextView link = (TextView)findViewById(R.id.about_link_text);
        SpannableString str = SpannableString.valueOf(link.getText()); 
        Linkify.addLinks(str, Linkify.ALL);
        link.setText(str, TextView.BufferType.SPANNABLE); 
        link.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				final Uri uri = Uri.parse("http://www.loppiskartan.se");
				Intent i = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(i);
			}
        	
        });
    }
}
