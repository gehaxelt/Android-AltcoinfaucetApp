package net.altcoinfaucet.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orm.query.Condition;
import com.orm.query.Select;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
	 
	
    private ProgressDialog pDialog;
 
    ArrayList<HashMap<String, String>> faucetList;
    ListView faucetListView;
    Context mainContext;
 
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
//	    
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
            new GetFaucetListTask().execute();
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
    
    
    
    /**
     * AsyncTask to load data
     * @author gehaxelt
     *
     */
	private class GetFaucetListTask extends AsyncTask<Void, Void, Void> {
 
		String cur_message;
		int cur_index;
		int max_index;
	    private String API_URL = "http://api.altcoinfaucet.net/public";
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Initialising...");
            pDialog.setCancelable(false);
            pDialog.show();
 
        }
 
        @Override
        protected Void doInBackground(Void... arg0) {
        	
            ServiceHandler sh = new ServiceHandler();
            String jsonStr = sh.makeServiceCall(API_URL + "/faucets", ServiceHandler.GET);
            
 
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
                
                cur_message = "Processing information ...";
                cur_index = 0;
                max_index = faucetArray.length()*3;
                
                for(int i = 0; i < faucetArray.length(); i++) //
                {
                	JSONObject faucetObj = faucetArray.getJSONObject(i);
                	
                	//Update if possible;
                	Faucet faucet = Faucet.findByFaucetId((long)faucetObj.getInt(JSONTag.TAG_ID));
                	if(faucet == null){
                		faucet = new Faucet(mainContext);
                	}
                	
                	faucet.fromJSONObj(faucetObj);
                	
                	//Update stats
                	ServiceHandler shDetails = new ServiceHandler();
                    String jsonDetails = shDetails.makeServiceCall(API_URL + "/faucet/" + faucet.getName() + "/stats", ServiceHandler.GET);
                    
                    try {
	                	if(jsonDetails != null)
	                	{
	                		FaucetStats fStats = faucet.getStats();
	                		fStats.fromJSONObject(new JSONArray(jsonDetails).getJSONObject(0) );
	                		fStats.save();
	                    	publishProgress();
	                	}
                    }catch(Exception e) {}
                    
                	//Update info
                	ServiceHandler shInfo = new ServiceHandler();
                	String jsonInfo = shInfo.makeServiceCall(API_URL + "/faucet/" + faucet.getName() + "/info", ServiceHandler.GET);
                	
                	try {
	                	
	                	if(jsonInfo != null)
	                	{
	                		FaucetInfo fInfo = faucet.getInfo();
	                		fInfo.fromJSONObject(new JSONArray(jsonInfo).getJSONObject(0).getJSONObject(JSONTag.TAG_INFORMATION));
	                		fInfo.save();
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
            if (pDialog.isShowing())
                pDialog.dismiss();
            loadFromDatabase();
        }
        
        @Override
        protected void onProgressUpdate(Void... values) {
        	// TODO Auto-generated method stub
        	super.onProgressUpdate(values);
        	cur_index++;
        	pDialog.setMessage(cur_message + String.valueOf(cur_index) + "/" + String.valueOf(max_index));
        }
 
    }
    
    

    
}
