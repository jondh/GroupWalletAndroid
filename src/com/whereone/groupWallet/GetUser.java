/**
 * Author: Jonathan Harrison
 * Date: 2/24/14
 * Description: 
 * 
 * 
 * 
 * 
 
 		GetUser getUser = new GetUser(httpRequest, profile.getInt("id", 0));
	   	getUser.setUserListener(new getUserListener(){

			@Override
			public void getUserPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getUserComplete(ArrayList<userRelation> _amount) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getUserCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	getUser.execute("<url>");
 
 * 
 * 
 * 
 * 
 */

package com.whereone.groupWallet;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.User;

public class GetUser extends AsyncTask<String, Void, User>{
	private DBhttpRequest httpRequest;
	private Profile profile;
	private getUserListener userListener;
	private Integer userID;
	
	private String resultType;
	
	public GetUser(DBhttpRequest httpRequest, Profile profile, Integer _userID){
		this.httpRequest = httpRequest;
		this.profile = profile;
		userID = _userID;
	}
	
	public void setUserListener(getUserListener _userListener) {
        this.userListener = _userListener;
    }
	
	@Override
	protected void onPreExecute(){ 
		userListener.getUserPreExecute();
	}
	
	@Override
	protected User doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userID.toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));

		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("GetUser Result", "User: " +  result);
		
		try {
			JSONObject jObj = new JSONObject(result);
			String success = jObj.getString("result");
			if(success.contains( "success" )){
				if(!jObj.getBoolean("empty")){
					JSONObject jObjU = jObj.getJSONObject("user");
					resultType = "success";
					return new User(
							jObjU.getInt("id"),
							jObjU.getString("username"),
							jObjU.getString("firstName"),
							jObjU.getString("lastName"),
							jObjU.getString("email"),
							jObjU.getString("fbID"));
				}
				resultType = "empty";
				return null;
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
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(User result) {
		userListener.getUserCompleted(result, resultType);
	}

	@Override
	protected void onCancelled() {
		userListener.getUserCancelled();
	}
	
	public interface getUserListener{
		public void getUserPreExecute();
		public void getUserCompleted(User user, String resultString);
		public void getUserCancelled();
	}
}

