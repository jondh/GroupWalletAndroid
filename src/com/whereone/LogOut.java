/**
 *  Author: Jonathan Harrison
 *  Date: 9/21/13
 *  Description: This class is used to log out
 *  Input: An instance of DBhttpRequest
 *  
 *  Output: Integer amount for the logged in userID -> 0 or negative if log in failed
 *  Implementation:
 *  
	   	LogOut LogOut = new LogOut();
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
import com.whereone.groupWallet.models.Profile;

public class LogOut extends AsyncTask<String, Void, Boolean> {

	private LogOutListener listener;
	
	private DBhttpRequest httpRequest;
	private Profile profile;
	
	public LogOut(DBhttpRequest httpRequest, Profile profile){
		this.httpRequest = httpRequest;
		this.profile = profile;
	}
	
	public void setLogOutListener(LogOutListener _listener) {
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
