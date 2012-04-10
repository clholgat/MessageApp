/**
 * 
 */
package com.anompom;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
 * @author carson
 *
 */
public class ConversationListActivity extends ListActivity {
	public RESTAPI api;
	public String TAG = "ConversationList";
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		api = new RESTAPI(this, getSharedPreferences("MessageAppPrefs", 0));
		
	}
	
	private class GetConversationsTask extends AsyncTask<ListActivity, Void, String>{
		private ListActivity activity;
    	@Override
		protected String doInBackground(ListActivity... params) {
			this.activity = params[0];
    		String res = null;
			try{
				URI uri = new URI(RESTAPI.BASE_URL+"conversation");
				Log.w(TAG, uri.getRawPath());
				HttpPost post = new HttpPost(uri);
				AndroidHttpClient client = AndroidHttpClient.newInstance("RESTAPI");
				List<NameValuePair> nameValueParams = new ArrayList<NameValuePair>();
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValueParams, "UTF-8");
				post.setEntity(entity);
				post.setHeader("Cookie", api.getAuthCookie());
				post.setHeader("X-Same-Domain", "1");
				ResponseHandler<String> responseHandler=new BasicResponseHandler();
				res = client.execute(post, responseHandler);
			}catch(Exception e){
				Log.w(TAG, e.getMessage());
			}
			return res;
		}
    	
    	protected void onPostExecute(String res){
			Gson gson = new Gson();
			ResponseObject ro = gson.fromJson(res, ResponseObject.class);
			if(ro.error != null){
				Log.w(TAG, ro.error);
			}else{
				activity.setListAdapter()
			}
			
    	}
    	
    	private class ResponseObject{
    		public String error;
    		public long startTime;
    		public List<String> participants;
    	}
	}
}
