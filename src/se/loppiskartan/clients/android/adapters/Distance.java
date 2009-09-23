package se.loppiskartan.clients.android.adapters;

public class Distance {

	private static String[] metricLengthUnits = {"meter", "km", "mil"};
	private static double[] metricLengthUnitsMagnitudes = {3, 1, 1};
	
	private int distance;
	private String distanceInWords;
	
	public Distance(int distance)
	{
		this.distance = distance;
		this.distanceInWords = getDistanceInWords(distance);
	}
	
	public static String getDistanceInWords(int distance) {
		String distanceInWords = null;
		double realDistance = distance;
		
		for(int i=0;i<metricLengthUnits.length && distanceInWords == null;i++)
		{
			boolean lastOne = (i == metricLengthUnits.length-1);
			int currentUnitSize = (int)Math.pow(10, metricLengthUnitsMagnitudes[i]);
			if (realDistance >= currentUnitSize && !lastOne)
			{
				realDistance /= currentUnitSize;
			}
			else
			{
				if (realDistance == Math.round(realDistance))
				{
					distanceInWords =  String.format("%.0f %s", realDistance, metricLengthUnits[i]) ;
				}
				else
				{
					distanceInWords =  String.format("%.1f %s", realDistance, metricLengthUnits[i]) ;
				}
			}
		}
		
		
		return distanceInWords;
	
	}

	public String toString()
	{
		return distanceInWords; 
	}

	public int getDistance() {
		return distance;
	}
	
}
