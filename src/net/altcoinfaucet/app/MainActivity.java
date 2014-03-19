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
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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
            
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }
 
        @Override
        protected Void doInBackground(Void... arg0) {
        	
            ServiceHandler httpRequest = new ServiceHandler();
            String jsonStr = httpRequest.makeServiceCall(apiUrl + "/faucets", ServiceHandler.GET);
            Log.d("fooo","bar");
 
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
                maximumIndex = faucetArray.length();
                
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

                	faucet.save();
                    faucetList.add(faucet.toLinkedHashMap());
                    
                	publishProgress();
                }     
                httpRequest.destroy();
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
