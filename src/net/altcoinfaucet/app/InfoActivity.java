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
import android.content.pm.ActivityInfo;
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
import android.widget.Toast;

public class InfoActivity extends Activity {

	Context infoContext;
	Faucet infoFaucet;
	ListView detailListView;
	ArrayList<HashMap<String, String>> detailList;
	ProgressDialog processDialog;
	GetFaucetDetailsTask asyncTask;
	
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
		
		setTitle(infoFaucet.getName() + " (" + infoFaucet.getShortName() + ")" + " - " + "Faucet");
		
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
        	asyncTask = new GetFaucetDetailsTask();
        	asyncTask.execute();
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void loadDetailListView()
    {
    	
    	detailList.clear();
    	
    	LinkedHashMap<String, String> faucetHashMap = infoFaucet.toLinkedHashMap();
    	
    	if(infoFaucet.getStats()!=null)
    	{
    		faucetHashMap.putAll(infoFaucet.getStats().toLinkedHashMap());
    	}
    	
    	if(infoFaucet.getInfo()!=null)
    	{
    		faucetHashMap.putAll(infoFaucet.getInfo().toLinkedHashMap());
    	}
    	
    	Set<java.util.Map.Entry<String, String>> entrySet = faucetHashMap.entrySet();
    	for( Iterator<java.util.Map.Entry<String, String>> entryIterator = entrySet.iterator(); entryIterator.hasNext();)
    	{
    		HashMap<String, String> dataPair = new HashMap<String, String>();
    		java.util.Map.Entry<String, String>  entry = entryIterator.next();
    		
    		
    		if(entry.getKey().equals(JSONTag.TAG_BALANCE)) {
    			
    	   		dataPair.put("setting", "Balance");
        		dataPair.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));  	   		
    		} else
    		if(entry.getKey().equals(JSONTag.TAG_MIN_PAYOUT))
    		{
    			dataPair.put("setting", "Min payout");
        		dataPair.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		} else
    		if(entry.getKey().equals(JSONTag.TAG_MAX_PAYOUT))
    		{
    			dataPair.put("setting", "Max payout");
        		dataPair.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		} else
    		if(entry.getKey().equals(JSONTag.TAG_WITHDRAW_LIMIT))
    		{
    			dataPair.put("setting", "Withdrawal limit");
        		dataPair.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		} else	
    		if(entry.getKey().equals(JSONTag.TAG_TRANSACTION_FEE))
    		{
    			dataPair.put("setting", "Transaction fee");
        		dataPair.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		}else	
    		if(entry.getKey().equals(JSONTag.TAG_TIMEOUT))
    		{
    			dataPair.put("setting", "Timeout");
        		dataPair.put("value", entry.getValue() + " minutes");
	
    		}else	
    		if(entry.getKey().equals(JSONTag.TAG_VOLUME))
    		{
    			dataPair.put("setting", "Total paid");
        		dataPair.put("value", entry.getValue() + " " + faucetHashMap.get(JSONTag.TAG_SHORTNAME));
	
    		}else	   		
    		if(entry.getKey().equals(JSONTag.TAG_TOTAL_PAYOUTS))
    		{
    			dataPair.put("setting", "Total payouts");
        		dataPair.put("value", entry.getValue());
	
    		}else
    		if(entry.getKey().equals(JSONTag.TAG_ALGORITHM))
    		{
    			dataPair.put("setting", "Algorithm");
        		dataPair.put("value", entry.getValue());
    		}else
    		if(entry.getKey().equals(JSONTag.TAG_HASHRATE))
    		{
    			dataPair.put("setting", "Hashrate");
        		dataPair.put("value", entry.getValue() + " MH/s");
    		}else 
    		if(entry.getKey().equals(JSONTag.TAG_DIFFICULTY))
    		{
    			dataPair.put("setting", "Difficulty");
        		dataPair.put("value", entry.getValue());
    		}else 
    		if(entry.getKey().equals(JSONTag.TAG_BLOCKS))
    		{
    			dataPair.put("setting", "Blocks");
        		dataPair.put("value", entry.getValue());
    		} 		
    		else {
    			continue;
    		}
    		
    		detailList.add(dataPair);
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
    	if(asyncTask!=null)
    	{
    		asyncTask.cancel(true);
    	}
    }
    
    /**
     * AsyncTask to load data
     * @author gehaxelt
     *
     */
	private class GetFaucetDetailsTask extends AsyncTask<Void, Void, Void> {
 
		String currentMessage;
		int currentIndex;
		int maximumIndex;
	    private String apiUrl = "http://api.altcoinfaucet.net/public";
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            processDialog = new ProgressDialog(InfoActivity.this);
            processDialog.setMessage("Initialising...");
            processDialog.setCancelable(false);
            processDialog.show();
            
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
 
        }
 
        @Override
        protected Void doInBackground(Void... arg0) {
        	
            currentMessage = "Processing information ...";
            currentIndex = 0;
            maximumIndex = 2;
            
            if(isCancelled()) return null;
        	//Update stats
        	ServiceHandler httpRequest = new ServiceHandler();
        	
            if(!httpRequest.isDataAvailable(infoContext))
            {
            	this.showToast("No internet connection. Update failed.");
            	return null;
            }
            
            String jsonDetails = httpRequest.makeServiceCall(apiUrl + "/faucet/" + infoFaucet.getName() + "/stats", ServiceHandler.GET);
            
            try {
            	if(jsonDetails != null)
            	{
            		FaucetStats faucetStatistics = infoFaucet.getStats();
            		faucetStatistics.fromJSONObject(new JSONArray(jsonDetails).getJSONObject(0) );
            		faucetStatistics.save();
                	publishProgress();
            	}
            }catch(Exception e) {}
            
            if(isCancelled()) return null;
        	//Update info
        	String jsonInfo = httpRequest.makeServiceCall(apiUrl + "/faucet/" + infoFaucet.getName() + "/info", ServiceHandler.GET);
        	
        	try {
            	
            	if(jsonInfo != null)
            	{
            		FaucetInfo faucetInformation = infoFaucet.getInfo();
            		faucetInformation.fromJSONObject(new JSONArray(jsonInfo).getJSONObject(0).getJSONObject(JSONTag.TAG_INFORMATION));
            		faucetInformation.save();
            		publishProgress();
            	}    
        	} catch(Exception e) {}

        	infoFaucet.save();  
        	this.showToast("Update successful!");
        	
        	return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (processDialog.isShowing())
                processDialog.dismiss();
            
            loadDetailListView();
            
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
           
        }
        
        @Override
        protected void onProgressUpdate(Void... values) {
        	// TODO Auto-generated method stub
        	super.onProgressUpdate(values);
        	currentIndex++;
        	processDialog.setMessage(currentMessage + String.valueOf(currentIndex) + "/" + String.valueOf(maximumIndex));
        	
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
		        	processDialog.dismiss();	
				}
			});
        }
        
        private void showToast(final String text)
        {
        	runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
	            	Toast.makeText(infoContext, text, Toast.LENGTH_SHORT).show();
				}
			});
        }
        
        
	}
}
