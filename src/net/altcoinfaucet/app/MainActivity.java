package net.altcoinfaucet.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orm.query.Condition;
import com.orm.query.Select;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity {
	 
	
    private ProgressDialog processDialog;
 
    ArrayList<HashMap<String, String>> faucetList;
    ListView faucetListView;
    Context mainContext;
    GetFaucetListTask asyncTask;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        mainContext = this.getBaseContext();
        
        setContentView(R.layout.activity_main);
 
        faucetList = new ArrayList<HashMap<String, String>>();
 
        faucetListView = getListView();
        
//	    Faucet.deleteAll(Faucet.class);
//	    FaucetStats.deleteAll(FaucetStats.class);
//	    FaucetInfo.deleteAll(FaucetInfo.class);
	    
        this.loadFromDatabase();
        
        faucetListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				HashMap<String, String> faucetData =  (HashMap<String, String>) parent.getItemAtPosition(position);
				
				Intent infoIntent = new Intent(mainContext, InfoActivity.class);
				infoIntent.putExtra("faucet_id", faucetData.get(JSONTag.TAG_DB_ID));
				startActivity(infoIntent);
				
				
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        switch (item.getItemId()) {
        case R.id.menu_exit: //Exit button
        	finish();
            return true;
            
        case R.id.menu_refresh: //Refresh data
            asyncTask = new GetFaucetListTask();
            asyncTask.execute();
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void loadFromDatabase()
    {
    	
    	faucetList.clear();
    	for(int i = 3; i > 0 ; i--)
    	{
	    	for(Faucet faucet : (List<Faucet>) Select.from(Faucet.class).where(new Condition[]{new Condition("state").eq(String.valueOf(i))}).list()  )
	    	{
	    		faucetList.add(faucet.toLinkedHashMap());
	    	}
    	}
    	
    	ListAdapter adapter = new SimpleAdapter(
                MainActivity.this, faucetList,
                R.layout.list_item, new String[] { "list_headline", "subheading"  }, new int[] { R.id.name, R.id.state });

       setListAdapter(adapter);
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	if(asyncTask!=null) {
	    	asyncTask.cancel(true);
    	}
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	this.loadFromDatabase();
    }
    
    
    /**
     * AsyncTask to load data
     * @author gehaxelt
     *
     */
	private class GetFaucetListTask extends AsyncTask<Void, Void, Void> {
 
		private String currentMessage;
		private int currentIndex;
		private int maximumIndex;
	    private String apiUrl = "http://api.altcoinfaucet.net/public";
        
		
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            processDialog = new ProgressDialog(MainActivity.this);
            processDialog.setMessage("Initialising...");
            processDialog.setCancelable(false);
            processDialog.show();
        }
 
        @Override
        protected Void doInBackground(Void... arg0) {
        	
            ServiceHandler httpRequest = new ServiceHandler();
            String jsonStr = httpRequest.makeServiceCall(apiUrl + "/faucets", ServiceHandler.GET);
            
 
            if (jsonStr == null) {
            	 Log.e("ServiceHandler", "Couldn't get any data from the url");
            	 return null;
            }
            
            
            try {
                JSONArray faucetArray = new JSONArray(jsonStr);

                //Rescan the whole database if size mismatches
                if(Faucet.listAll(Faucet.class).size() != faucetArray.length())
                {
                	Log.d("Database","Deleted all faucets");
                	Faucet.deleteAll(Faucet.class);
                }
                
                faucetList.clear();
                
                currentMessage = "Processing information ...";
                currentIndex = 0;
                maximumIndex = faucetArray.length()*3;
                
                for(int i = 0; i < faucetArray.length(); i++) //
                {
                    
                	if(isCancelled()) return null;
                	JSONObject jsonFaucet = faucetArray.getJSONObject(i);
                	
                	//Update if possible;
                	Faucet faucet = Faucet.findByFaucetId((long)jsonFaucet.getInt(JSONTag.TAG_ID));
                	if(faucet == null){
                		faucet = new Faucet(mainContext);
                	}
                	
                	faucet.fromJSONObj(jsonFaucet);
                	
                	if(isCancelled()) return null;
                	
                	//Update stats
                    String jsonDetails = httpRequest.makeServiceCall(apiUrl + "/faucet/" + faucet.getName() + "/stats", ServiceHandler.GET);
                    
                    try {
	                	if(jsonDetails != null)
	                	{
	                		FaucetStats faucetStatistics = faucet.getStats();
	                		faucetStatistics.fromJSONObject(new JSONArray(jsonDetails).getJSONObject(0) );
	                		faucetStatistics.save();
	                    	publishProgress();
	                	}
                    }catch(Exception e) {}
                    
                    if(isCancelled()) return null;
                    //Update info
                	String jsonInformation = httpRequest.makeServiceCall(apiUrl + "/faucet/" + faucet.getName() + "/info", ServiceHandler.GET);
                	
                	try {
	                	
	                	if(jsonInformation != null)
	                	{
	                		FaucetInfo faucetInformation = faucet.getInfo();
	                		faucetInformation.fromJSONObject(new JSONArray(jsonInformation).getJSONObject(0).getJSONObject(JSONTag.TAG_INFORMATION));
	                		faucetInformation.save();
	                		publishProgress();
	                	}    
                	} catch(Exception e) {}

                	faucet.save();
                    faucetList.add(faucet.toLinkedHashMap());
                    
                	publishProgress();
                }     
           
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (processDialog.isShowing())
                processDialog.dismiss();
            loadFromDatabase();
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
        	Handler handler = new Handler(mainContext.getMainLooper());
        	handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
		        	processDialog.dismiss();	
				}
			});
        }
 
    }
    
    

    
}
