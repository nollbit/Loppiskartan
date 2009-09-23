package se.loppiskartan.clients.android.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.NameValuePair;

import se.loppiskartan.clients.android.storage.FleaStorage;

import android.content.Context;

public class FleaLocationsGateway {
	final static int TIMEOUT_MILLIS = 15 * 1000;
	final static String URI = "http://www.loppiskartan.se/locations/search.xml?";

    private static FleaLocationsGateway instance = null;
	
    public static FleaLocationsGateway getInstance()
    {
    	if (instance == null) instance = new FleaLocationsGateway();
    	return instance;
    }
    
	public ArrayList<FleaLocation> search(String type, int radius, double lat, double lng) throws InvalidParameterException, IOException
	{
		ArrayList<FleaLocation> fleas = null;
		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		
		data.add(new BasicNameValuePair("location_type_id", type));
		data.add(new BasicNameValuePair("radius", String.valueOf(radius  / 1000))); // we use meters, loppiskartan uses km
		data.add(new BasicNameValuePair("lat", String.valueOf(lat)));
		data.add(new BasicNameValuePair("lng", String.valueOf(lng)));
		
		fleas = getLocationData(URI, data);
		
		return fleas;
	}

	public ArrayList<FleaLocation> searchByAddress(String type, int radius, String address) throws InvalidParameterException, IOException
	{
		ArrayList<FleaLocation> fleas = null;
		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		
		data.add(new BasicNameValuePair("location_type_id", type));
		data.add(new BasicNameValuePair("radius", String.valueOf(radius  / 1000))); // we use meters, loppiskartan uses km
		data.add(new BasicNameValuePair("address", address));
		
		fleas = getLocationData(URI, data);
		
		return fleas;
	}

	
	private ArrayList<FleaLocation> getLocationData(String uri, List<BasicNameValuePair> data) throws IOException
	{
		ArrayList<FleaLocation> fleas = null;
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpParams params = httpclient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_MILLIS);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT_MILLIS);

		String queryString = URLEncodedUtils.format(data, "UTF-8");
		HttpGet httpget = new HttpGet(uri + queryString); 
		
		HttpResponse response = httpclient.execute(httpget);
		
		if (response.getStatusLine().getStatusCode() != 200)
		{
			throw new IOException();
		}

		HttpEntity entity = response.getEntity();
		
		if (entity != null)
		{
			FleaLocationsXmlParser parser = new FleaLocationsXmlParser(entity.getContent()); 
			fleas = parser.parse();
		}
		
		// When HttpClient instance is no longer needed, 
		// shut down the connection manager to ensure
		// immediate deallocation of all system resources
		httpclient.getConnectionManager().shutdown();

		return fleas;        
	}
}
