/**
 * Author: Jonathan Harrison
 * Date: 4/20/14
 * Description: 
 * 
 * 
 * 
 * 
 */

package com.whereone.groupWallet;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.User;

public class GetWRUsers extends AsyncTask<String, Void, ArrayList<User> >{
	private DBhttpRequest httpRequest;
	private Profile profile;
	private UserWRListener userListener;
	private ArrayList<Integer> userID;
	
	private String resultType;
	
	public GetWRUsers(DBhttpRequest httpRequest, Profile profile, ArrayList<Integer> _userID){
		this.httpRequest = httpRequest;
		this.profile = profile;
		this.userID = _userID;
		if(userID == null){
			userID = new ArrayList<Integer>();
		}
		if(userID.size() == 0){
			userID.add(0);
		}
	}
	
	public void setWRUsersListener(UserWRListener _userListener) {
        this.userListener = _userListener;
    }
	
	@Override
	protected void onPreExecute(){ 
		userListener.getUsersPreExecute();
	}
	
	@Override
	protected ArrayList<User> doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for(int i = 0; i < userID.size(); i++){
			nameValuePairs.add( new BasicNameValuePair("userID[]", userID.get(i).toString() ) );
		}
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));

		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("GetUsers Result", "Users: " +  result);
		
		try {
			JSONObject jObj = new JSONObject(result);
			String success = jObj.getString("result");
			if(success.contains( "success" )){
				ArrayList<User> users = new ArrayList<User>();
				if(!jObj.getBoolean("empty")){
					JSONArray jObjA = jObj.getJSONArray("users");
					for(int i = 0; i < jObjA.length(); i++){
						JSONObject jObjUser = jObjA.getJSONObject(i);
						JSONObject jObjU = jObjUser.getJSONObject("User");
						users.add( new User(
								jObjU.getInt("id"),
								jObjU.getString("username"),
								jObjU.getString("firstName"),
								jObjU.getString("lastName"),
								jObjU.getString("email"),
								jObjU.getString("fbID"),
								jObjU.getString("dateTime")) );
					}
					resultType = "success";
					return users;
				}
				resultType = "empty";
				return users;
			}
			else if(success.contains("timeout")){
				resultType = "timeout";
			}
			else if(success.contains("unknownHost")){
				resultType = "unknownHost";
			}
			else{
				resultType = "failure";
			}
			
			return null;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultType = "failure";
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(ArrayList<User> result) {
		userListener.getUsersCompleted(result, resultType);
	}

	@Override
	protected void onCancelled() {
		userListener.getUsersCancelled();
	}
	
	public interface UserWRListener{
		public void getUsersPreExecute();
		public void getUsersCompleted(ArrayList<User> user, String resultString);
		public void getUsersCancelled();
	}
}

