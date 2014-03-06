/**
 *  Author: Jonathan Harrison
 *  Date: 9/21/13
 *  Description: This class is used to create a new user on the where one server
 *  Input: An instance of DBhttpRequest
 *		   A String for user name
 *		   A String for password
 *		   A String for email
 *		   A String for first name
 *		   A String for last name
 *         A String for app ("" or "GroupWallet" or "Pace")
 *  Output: Integer amount for the logged in userID -> 0 or negative if log in / account creation failed
 *  Implementation:
 *  
	   	NewUser newUser = new NewUser(DBhttpRequest, String _userName, String _password, String _email,
			String _firstName, String _lastName, String _app);
	   	newUser.setnewUserListener(new newUserListener(){
	   		@Override
	   		public void newUserPreExecute(){
	   		
	   		}
	   		@Override
	   		public void newUserComplete(Integer _userID){
	   		
	   		}
	   		@Override
	   		public void newUserCancelled(){
	   		
	   		}
	   	});
	   	newUser.execute();
 * 
 * 
 *  SERVER input (name value pairs): 
 *  				"userName"  -> String for user name
 * 					"password"  -> String for password
 * 					"email"     -> String for email
 * 					"firstName" -> String for first name
 *					"lastName"  -> String for last name
 * 					"app"       -> String for which app account is being created for ("" (none) or "GroupWallet" etc)
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

public class NewUser extends AsyncTask<Void, Void, Integer> {
	private DBhttpRequest httpRequest;
	private NewUserListener listener;
	private String userName;
	private String password;
	private String email;
	private String firstName;
	private String lastName;
	private String app;
	
	NewUser(DBhttpRequest _httpRequest, String _userName, String _password, String _email,
			String _firstName, String _lastName, String _app){
		httpRequest = _httpRequest;
		userName = _userName;
		password = _password;
		email = _email;
		firstName = _firstName;
		lastName = _lastName;
		app = _app;
	}

	public void setNewUserListener(NewUserListener _listener) {
        this.listener = _listener;
    }
	
	@Override
	protected void onPreExecute(){
		listener.newUserPreExecute();
	}
	
	@Override
	protected Integer doInBackground(Void... arg0) {
		String url = "http://jondh.com/GroupWallet/android/newUser.php";
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("userName",userName));
		nameValuePairs.add(new BasicNameValuePair("password",password));
		nameValuePairs.add(new BasicNameValuePair("email",email));
		nameValuePairs.add(new BasicNameValuePair("firstName",firstName));
		nameValuePairs.add(new BasicNameValuePair("lastName",lastName));
		nameValuePairs.add(new BasicNameValuePair("app",app));
		String result = httpRequest.sendRequest(nameValuePairs, url);
		
		try {
			JSONObject jObj = new JSONObject(result);
			return jObj.getInt("userID");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		listener.newUserComplete(result);
	}

	@Override
	protected void onCancelled() {
		listener.newUserCancelled();
	}
	
	public interface NewUserListener{
		public void newUserPreExecute();
		public void newUserComplete(Integer _userID);
		public void newUserCancelled();
	}
}