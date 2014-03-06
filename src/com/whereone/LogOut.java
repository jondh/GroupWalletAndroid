/**
 *  Author: Jonathan Harrison
 *  Date: 9/21/13
 *  Description: This class is used to log out
 *  Input: An instance of DBhttpRequest
 *  
 *  Output: Integer amount for the logged in userID -> 0 or negative if log in failed
 *  Implementation:
 *  
	   	LogOut LogOut = new LogOut(DBhttpRequest, Integer userId);
	   	LogOut.setLogOutListener(new LogOutListener(){
	   		@Override
	   		public void LogOutComplete(Boolean result){
	   		
	   		}
	   		@Override
	   		public void LogOutCancelled(){
	   		
	   		}
	   	});
	   	LogOut.execute("<url>");
 * 
 * 
 */

package com.whereone;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;

import com.whereone.groupWallet.controllers.DBhttpRequest;

public class LogOut extends AsyncTask<String, Void, Boolean> {
	private DBhttpRequest httpRequest;
	private LogOutListener listener;
	private Integer userID;
	private String publicToken;
	private String privateToken;
	private String timeStamp;
	
	public LogOut(DBhttpRequest _httpRequest, Integer _user_id, String public_token, String private_tokenH, String _timeStamp){
		httpRequest = _httpRequest;
		userID = _user_id;
		publicToken = public_token;
		privateToken = private_tokenH;
		timeStamp = _timeStamp;
	}
	
	public void setLogOutListener(LogOutListener _listener) {
        this.listener = _listener;
    }
	
	@Override
	protected Boolean doInBackground(String... arg0) {
		
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id",userID.toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", publicToken));
		nameValuePairs.add(new BasicNameValuePair("private_token", privateToken));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", timeStamp));
		String result = httpRequest.sendRequest(nameValuePairs, url);
		
		System.out.println(result);
		
		if(result.contains("success") || result.contains("failure")){
			return true;
		}
		else return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		listener.LogOutComplete(result);
	}

	@Override
	protected void onCancelled() {
		listener.LogOutCancelled();
	}
	
	public interface LogOutListener{
		public void LogOutComplete(Boolean _user);
		public void LogOutCancelled();
	}
}
