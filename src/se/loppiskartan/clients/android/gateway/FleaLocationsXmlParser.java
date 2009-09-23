package se.loppiskartan.clients.android.gateway;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

public class FleaLocationsXmlParser {
	
	private static String leXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><locations><location id=\"139\">  <location_type>Loppis</location_type>  <header>Bondensloppis</header>  <link>http://www.facebook.com/group.php?gid=77926671918&amp;ref=mf</link>  <phone></phone>  <email>bondensloppis(at)gmail.com</email>  <entrance_fee>0</entrance_fee>  <rating_average>0.0</rating_average>  <coordinates lng=\"18.086472\" lat=\"59.3135568\"/>  <address city=\"Stockholm\" street=\"Bondegatan,S&#246;dermalm\"/>  <description></description>  <open_hours>    <open_periods>    </open_periods>    <open_dates>      <open_date open=\"10:00\" close=\"17:00\" date=\"2009-08-29\"/>    </open_dates>  </open_hours></location>    <location id=\"102\">    <location_type>Loppis</location_type>    <header>Konstf&#246;reningen Bellis</header>    <link></link>    <phone></phone>    <email></email>    <entrance_fee>0</entrance_fee>    <rating_average>3.5</rating_average>    <coordinates lng=\"18.136576\" lat=\"59.229243\"/>    <address city=\"Huddinge\" street=\"Spelv&#228;gen 14\"/>    <description>Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit..</description>    <open_hours>      <open_periods>        <open_period end=\"\" start=\"\">          <open_days>            <open_day open=\"18:00\" weekday=\"torsdag\" close=\"20:30\"/>            <open_day open=\"12:00\" weekday=\"s&#246;ndag\" close=\"15:00\"/>          </open_days>        </open_period>      </open_periods>      <open_dates>      </open_dates>    </open_hours>  </location>  </locations>";
	private InputStream is;

	public FleaLocationsXmlParser()
	{
		try {
			this.is = new ByteArrayInputStream(leXml.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}

	public FleaLocationsXmlParser(InputStream is)
	{
		this.is = is;
	}
	
	public ArrayList<FleaLocation> parse() {
		ArrayList<FleaLocation> locations = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            // auto-detect the encoding from the stream
            parser.setInput(this.is, null);
            int eventType = parser.getEventType();
            FleaLocation currentLocation = null;
            boolean done = false;
            while (eventType != XmlPullParser.END_DOCUMENT && !done){
                String name = null;
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                    	locations = new ArrayList<FleaLocation>();
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("location")){
                        	currentLocation = new FleaLocation();
                        	for (int i=0; i < parser.getAttributeCount();i++)
                        	{
                        		if (parser.getAttributeName(i).equalsIgnoreCase("id"))
                        		{
                        			currentLocation.id = Integer.parseInt(parser.getAttributeValue(i));
                        		}
                        	}
                        } else if (currentLocation != null){
                            if (name.equalsIgnoreCase("header")){
                            	currentLocation.title = parser.nextText();
                            } else if (name.equalsIgnoreCase("link")){
                            	currentLocation.link = parser.nextText();
                            } else if (name.equalsIgnoreCase("phone")){
                            	currentLocation.phone = parser.nextText();
                            } else if (name.equalsIgnoreCase("coordinates")){
                            	for (int i=0; i < parser.getAttributeCount();i++)
                            	{
                            		if (parser.getAttributeName(i).equalsIgnoreCase("lng"))
                            		{
                            			currentLocation.longitude = Float.parseFloat(parser.getAttributeValue(i));
                            		} else if (parser.getAttributeName(i).equalsIgnoreCase("lat"))
                            		{
                            			currentLocation.latitude = Double.parseDouble(parser.getAttributeValue(i));
                            		}
                            	}
                            } else if (name.equalsIgnoreCase("address")){
                            	for (int i=0; i < parser.getAttributeCount();i++)
                            	{
                            		if (parser.getAttributeName(i).equals("city"))
                            		{
                            			currentLocation.city = parser.getAttributeValue(i);
                            		}
                            		else if (parser.getAttributeName(i).equals("street"))
                            		{
                            			currentLocation.street = parser.getAttributeValue(i);
                            		}
                            	}
                            } else if (name.equalsIgnoreCase("description")){
                            	currentLocation.description = parser.nextText();
                            } else if (name.equalsIgnoreCase("distance")){
                            	// km to meters
                            	Double distance = Double.parseDouble(parser.nextText()) * 1000;
                            	currentLocation.distance = (distance.intValue());;
                            } else if (name.equalsIgnoreCase("email")){
                            	currentLocation.email = parser.nextText();
                            } else if (name.equalsIgnoreCase("rating_average")){
                            	currentLocation.rating = Float.parseFloat(parser.nextText());
                            }    
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("location") && 
                        		currentLocation != null){
                            locations.add(currentLocation);
                        } else if (name.equalsIgnoreCase("locations")){
                            done = true;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return locations;

    }

}
