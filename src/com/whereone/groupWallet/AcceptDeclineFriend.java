/**
 * Author: Jonathan Harrison
 * Date: 4/23/14
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
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Friend;
import com.whereone.groupWallet.models.Profile;

public class AcceptDeclineFriend extends AsyncTask<String, Void, String>{
	private AcceptDeclineFriendListener listener;
	
	private DBhttpRequest httpRequest;
	private Profile profile;
	private Friend friend;
	
	private String resultType;
	private Type type;
	
	public enum Type{
		DECLINE,
		ACCEPT
	}
	
	public AcceptDeclineFriend(DBhttpRequest httpRequest, Profile profile, Friend friend, Type type){
		this.httpRequest = httpRequest;
		this.profile = profile;
		this.friend = friend;
		this.type = type;
	}
	
	public void setAcceptDeclineListener(AcceptDeclineFriendListener _listener) {
		this.listener = _listener;
    }
	
	@Override
	protected String doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
		nameValuePairs.add(new BasicNameValuePair("user1", friend.getUser1().toString() ));
		nameValuePairs.add(new BasicNameValuePair("user2", friend.getUser2().toString() ));
		nameValuePairs.add(new BasicNameValuePair("accept", type.ordinal()+"" ));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));
		
		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("AcceptDeclineFriend Result", result);
		try {
			JSONObject jObject = new JSONObject(result);
			String jResult = jObject.getString("result");
			if(jResult.contains( "success" )){
				
				resultType = "success";
				return resultType;
			}
			else if(jResult.contains("timeout")){
				resultType = "timeout";
			}
			else if(jResult.contains("unknownHost")){
				resultType = "unknownHost";
			}
			else{
				resultType = "failure";
			}
			return resultType;
		} catch (JSONException e) {
			e.printStackTrace();
			resultType = "failure";
			return resultType;
		}
	}
	
	@Override
	protected void onPostExecute(final String result) {
		if( listener != null){
			listener.onComplete(result);
		}
	}

	@Override
	protected void onCancelled() {
		if( listener != null){
			listener.onCancelled();
		}
	}
	
	public interface AcceptDeclineFriendListener{
		public void onPreExecute();
		public void onComplete(String resultString);
		public void onCancelled();
	}
}
