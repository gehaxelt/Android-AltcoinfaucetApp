package net.altcoinfaucet.app;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.orm.SugarRecord;

public class FaucetStats extends SugarRecord<FaucetStats>{

	int total_payouts;
	double volume;
	
	public FaucetStats(Context arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void fromJSONObject(JSONObject json)
	{
		try {
			this.total_payouts = json.getInt(JSONTag.TAG_TOTAL_PAYOUTS);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			this.total_payouts = 0;
			e.printStackTrace();
		}
		try {
			this.volume = json.getDouble(JSONTag.TAG_VOLUME);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			this.volume = 0;
			e.printStackTrace();
		}
	}
	
	public HashMap<String, String> toHashMap()
	{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(JSONTag.TAG_VOLUME, String.valueOf(this.volume));
		map.put(JSONTag.TAG_TOTAL_PAYOUTS, String.valueOf(this.total_payouts));
		
		return map;
	}
	
	
}
