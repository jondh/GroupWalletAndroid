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

package com.whereone.groupwalletcake;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.User;

public class GetUser extends AsyncTask<String, Void, User>{
	private DBhttpRequest httpRequest;
	private getUserListener userListener;
	private Integer userID;
	private String publicToken;
	private String privateToken;
	private String timeStamp;
	
	public GetUser(DBhttpRequest _httpRequest, Integer _userID, String public_token, String private_tokenH, String _timeStamp){
		httpRequest = _httpRequest;
		userID = _userID;
		publicToken = public_token;
		privateToken = private_tokenH;
		timeStamp = _timeStamp;
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
		nameValuePairs.add(new BasicNameValuePair("public_token", publicToken));
		nameValuePairs.add(new BasicNameValuePair("private_token", privateToken));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", timeStamp));

		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("GetUser Result", "User: " +  result);
		
		try {
			JSONObject jObj = new JSONObject(result);
			String success = jObj.getString("result");
			if(success.contains( "success" )){
				if(!jObj.getBoolean("empty")){
					JSONObject jObjU = jObj.getJSONObject("user");
					return new User(
							jObjU.getInt("id"),
							jObjU.getString("username"),
							jObjU.getString("firstName"),
							jObjU.getString("lastName"),
							jObjU.getString("email"),
							0);
				}
				return new User(-1,"-1","-1","-1","-1",-1);
			}
			else{
				return null;
			}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(User result) {
		userListener.getUserCompleted(result);
	}

	@Override
	protected void onCancelled() {
		userListener.getUserCancelled();
	}
	
	public interface getUserListener{
		public void getUserPreExecute();
		public void getUserCompleted(User user);
		public void getUserCancelled();
	}
}

