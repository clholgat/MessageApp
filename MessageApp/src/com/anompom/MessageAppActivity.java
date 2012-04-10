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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MessageAppActivity extends Activity {
	public static final String PREFS = "MessageAppPrefs";
	public static final String TAG = "MessageApp";
	public RESTAPI api;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	final SharedPreferences settings = getSharedPreferences(PREFS, 0);
    	if(!settings.contains("UserName")){
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AccountManager mgr = AccountManager.get(this);
            Account[] accts = mgr.getAccountsByType("com.google");
            final String[] items = new String[accts.length];
            for(int i = 0; i < accts.length; i++){
            	items[i] = accts[i].name;
            }
    		builder.setTitle("Choose Account To Log Into MessageApp");
    		builder.setItems(items,  new DialogInterface.OnClickListener() {
    		    public void onClick(DialogInterface dialog, int item) {
    		    	SharedPreferences.Editor editor = settings.edit();
    		    	editor.putString("UserName", items[item]);
    		    	editor.commit();
    		        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
    		    }
    		});
    		AlertDialog alert = builder.create();
    		alert.show();
    	}
    	
    	api = new RESTAPI((Context)this, settings);
    	new AuthTask().execute(this);
    }
    
    private class AuthTask extends AsyncTask<Activity, Void, String>{
    	private Activity activity;
    	@Override
		protected String doInBackground(Activity... params) {
			this.activity = params[0];
    		String res = null;
			try{
				URI uri = new URI(RESTAPI.BASE_URL+"register");
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
    		Intent intent;
			Gson gson = new Gson();
			ResponseObject ro = gson.fromJson(res, ResponseObject.class);
			if(ro.error != null){
				Log.w(TAG, ro.error);
			}else{
				intent = new Intent(activity, ConversationListActivity.class);
		    	startActivityForResult(intent, 0);
			}
			
    	}
    	private class ResponseObject{
    		public String error;
    		public String userid;
    	}
    }
}