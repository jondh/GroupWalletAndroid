/**
 * Author: Jonathan Harrison
 * Date: 2/24/14
 * Description: 
 * 
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

public class GetPageUsers extends AsyncTask<String, Void, ArrayList<User>>{
	private DBhttpRequest httpRequest;
	private getUsersListener listener;
	private Profile profile;
	private Integer start;
	private Integer length;
	private String match;
	private ArrayList<Integer> usersExclude;
	
	private String resultType;
	
	public GetPageUsers(DBhttpRequest httpRequest, Profile profile, Integer start, Integer length, String match, ArrayList<Integer> usersExclude){
		this.httpRequest = httpRequest;
		this.profile = profile;
		this.start = start;
		this.length = length;
		this.match = match;
		if(usersExclude == null){ this.usersExclude = new ArrayList<Integer>(); }
		else{ this.usersExclude = usersExclude; }
	}
	
	public void setUsersListener(getUsersListener _listener) {
        this.listener = _listener;
    }
	
	@Override
	protected void onPreExecute(){ 
		listener.getUsersPreExecute();
	}
	
	@Override
	protected ArrayList<User> doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("start", start.toString()));
		nameValuePairs.add(new BasicNameValuePair("length", length.toString()));
		nameValuePairs.add(new BasicNameValuePair("match", match ));
		for(int i = 0; i < usersExclude.size(); i++){
			nameValuePairs.add( new BasicNameValuePair("usersExclude[]", usersExclude.get(i).toString() ) );
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
					JSONArray jObjUsers = jObj.getJSONArray("users");
					for(int i = 0; i < jObjUsers.length(); i++){
						JSONObject jObjUser = jObjUsers.getJSONObject(i);
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
		System.out.println("get user post exe");
		listener.getUsersCompleted(result, resultType);
	}

	@Override
	protected void onCancelled() {
		listener.getUsersCancelled();
	}
	
	public interface getUsersListener{
		public void getUsersPreExecute();
		public void getUsersCompleted(ArrayList<User> Users, String resultString);
		public void getUsersCancelled();
	}
}

