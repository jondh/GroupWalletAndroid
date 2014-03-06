/**
 *  Author: Jonathan Harrison
 *  Date: 9/21/13
 *  Description: This class is used to log in to the where one server
 *  Input: An instance of DBhttpRequest
 *		   A String for user name
 *		   A String for password
 *  Output: Integer amount for the logged in userID -> 0 or negative if log in failed
 *  Implementation:
 *  
	   	LogIn logIn = new LogIn(DBhttpRequest, String userName, String password);
	   	logIn.setLogInListener(new LogInListener(){
	   		@Override
	   		public void logInComplete(Profile _user){
	   		
	   		}
	   		@Override
	   		public void logInCancelled(){
	   		
	   		}
	   	});
	   	logIn.execute("<url>");
 * 
 * 
 *  SERVER input (name value pairs): "userName" -> String for user name
 * 									 "password" -> String for password
 * 
 *  SERVER output: returns a JSON object with the following name value pair:
 *  				"userID" -> Integer for userID (0 or negative for failure)
 */

package com.whereone;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Profile;

public class LogIn extends AsyncTask<String, Void, Profile> {
	private DBhttpRequest httpRequest;
	private LogInListener listener;
	private String userName;
	private String password;
	
	public LogIn(DBhttpRequest _httpRequest, String _userName, String _password){
		httpRequest = _httpRequest;
		userName = _userName;
		password = _password;
	}
	
	public void setLogInListener(LogInListener _listener) {
        this.listener = _listener;
    }
	
	@Override
	protected Profile doInBackground(String... arg0) {
		
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("username",userName));
		nameValuePairs.add(new BasicNameValuePair("password",password));
		String result = httpRequest.sendRequest(nameValuePairs, url);
		
		System.out.println(result);
		
		try {
			JSONObject jObj = new JSONObject(result);
			JSONObject userJSON = jObj.getJSONObject("User");
			JSONObject tokenJSON = jObj.getJSONObject("Token");
			Profile user = new Profile(
					userJSON.getInt("id"),
					userJSON.getString("username"),
					"",
					userJSON.getString("firstName"),
					userJSON.getString("lastName"),
					userJSON.getString("email"),
					userJSON.getInt("fbID"),
					tokenJSON.getString("Private"),
					tokenJSON.getString("Public")
			);
			return user;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onPostExecute(Profile result) {
		listener.logInComplete(result);
	}

	@Override
	protected void onCancelled() {
		listener.logInCancelled();
	}
	
	public interface LogInListener{
		public void logInComplete(Profile _user);
		public void logInCancelled();
	}
}
