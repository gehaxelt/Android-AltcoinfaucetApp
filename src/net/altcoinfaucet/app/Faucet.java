package net.altcoinfaucet.app;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;


public class Faucet extends SugarRecord<Faucet>{
	private int faucetId;
	private String name;
	private String shortname;
	private String slogan;
	private String donate;
	private double balance;
	private double min_payout;
	private double max_payout;
	private double withdraw_limit;
	private double transaction_fee;
	private int timeout;
	private boolean enabled;
	private int state;
	private FaucetStats stats;
	private FaucetInfo info;
		
	
	 
	public Faucet(Context ctx) {
		super(ctx);
		
		if(this.stats==null)
		{
			this.stats = new FaucetStats(ctx);
		}
		
		if(this.info == null)
		{
			this.info = new FaucetInfo(ctx);
		}
	}
	
	public void fromJSONObj(JSONObject json) throws JSONException
	{
		this.faucetId = json.getInt(JSONTag.TAG_ID);
		this.name = json.getString(JSONTag.TAG_NAME).toUpperCase();
		this.shortname = json.getString(JSONTag.TAG_SHORTNAME);
		this.slogan = json.getString(JSONTag.TAG_SLOGAN);
		
		try {
			this.donate = json.getString(JSONTag.TAG_DONATE);
		} catch(Exception e)
		{
			this.donate = "";
		}
		
		try {
			this.balance = json.getDouble(JSONTag.TAG_BALANCE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.balance = 0;
			e.printStackTrace();
		}
		
		try {
			this.min_payout = json.getDouble(JSONTag.TAG_MIN_PAYOUT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.min_payout = 0;
			e.printStackTrace();
		}
		try {
			this.max_payout = json.getDouble(JSONTag.TAG_MAX_PAYOUT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.max_payout = 0;
			e.printStackTrace();
		}
		
		try {
			this.withdraw_limit = json.getDouble(JSONTag.TAG_WITHDRAW_LIMIT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.withdraw_limit =  0;
			e.printStackTrace();
		}
		
		try {
			this.transaction_fee = json.getDouble(JSONTag.TAG_TRANSACTION_FEE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.transaction_fee = 0;
			e.printStackTrace();
		}
		
		this.timeout = json.getInt(JSONTag.TAG_TIMEOUT);
		this.enabled = json.getBoolean(JSONTag.TAG_ENABLED);
		
		this.state = json.getInt(JSONTag.TAG_STATE);

	}

	
	public LinkedHashMap<String, String> toLinkedHashMap(){
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		map.put(JSONTag.TAG_DB_ID, String.valueOf(this.getId()));
		map.put(JSONTag.TAG_ID, String.valueOf(this.id));
		map.put(JSONTag.TAG_NAME, this.name);
		map.put(JSONTag.TAG_SHORTNAME, this.shortname);
		map.put(JSONTag.TAG_DONATE,this.donate);
		map.put(JSONTag.TAG_BALANCE, String.valueOf(this.balance));
		map.put(JSONTag.TAG_MIN_PAYOUT, String.valueOf(this.min_payout));
		map.put(JSONTag.TAG_MAX_PAYOUT, String.valueOf(this.max_payout));
		map.put(JSONTag.TAG_WITHDRAW_LIMIT, String.valueOf(this.withdraw_limit));
		map.put(JSONTag.TAG_TRANSACTION_FEE, String.valueOf(this.transaction_fee));
		map.put(JSONTag.TAG_TIMEOUT, String.valueOf(this.timeout));
		map.put(JSONTag.TAG_ENABLED, String.valueOf(this.enabled));
		
		switch(this.state)
		{
			case 0:
				map.put(JSONTag.TAG_STATE, "Down");
				break;
			case 2 :
				map.put(JSONTag.TAG_STATE, "Disabled");
				break;
			case 1:
				map.put(JSONTag.TAG_STATE, "Empty");
				break;
			case 3:
				map.put(JSONTag.TAG_STATE, "Up");
				break;
		}
		
		
		map.put("list_headline", this.name + " (" + this.shortname + ")" + " - " + "Faucet");
		map.put("subheading", map.get(JSONTag.TAG_STATE) + ": " + String.valueOf(this.balance) + " " + this.shortname);
		
		return map;
 	}
	
	public static Faucet findByFaucetId(long id)
	{
		List<Faucet> fList = Faucet.find(Faucet.class, "faucet_Id = ?",String.valueOf(id));
		if(fList.size()==0)
			return null;
		
		return fList.get(0);
	}

	public FaucetStats getStats() {
		// TODO Auto-generated method stub
		return this.stats;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	public FaucetInfo getInfo() {
		// TODO Auto-generated method stub
		return this.info;
	}

	public String getShortName() {
		// TODO Auto-generated method stub
		return this.shortname;
	}
	

}
