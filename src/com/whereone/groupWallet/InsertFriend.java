/**
 * Author: Jonathan Harrison
 * Date: 4/16/14
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

public class InsertFriend extends AsyncTask<String, Void, Friend>{
	private Friend friend;
	private InsertFriendListener listener;
	
	private DBhttpRequest httpRequest;
	private Profile profile;
	
	private String resultType;
	
	public InsertFriend(DBhttpRequest httpRequest, Profile profile, Friend friend){
		this.httpRequest = httpRequest;
		this.profile = profile;
		this.friend = friend;
	}
	
	public void setInsertFriendListener(InsertFriendListener _listener) {
        this.listener = _listener;
    }
	
	@Override
	protected Friend doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
		nameValuePairs.add(new BasicNameValuePair("user1", friend.getUser1().toString() ));
		nameValuePairs.add(new BasicNameValuePair("user2", friend.getUser2().toString() ));
		nameValuePairs.add(new BasicNameValuePair("accept", "0"));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));
		
		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("InsertWalletRelation Result", result);
		
		try {
			JSONObject jObject = new JSONObject(result);
			String jResult = jObject.getString("result");
			if(jResult.contains( "success" )) {
				JSONObject jWR = jObject.getJSONObject("friend");
				resultType = "success";
				return new Friend( jWR.getInt("id"),
						jWR.getInt("user_id_1"),
						jWR.getInt("user_id_2"),
						jWR.getBoolean("accept"));
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
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			resultType = "failure";
			return null;
		}
		
	}
	
	@Override
	protected void onPostExecute(final Friend result) {
		listener.insertFriendComplete(result, resultType);
	}

	@Override
	protected void onCancelled() {
		listener.insertFriendCancelled();
	}
	
	public interface InsertFriendListener{
		public void insertFriendPreExecute();
		public void insertFriendComplete(Friend result, String resultString);
		public void insertFriendCancelled();
	}
}
