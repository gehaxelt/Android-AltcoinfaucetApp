package net.altcoinfaucet.app;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.orm.SugarRecord;

public class FaucetStats extends SugarRecord<FaucetStats>{

	private int totalPayouts;
	private double volume;
	
	public FaucetStats(Context arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void fromJSONObject(JSONObject json)
	{
		try {
			this.totalPayouts = json.getInt(JSONTag.TAG_TOTAL_PAYOUTS);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			this.totalPayouts = 0;
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
	
	public LinkedHashMap<String, String> toLinkedHashMap()
	{
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put(JSONTag.TAG_VOLUME, String.valueOf(this.volume));
		map.put(JSONTag.TAG_TOTAL_PAYOUTS, String.valueOf(this.totalPayouts));
		
		return map;
	}
	
	
}
