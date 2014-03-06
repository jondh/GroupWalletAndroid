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

package com.whereone.groupwalletcake;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.whereone.groupWallet.controllers.DBhttpRequest;

public class InsertRecord extends AsyncTask<String, Void, Boolean>{
	private Integer userID;
	private Integer otherUID;
	private Double amount;
	private Integer walletID;
	private String comments;
	private Integer owe;
	private DBhttpRequest httpRequest;
	private insertRecordListener listener;
	private String publicToken;
	private String privateToken;
	private String timeStamp;
	
	public InsertRecord(Integer _userID, Integer _otherUID, Double _amount, Integer _walletID, 
			String _Comments, Boolean _owe, DBhttpRequest _http, String public_token, String private_tokenH, String _timeStamp){
		userID = _userID;
		otherUID = _otherUID;
		amount = _amount;
		walletID = _walletID;
		comments = _Comments;
		if(_owe) owe = 1;
		else owe = 0;
		httpRequest = _http;
		publicToken = public_token;
		privateToken = private_tokenH;
		timeStamp = _timeStamp;
	}
	
	public void setInsertRecordListener(insertRecordListener _listener) {
        this.listener = _listener;
    }
	
	@Override
	protected Boolean doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
		nameValuePairs.add(new BasicNameValuePair("userID", userID.toString()));
		nameValuePairs.add(new BasicNameValuePair("otherUID", otherUID.toString()));
		nameValuePairs.add(new BasicNameValuePair("amount", amount.toString()));
		nameValuePairs.add(new BasicNameValuePair("walletID", walletID.toString()));
		nameValuePairs.add(new BasicNameValuePair("comments", comments));
		nameValuePairs.add(new BasicNameValuePair("owe", owe.toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", publicToken));
		nameValuePairs.add(new BasicNameValuePair("private_token", privateToken));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", timeStamp));
		
		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("InsertRecord Result", result);
		try {
			JSONObject jObject = new JSONObject(result);
			String res = jObject.getString("result");
			if(res.contains( "success" )) return true;
			else return false;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	protected void onPostExecute(final Boolean result) {
		listener.insertRecordComplete(result);
	}

	@Override
	protected void onCancelled() {
		listener.insertRecordCancelled();
	}
	
	public interface insertRecordListener{
		public void insertRecordPreExecute();
		public void insertRecordComplete(Boolean result);
		public void insertRecordCancelled();
	}
}
