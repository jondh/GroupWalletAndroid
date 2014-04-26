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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Friend;
import com.whereone.groupWallet.models.Profile;

public class GetFriends extends AsyncTask<String, Void, ArrayList<Friend>>{
	private DBhttpRequest httpRequest;
	private Profile profile;
	private GetFriendsListener listener;
	private ArrayList<Integer> currentFriends;
	
	private String resultType;
	
	public GetFriends(DBhttpRequest httpRequest, Profile profile, ArrayList<Integer> currentFriends){
		this.httpRequest = httpRequest;
		this.profile = profile;
		this.currentFriends = currentFriends;
	}
	public void setGetFriendsListener(GetFriendsListener _walletListener) {
        this.listener = _walletListener;
    }
	
	@Override
	protected void onPreExecute(){ 
		listener.getFriendsPreExecute();
	}
	
	@Override
	protected ArrayList<Friend> doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		if(currentFriends != null){
			if(currentFriends.size() == 0){
				nameValuePairs.add(new BasicNameValuePair("currentFriends[]", "0"));
			}
			else{
				for(int i = 0; i < currentFriends.size(); i++){
					nameValuePairs.add(new BasicNameValuePair("currentFriends[]", currentFriends.get(i).toString()));
				}
			}
		}
		else{
			nameValuePairs.add(new BasicNameValuePair("currentFriends[]", "0"));
		}
		nameValuePairs.add(new BasicNameValuePair("user_id", profile.getUserID().toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));

		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("GetFriends Result", "Friends: " +  result);
		
		ArrayList<Friend> friends = new ArrayList<Friend>();
		try {
			JSONObject jObj = new JSONObject(result);
			String jResult = jObj.getString("result");
			if(jResult.contains("success")){
				if(!jObj.getBoolean("empty")){
					JSONArray jArr = jObj.getJSONArray("friends");
					for(int i = 0; i < jArr.length(); i++){
						JSONObject jObject = jArr.getJSONObject(i);
						JSONObject jObjWR = jObject.getJSONObject("Friend");
						friends.add(new Friend(
								jObjWR.getInt("id"),
								jObjWR.getInt("user_id_1"),
								jObjWR.getInt("user_id_2"),
								jObjWR.getBoolean("accept")
							)
						);
					}
					resultType = "success";
					return friends;
				}
				else{
					resultType = "empty";
					return friends;
				}
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
			resultType = "failure";
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(final ArrayList<Friend> result) {
		listener.getFriendsComplete(result, resultType);
	}

	@Override
	protected void onCancelled() {
		listener.getFriendsCancelled();
	}
	
	public interface GetFriendsListener{
		public void getFriendsPreExecute();
		public void getFriendsComplete(ArrayList<Friend> friends, String resultString);
		public void getFriendsCancelled();
	}
}

