package se.loppiskartan.clients.android.storage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;

public class ApiKeyStorage {
	private static ApiKeyStorage instance;
	
	final static String[] signatures = {
		"308201e53082014ea00302010202044a33cba7300d06092a864886f70d01010505003037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f6964204465627567301e170d3039303631333135353431355a170d3130303631333135353431355a3037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f696420446562756730819f300d06092a864886f70d010101050003818d0030818902818100bda06f1d709e1a7ee69aea9d7b8a38624e861d5498f7bb7aa70feb87d31d3a5c80dc3defdc90786acf138de032d9f3fd75eb29f917bbcefc30affd53e3b4577d7acab7b3ff779645e58d73833360b2f95cb4e4e77ea272db362f66aed785a89a92425eaf1615b10c4ea32233acdf9e45364ee1720c954e87dc2a214e1306d7b70203010001300d06092a864886f70d010105050003818100100567b4971e0baafab36de65c8d636ee6b8a9f67e2fe53fc14b4fc214eb718a142a9e040f250fe633e91a3f242804b5234cedfdd2e2608bb800036200bb1068c97be246323dbc759a1f3b1dd00cb75eea256e3a083d5e689be833beeb5b5cfd6a67c8bf4b9dabd4ef83e513c767fe6e426ef447728b01a75fa7ec179ca8bdc2",
	};
	
	final static String[] apikeys = {
		"0MtiE0IlLNlgoVDUR_FXXaxk6qDQWsTbRj-34HA", // debug
		"0MtiE0IlLNliB0pQ7viuApscFOmqmlgj4mZ15qw" // live
	};
	
	Context context;
	
	public static ApiKeyStorage getInstance(Context context)
	{
		if (instance == null) instance = new ApiKeyStorage(context);
		return instance;
	}

	public ApiKeyStorage(Context context)
	{
		this.context = context;
	}
	
	public String getMapsApiKey()
	{
		String mapsApiKey = null;;
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
			Signature[] s = pInfo.signatures;
			for(int i=0;i<s.length;i++)
			{
				String blabla = s[i].toCharsString();
				for(int o=0;i<signatures.length;o++)
				{
					
					
					if (s[i].toCharsString() == signatures[o])
					{
						mapsApiKey = apikeys[o];
					}
				}
			}
		} catch (NameNotFoundException e) {
		}
		if (mapsApiKey == null)
		{
			mapsApiKey = apikeys[apikeys.length-1];
		}
		
		return mapsApiKey;
	}
}
