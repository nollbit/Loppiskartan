package se.loppiskartan.clients.android.gateway;

import java.util.HashMap;

public class FleaLocation implements Comparable<FleaLocation> {
	public final static String TYPE_ALL = "";
	public final static String TYPE_FLEE = "1";
	public final static String TYPE_AUCTION = "2";
	public final static String TYPE_ANTIQUE = "3";
	public final static String TYPE_SECONDHAND = "3";

	public int type;
	public int id;
	public String title;
	public String link;
	public double rating;
	public String description;
	public String phone;
	public String email;
	public double latitude;
	public double longitude;
	public String city;
	public String street;
	
	public int distance;

	public int compareTo(FleaLocation another) {
        if (another == null) return 1;
        // sort descending, most recent first
        return Integer.valueOf(another.distance).compareTo(Integer.valueOf(distance));
	}
}
