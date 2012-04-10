/**
 * 
 */
package com.anompom;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;

/**
 * @author carson
 *
 */
public class RESTAPI {
	private static final String TAG = "RESTAPI";
	public static String BASE_URL = "http://silver-bullet-project.appspot.com/api/";
	public static String LOGIN_URL = "http://silver-bullet-project.appspot.com/_ah/login";
	private Context mContext = null;
	private AccountManager manager;
	private Account mAccount;
	private String cookie = null;
	
	public RESTAPI(Context context, SharedPreferences settings){
		this.mContext = context;
		this.manager = AccountManager.get(context);
		String user = settings.getString("UserName", null);
		Account[] accts = manager.getAccountsByType("com.google");
		this.mAccount = null;
		for(Account a: accts){
			if(a.name.equals(user)){
				this.mAccount = a;
			}
		}
	}
	
	public String getAuthCookie() throws URISyntaxException, ClientProtocolException, IOException{
		if(cookie != null){
			return cookie;
		}
		String authToken = getAuthToken();
		AndroidHttpClient client = AndroidHttpClient.newInstance("RESTAPI");
		String continueURL = ""; //might need this later
		URI uri = new URI(LOGIN_URL+"?continue="+URLEncoder.encode(continueURL, "UTF-8")+"&auth="+authToken);
		Log.w(TAG, authToken);
		HttpGet get = new HttpGet(uri);
		HttpParams params = new BasicHttpParams();
		HttpClientParams.setRedirecting(params, false);
		get.setParams(params);
		HttpResponse response = client.execute(get);
		Header[] headers = response.getHeaders("Set-Cookie");
		if(response.getStatusLine().getStatusCode() != 302 || headers.length == 0){
			Log.w(TAG, response.getStatusLine().getStatusCode()+" "+headers.length);
			return null;
		}
		for(Header h: headers){
			Log.w(TAG, h.getValue());
			if(h.getValue().indexOf("ACSID=") >= 0){
				String value = h.getValue();
				String[] parts = value.split(";");
				cookie = parts[0];
			}
		}
		return cookie;
	}

	public String getAuthToken(){
		String authToken = null;
		try{
			AccountManagerFuture<Bundle> future = manager.getAuthToken(mAccount, "ah", null, (Activity)mContext, null, null);
			Bundle bundle = future.getResult();
			authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN).toString();
			if(authToken == null){
				Log.w(TAG, "AuthToken is null");
			}
		} catch (OperationCanceledException e) {
	        Log.w(TAG, e.getMessage());
	    } catch (AuthenticatorException e) {
	        Log.w(TAG, e.getMessage());
	    } catch (IOException e) {
	        Log.w(TAG, e.getMessage());
	    }
		return authToken;
	}
}