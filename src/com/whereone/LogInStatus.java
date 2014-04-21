/**
 *  Author: Jonathan Harrison
 *  Date: 9/21/13
 *  Description: This class is used to log out
 *  Input: An instance of DBhttpRequest
 *  
 *  Output: Integer amount for the logged in userID -> 0 or negative if log in failed
 *  Implementation:
 *  
	   	LogInStatus LogInStatus = new LogInStatus(DBhttpRequest, Integer userId);
	   	LogInStatus.setLogInStatusListener(new LogInStatusListener(){
	   		@Override
	   		public void LogInStatusComplete(Boolean result){
	   		
	   		}
	   		@Override
	   		public void LogInStatusCancelled(){
	   		
	   		}
	   	});
	   	LogInStatus.execute("<url>");
 * 
 * 
 */

package com.whereone;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Profile;

public class LogInStatus extends AsyncTask<String, Void, Boolean> {
	private DBhttpRequest httpRequest;
	private Profile profile;
	private LogInStatusListener listener;
	
	private String resultType;
	
	public LogInStatus(DBhttpRequest _httpRequest, Profile profile){
		httpRequest = _httpRequest;
		this.profile = profile;
	}
	
	public void setLogInStatusListener(LogInStatusListener _listener) {
        this.listener = _listener;
    }
	
	@Override
	protected Boolean doInBackground(String... arg0) {
		
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", profile.getUserID().toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));
		String result = httpRequest.sendRequest(nameValuePairs, url);
		
		System.out.println(result);
		
		if(result.contains("success")){
			return true;
		}
		else if(result.contains("timeout")){
			resultType = "timeout";
		}
		else if(result.contains("unknownHost")){
			resultType = "unknownHost";
		}
		else{
			resultType = "failure";
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		listener.LogInStatusComplete(result, resultType);
	}

	@Override
	protected void onCancelled() {
		listener.LogInStatusCancelled();
	}
	
	public interface LogInStatusListener{
		public void LogInStatusComplete(Boolean result, String resultString);
		public void LogInStatusCancelled();
	}
}
