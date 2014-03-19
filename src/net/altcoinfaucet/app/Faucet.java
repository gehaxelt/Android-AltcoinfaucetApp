package net.altcoinfaucet.app;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.query.Condition;
import com.orm.query.Select;


public class Faucet extends SugarRecord<Faucet>{
	private int faucetId;
	private String name;
	private String shortName;
	private String slogan;
	private String donationAddress;
	private double balance;
	private double minPayout;
	private double maxPayout;
	private double withdrawLimit;
	private double transactionFee;
	private int timeout;
	private boolean enabled;
	private int state;
	private FaucetStats statistics;
	private FaucetInfo information;
	
	@Ignore
	private Context ctx;
	
	 
	public Faucet(Context ctx) {
		super(ctx);
		this.ctx = ctx;
	}
	
	public void fromJSONObj(JSONObject json) throws JSONException
	{
		this.faucetId = json.getInt(JSONTag.TAG_ID);
		this.name = json.getString(JSONTag.TAG_NAME).toUpperCase();
		this.shortName = json.getString(JSONTag.TAG_SHORTNAME);
		this.slogan = json.getString(JSONTag.TAG_SLOGAN);
		
		try {
			this.donationAddress = json.getString(JSONTag.TAG_DONATE);
		} catch(Exception e)
		{
			this.donationAddress = "";
		}
		
		try {
			this.balance = json.getDouble(JSONTag.TAG_BALANCE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.balance = 0;
			e.printStackTrace();
		}
		
		try {
			this.minPayout = json.getDouble(JSONTag.TAG_MIN_PAYOUT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.minPayout = 0;
			e.printStackTrace();
		}
		try {
			this.maxPayout = json.getDouble(JSONTag.TAG_MAX_PAYOUT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.maxPayout = 0;
			e.printStackTrace();
		}
		
		try {
			this.withdrawLimit = json.getDouble(JSONTag.TAG_WITHDRAW_LIMIT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.withdrawLimit =  0;
			e.printStackTrace();
		}
		
		try {
			this.transactionFee = json.getDouble(JSONTag.TAG_TRANSACTION_FEE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.transactionFee = 0;
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
		map.put(JSONTag.TAG_SHORTNAME, this.shortName);
		map.put(JSONTag.TAG_DONATE,this.donationAddress);
		map.put(JSONTag.TAG_BALANCE, String.valueOf(this.balance));
		map.put(JSONTag.TAG_MIN_PAYOUT, String.valueOf(this.minPayout));
		map.put(JSONTag.TAG_MAX_PAYOUT, String.valueOf(this.maxPayout));
		map.put(JSONTag.TAG_WITHDRAW_LIMIT, String.valueOf(this.withdrawLimit));
		map.put(JSONTag.TAG_TRANSACTION_FEE, String.valueOf(this.transactionFee));
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
		
		
		map.put("list_headline", this.name + " (" + this.shortName + ")" + " - " + "Faucet");
		map.put("subheading", map.get(JSONTag.TAG_STATE) + ": " + String.valueOf(this.balance) + " " + this.shortName);
		
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
		if(this.statistics == null)
		{
			this.statistics = new FaucetStats(this.ctx);
		}
		
		return this.statistics;
	}

	public FaucetInfo getInfo() {
		// TODO Auto-generated method stub
		if(this.information == null)
		{
			this.information = new FaucetInfo(this.ctx);
		}
		return this.information;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}
	
	public String getShortName() {
		// TODO Auto-generated method stub
		return this.shortName;
	}
	

}
