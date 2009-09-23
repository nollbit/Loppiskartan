package se.loppiskartan.clients.android.storage;

import java.util.ArrayList;
import java.util.List;

import se.loppiskartan.clients.android.gateway.FleaLocation;

public class FleaStorage {

    private static FleaStorage instance;

    private ArrayList<FleaLocation> searchResults = null;
	public static FleaStorage getInstance()
    {
      if (instance == null) instance = new FleaStorage();
      return instance;
    }
    
	public ArrayList<FleaLocation> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(ArrayList<FleaLocation> searchResults) {
		this.searchResults = searchResults;
	}
	
	public FleaLocation getFleaById(int id)
	{
		if (searchResults != null)
		{
			for(int i=0;i<searchResults.size();i++)
			{
				if (searchResults.get(i).id == id)
				{
					return searchResults.get(i);
				}
			}
		}
		return null;
	}

    
}
