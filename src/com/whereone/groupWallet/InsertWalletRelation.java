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
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.WalletRelation;

public class InsertWalletRelation extends AsyncTask<String, Void, WalletRelation>{
	private WalletRelation walletRelation;
	private InsertWalletRelationListener listener;
	
	private DBhttpRequest httpRequest;
	private Profile profile;
	
	private String resultType;
	
	public InsertWalletRelation(DBhttpRequest httpRequest, Profile profile, WalletRelation walletRelation){
		this.httpRequest = httpRequest;
		this.profile = profile;
		this.walletRelation = walletRelation;
	}
	
	public void setinsertWalletRelationListener(InsertWalletRelationListener _listener) {
        this.listener = _listener;
    }
	
	@Override
	protected WalletRelation doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
		nameValuePairs.add(new BasicNameValuePair("walletID", walletRelation.getWalletID().toString() ));
		nameValuePairs.add(new BasicNameValuePair("userID", walletRelation.getUserID().toString() ));
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
				JSONObject jWR = jObject.getJSONObject("walletRelation");
				resultType = "success";
				return new WalletRelation( jWR.getInt("id"),
						jWR.getInt("wallet_id"),
						jWR.getInt("user_id"),
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultType = "failure";
			return null;
		}
		
	}
	
	@Override
	protected void onPostExecute(final WalletRelation result) {
		listener.insertWalletRelationComplete(result, resultType);
	}

	@Override
	protected void onCancelled() {
		listener.insertWalletRelationCancelled();
	}
	
	public interface InsertWalletRelationListener{
		public void insertWalletRelationPreExecute();
		public void insertWalletRelationComplete(WalletRelation result, String resultString);
		public void insertWalletRelationCancelled();
	}
}
