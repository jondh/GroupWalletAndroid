/**
 * Author: Jonathan Harrison
 * Date: 2/28/14
 * Description: 
 * 
 * 
 * 
 * 
 
 		InsertRecord insertRecord = new InsertRecord(Integer _userID, Integer _otherUID, Double _amount, Integer _walletID, String _Comments, Boolean _owe, DBhttpRequest httpRequest);
	   	insertRecord.setInsertRecordListener(new insertRecordListener(){

			@Override
			public void insertRecordPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertRecordComplete(Boolean result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertRecordCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	insertRecord.execute("<url>");
 
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
import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupWallet.models.WalletRelation;

public class InsertWallet extends AsyncTask<String, Void, Wallet>{
	private String name;
	
	private InsertWalletListener listener;
	
	private DBhttpRequest httpRequest;
	private Profile profile;
	
	private String resultType;
	private WalletRelation walletR;
	
	public InsertWallet(DBhttpRequest httpRequest, Profile profile, String _name){
		this.httpRequest = httpRequest;
		this.profile = profile;
		this.name = _name;
	}
	
	public void setInsertWalletListener(InsertWalletListener _listener) {
		this.listener = _listener;
    }
	
	@Override
	protected Wallet doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
		nameValuePairs.add(new BasicNameValuePair("name", name));
		nameValuePairs.add(new BasicNameValuePair("userID", profile.getUserID().toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));
		
		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("InsertWallet Result", result);
		try {
			JSONObject jObject = new JSONObject(result);
			String jResult = jObject.getString("result");
			if(jResult.contains( "success" )){
				JSONObject jWallet = jObject.getJSONObject("wallet");
				JSONObject jWR = jObject.getJSONObject("relation");
				walletR = new WalletRelation( jWR.getInt("id"),
						jWR.getInt("wallet_id"),
						jWR.getInt("user_id"),
						jWR.getBoolean("accept"));
				resultType = "success";
				return new Wallet(jWallet.getInt("id"),
						jWallet.getString("name"),
						jWallet.getString("date"),
						jWallet.getInt("user_id"));
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
	protected void onPostExecute(final Wallet result) {
		if( listener != null){
			listener.insertWalletComplete(result, walletR, resultType);
		}
	}

	@Override
	protected void onCancelled() {
		if( listener != null){
			listener.insertWalletCancelled();
		}
	}
	
	public interface InsertWalletListener{
		public void insertWalletPreExecute();
		public void insertWalletComplete(Wallet result, WalletRelation walletR, String resultString);
		public void insertWalletCancelled();
	}
}
