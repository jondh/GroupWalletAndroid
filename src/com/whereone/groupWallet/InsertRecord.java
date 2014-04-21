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

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Profile;

public class InsertRecord extends AsyncTask<String, Void, Boolean>{
	private Integer userID;
	private Integer otherUID;
	private String amount;
	private Integer walletID;
	private String comments;
	private Integer owe;
	private insertRecordListener listener;
	
	private DBhttpRequest httpRequest;
	private Profile profile;
	
	private String resultType;
	
	public InsertRecord(DBhttpRequest httpRequest, Profile profile, Integer _userID, Integer _otherUID, Double _amount, Integer _walletID, String _Comments, Boolean _owe){
		this.httpRequest = httpRequest;
		this.profile = profile;
		userID = _userID;
		otherUID = _otherUID;
		DecimalFormat df = new DecimalFormat("#0.00");
		amount = df.format(_amount);
		walletID = _walletID;
		comments = _Comments;
		if(_owe) owe = 1;
		else owe = 0;
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
		nameValuePairs.add(new BasicNameValuePair("amount", amount));
		nameValuePairs.add(new BasicNameValuePair("walletID", walletID.toString()));
		nameValuePairs.add(new BasicNameValuePair("comments", comments));
		nameValuePairs.add(new BasicNameValuePair("owe", owe.toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));
		
		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("InsertRecord Result", result);
		try {
			JSONObject jObject = new JSONObject(result);
			String jResult = jObject.getString("result");
			if(jResult.contains( "success" )) return true;
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
			return false;
		}
	}
	
	@Override
	protected void onPostExecute(final Boolean result) {
		listener.insertRecordComplete(result, resultType);
	}

	@Override
	protected void onCancelled() {
		listener.insertRecordCancelled();
	}
	
	public interface insertRecordListener{
		public void insertRecordPreExecute();
		public void insertRecordComplete(Boolean result, String resultString);
		public void insertRecordCancelled();
	}
}
