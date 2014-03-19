package net.altcoinfaucet.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.DropBoxManager.Entry;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class InfoActivity extends Activity {

	Context infoContext;
	Faucet infoFaucet;
	ListView detailListView;
	ArrayList<HashMap<String, String>> detailList;
	ProgressDialog pDialog;
	GetFaucetDetailsTask aTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		infoContext = this.getBaseContext();
		setContentView(R.layout.activity_info);
		
		
		Bundle parameters = getIntent().getExtras();
		infoFaucet = Faucet.findById(Faucet.class, Long.valueOf(parameters.getString("faucet_id")));
		
		detailListView = (ListView) findViewById(R.id.info_listview);
		
		detailList = new  ArrayList<HashMap<String, String>>();
		
		TextView infoTitle = (TextView) findViewById(R.id.info_title);
		infoTitle.setText(infoFaucet.getName() + " (" + infoFaucet.getShortName() + ")" + " - " + "Faucet");
		
		this.loadDetailListView();
		
	}
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_info, menu);
        return true;
    }
    
    @SuppressLint("NewApi")
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        switch (item.getItemId()) {
        case R.id.menu_back: //Back button
        	finish();
            return true;
            
        case R.id.menu_refresh: //Refresh data
        	aTask = new GetFaucetDetailsTask();
        	aTask.execute();
        	aTask = null;
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void loadDetailListView()
    {
    	
    	detailList.clear();
    	
    	LinkedHashMap<String, String> faucetHashMap = infoFaucet.toLinkedHashMap();
    	faucetHashMap.putAll(infoFaucet.getStats().toLinkedHashMap());
    	faucetHashMap.putAll(infoFaucet.getInfo().toLinkedHashMap());
    	
    	Set<java.util.Map.Entry<String, String>> entrySet = faucetHashMap.entrySet();
    	for( Iterator<java.util.Map.Entry<String, String>> entryIterator = entrySet.iterator(); entryIterator.hasNext();)
    	{
    		HashMap<String, String> data = new HashMap<String, String>();
    		java.util.Map.Entry<String, String>  entry = entryIterator.next();
    		
    		
    		if(entry.getKey().equals(JSONTag.TAG_BALANCE)) {
    			
    	   		data.put("setting", "Balance");
        		data.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));  	   		
    		} else
    		if(entry.getKey().equals(JSONTag.TAG_MIN_PAYOUT))
    		{
    			data.put("setting", "Min payout");
        		data.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		} else
    		if(entry.getKey().equals(JSONTag.TAG_MAX_PAYOUT))
    		{
    			data.put("setting", "Max payout");
        		data.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		} else
    		if(entry.getKey().equals(JSONTag.TAG_WITHDRAW_LIMIT))
    		{
    			data.put("setting", "Withdrawal limit");
        		data.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		} else	
    		if(entry.getKey().equals(JSONTag.TAG_TRANSACTION_FEE))
    		{
    			data.put("setting", "Transaction fee");
        		data.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		}else	
    		if(entry.getKey().equals(JSONTag.TAG_TIMEOUT))
    		{
    			data.put("setting", "Timeout");
        		data.put("value", entry.getValue() + " minutes");
	
    		}else	
    		if(entry.getKey().equals(JSONTag.TAG_VOLUME))
    		{
    			data.put("setting", "Total paid");
        		data.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		}else	   		
    		if(entry.getKey().equals(JSONTag.TAG_TOTAL_PAYOUTS))
    		{
    			data.put("setting", "Total payouts");
        		data.put("value", entry.getValue());
	
    		}else
    		if(entry.getKey().equals(JSONTag.TAG_ALGORITHM))
    		{
    			data.put("setting", "Algorithm");
        		data.put("value", entry.getValue());
    		}else
    		if(entry.getKey().equals(JSONTag.TAG_HASHRATE))
    		{
    			data.put("setting", "Hashrate");
        		data.put("value", entry.getValue() + " MH/s");
    		}else 
    		if(entry.getKey().equals(JSONTag.TAG_DIFFICULTY))
    		{
    			data.put("setting", "Difficulty");
        		data.put("value", entry.getValue());
    		}else 
    		if(entry.getKey().equals(JSONTag.TAG_BLOCKS))
    		{
    			data.put("setting", "Blocks");
        		data.put("value", entry.getValue());
    		} 		
    		else {
    			continue;
    		}
    		
    		detailList.add(data);
    	}
    	
    	ListAdapter adapter = new SimpleAdapter(
                InfoActivity.this, detailList,
                R.layout.list_item, new String[] { "setting", "value"  }, new int[] { R.id.name, R.id.state });

       detailListView.setAdapter(adapter);
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	if(aTask!=null)
    	{
    		aTask.cancel(true);
    	}
    }
    
    /**
     * AsyncTask to load data
     * @author gehaxelt
     *
     */
	private class GetFaucetDetailsTask extends AsyncTask<Void, Void, Void> {
 
		String cur_message;
		int cur_index;
		int max_index;
	    private String API_URL = "http://api.altcoinfaucet.net/public";
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(InfoActivity.this);
            pDialog.setMessage("Initialising...");
            pDialog.setCancelable(false);
            pDialog.show();
 
        }
 
        @Override
        protected Void doInBackground(Void... arg0) {
        	
            cur_message = "Processing information ...";
            cur_index = 0;
            max_index = 2;
            
            if(isCancelled()) return null;
        	//Update stats
        	ServiceHandler shDetails = new ServiceHandler();
            String jsonDetails = shDetails.makeServiceCall(API_URL + "/faucet/" + infoFaucet.getName() + "/stats", ServiceHandler.GET);
            
            try {
            	if(jsonDetails != null)
            	{
            		FaucetStats fStats = infoFaucet.getStats();
            		fStats.fromJSONObject(new JSONArray(jsonDetails).getJSONObject(0) );
            		fStats.save();
                	publishProgress();
            	}
            }catch(Exception e) {}
            
            if(isCancelled()) return null;
        	//Update info
        	ServiceHandler shInfo = new ServiceHandler();
        	String jsonInfo = shInfo.makeServiceCall(API_URL + "/faucet/" + infoFaucet.getName() + "/info", ServiceHandler.GET);
        	
        	try {
            	
            	if(jsonInfo != null)
            	{
            		FaucetInfo fInfo = infoFaucet.getInfo();
            		fInfo.fromJSONObject(new JSONArray(jsonInfo).getJSONObject(0).getJSONObject(JSONTag.TAG_INFORMATION));
            		fInfo.save();
            		publishProgress();
            	}    
        	} catch(Exception e) {}

        	infoFaucet.save();  
        	
        	return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            
            loadDetailListView();
           
        }
        
        @Override
        protected void onProgressUpdate(Void... values) {
        	// TODO Auto-generated method stub
        	super.onProgressUpdate(values);
        	cur_index++;
        	pDialog.setMessage(cur_message + String.valueOf(cur_index) + "/" + String.valueOf(max_index));
        }
        
        @Override
        protected void onCancelled() {
        	// TODO Auto-generated method stub
        	super.onCancelled();
        	Handler h = new Handler(infoContext.getMainLooper());
        	h.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
		        	pDialog.dismiss();	
				}
			});
        }
        
        
	}
}
