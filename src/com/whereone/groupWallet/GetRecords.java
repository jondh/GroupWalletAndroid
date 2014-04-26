/**
 * Author: Jonathan Harrison
 * Date: 2/24/14
 * Description: 
 * 
 * 
 * 
 * 
 
 		GetRecords getRecords = new GetRecords(httpRequest, profile.getInt("id", 0));
	   	getRecords.setRecordsListener(new getRecordsListener(){

			@Override
			public void getRecordsPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getRecordsComplete(ArrayList<Record> _amount) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getRecordsCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	getRecords.execute("<url>");
 
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
import com.whereone.groupWallet.models.Record;

public class GetRecords extends AsyncTask<String, Void, ArrayList<Record>>{
	private getRecordsListener listener;
	private DBhttpRequest httpRequest;
	private Profile profile;
	private ArrayList<Integer> walletIds;
	private ArrayList<Integer> currentRecords;
	
	private String resultType;
	
	/*
	 *  Note: two options here for getting all records user is associated with (directly or in wallet)
	 *  	1) let the server find all user wallets to get all records
	 *  	2) provide list of wallet ids user is in
	 *   * option (2) being used now -> note that walletList may not be updated
	 */
	public GetRecords(DBhttpRequest httpRequest, Profile profile, ArrayList<Integer> wallets, ArrayList<Integer> _currentRecords){
		this.httpRequest = httpRequest;
		this.profile = profile;
		walletIds = wallets;
		currentRecords = _currentRecords;
	}
	
	public void setRecordsListener(getRecordsListener _listener) {
        this.listener = _listener;
    }
	
	@Override
	protected void onPreExecute(){ 
		listener.getRecordsPreExecute();
	}
	
	@Override
	protected ArrayList<Record> doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		//nameValuePairs.add(new BasicNameValuePair("user_id", userID.toString()));
		if(walletIds == null){
			walletIds = new ArrayList<Integer>();
			walletIds.add(0);
		}
		else if(walletIds.size() == 0){
			walletIds.add(0);
		}
		for(int i = 0; i < walletIds.size(); i++){
			nameValuePairs.add(new BasicNameValuePair("wallets[]", walletIds.get(i).toString()));
		}
		if(currentRecords != null){
			for(int i = 0; i < currentRecords.size(); i++){
				nameValuePairs.add(new BasicNameValuePair("currentRecords[]", currentRecords.get(i).toString()));
			}
		}
		else{
			nameValuePairs.add(new BasicNameValuePair("currentRecords[]", "0"));
		}
		nameValuePairs.add(new BasicNameValuePair("userID", profile.getUserID().toString() ));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));

		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("GetRecords Result", "Records: " +  result);
		
		ArrayList<Record> Records = new ArrayList<Record>();
		try {
			JSONObject jObj = new JSONObject(result);
			String jResult = jObj.getString("result");
			if(jResult.contains("success")){
				resultType = "success";
				if(!jObj.getBoolean("empty")){
					JSONArray jArr = jObj.getJSONArray("records");
					for(int i = 0; i < jArr.length(); i++){
						JSONObject jObject = jArr.getJSONObject(i);
						JSONObject jObjR = jObject.getJSONObject("Transaction");
						Records.add(new Record(
								jObjR.getInt("transaction_id"),
								jObjR.getInt("oweUID"),
								jObjR.getInt("owedUID"),
								jObjR.getDouble("amount"),
								jObjR.getInt("wallet_id"),
								jObjR.getString("comments"),
								jObjR.getString("dateTime")
							)
						);
					}
					return Records;
				}
				else{
					resultType = "empty";
					return null;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultType = "failure";
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(final ArrayList<Record> result) {
		listener.getRecordsComplete(result, resultType);
	}

	@Override
	protected void onCancelled() {
		listener.getRecordsCancelled();
	}
	
	public interface getRecordsListener{
		public void getRecordsPreExecute();
		public void getRecordsComplete(ArrayList<Record> _amount, String resultType);
		public void getRecordsCancelled();
	}
}

