package net.altcoinfaucet.app;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.orm.SugarRecord;

public class FaucetInfo extends SugarRecord<FaucetInfo>{

	private int blocks;
	private double hashrate;
	private double difficulty;
	private String algorithm;
	
	public FaucetInfo(Context arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	
	public void fromJSONObject(JSONObject json)
	{
		try {
			this.blocks = json.getInt(JSONTag.TAG_BLOCKS);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			this.blocks = 0;
			e.printStackTrace();
		}
		try {
			this.hashrate = json.getDouble(JSONTag.TAG_HASHRATE) / 1000000;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			this.hashrate = 0;
			e.printStackTrace();
		}
		try {
			this.difficulty = json.getDouble(JSONTag.TAG_DIFFICULTY);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			this.difficulty = 0;
			e.printStackTrace();
		}
		try {
			this.algorithm = json.getString(JSONTag.TAG_ALGORITHM);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			this.algorithm = "";
			e.printStackTrace();
		}
		
	}
	
	public LinkedHashMap<String, String> toLinkedHashMap()
	{
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put(JSONTag.TAG_ALGORITHM, this.algorithm);
		map.put(JSONTag.TAG_BLOCKS, String.valueOf(this.blocks));
		map.put(JSONTag.TAG_DIFFICULTY, String.valueOf(this.difficulty));
		map.put(JSONTag.TAG_HASHRATE, String.valueOf(this.hashrate));
		return map;
	}

}
