package net.altcoinfaucet.app;
 
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
 
public class ServiceHandler {
 
    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
	private DefaultHttpClient httpclient;
	private HttpParams httpparams;
 
    public ServiceHandler() {
    	// http client
    	this.httpclient = new DefaultHttpClient();
    	
    	this.httpparams = httpclient.getParams();
    	httpparams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    	
    	HttpConnectionParams.setTcpNoDelay(httpparams, true);
    	httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				return 1000;
			}
		});
    	httpclient.setReuseStrategy(new ConnectionReuseStrategy() {
			
			@Override
			public boolean keepAlive(HttpResponse response, HttpContext context) {
				return true;
			}
		});
    }
 
    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * */
    public String makeServiceCall(String url, int method) {
        return this.makeServiceCall(url, method, null);
    }
 
    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public String makeServiceCall(String url, int method,
            List<NameValuePair> params) {
        try {
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;
             
            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setParams(httpparams);
                // adding post params
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }
 
                httpResponse = httpclient.execute(httpPost);
 
            } else if (method == GET) {
                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url);
                httpGet.setParams(httpparams);
 
                httpResponse = httpclient.execute(httpGet);
 
            }
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
         
        return response;
 
    }
    
    public void destroy() {
    	httpclient.getConnectionManager().shutdown();
    }
    
    //Source: http://stackoverflow.com/questions/2326767/how-do-you-check-the-internet-connection-in-android
    public boolean isDataAvailable(Context ctx)
    {
     ConnectivityManager conxMgr = (ConnectivityManager) ctx.getSystemService(ctx.CONNECTIVITY_SERVICE);

     NetworkInfo mobileNwInfo = conxMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
     NetworkInfo wifiNwInfo   = conxMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

    return ((mobileNwInfo== null? false : mobileNwInfo.isConnected() )|| (wifiNwInfo == null? false : wifiNwInfo.isConnected()));
    }
}