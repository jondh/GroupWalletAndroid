/**
 * Author: Jonathan Harrison
 * Date: 2/24/14
 * Description: 
 * 
 * 
 * 
 * 
 
 		GetWalletRelations getWalletRelations = new GetWalletRelations(httpRequest, profile.getInt("id", 0));
	   	getWalletRelations.setWalletRelationsListener(new getWalletRelationsListener(){

			@Override
			public void getWalletRelationsPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getWalletRelationsComplete(ArrayList<WalletRelation> _amount) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getWalletRelationsCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	getWalletRelations.execute("<url>");
 
 * 
 * 
 * 
 * 
 */

package com.whereone.groupwalletcake;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.WalletRelation;

public class GetWalletRelations extends AsyncTask<String, Void, ArrayList<WalletRelation>>{
	private DBhttpRequest httpRequest;
	private getWalletRelationsListener walletListener;
	private Integer userID;
	private Integer walletID;
	private String publicToken;
	private String privateToken;
	private String timeStamp;
	
	public GetWalletRelations(DBhttpRequest _httpRequest, Integer _userID, Integer _walletID, String public_token, String private_tokenH, String _timeStamp){
		httpRequest = _httpRequest;
		userID = _userID;
		walletID = _walletID;
		publicToken = public_token;
		privateToken = private_tokenH;
		timeStamp = _timeStamp;
	}
	
	public void setWalletID(Integer _walletID){
		walletID = _walletID;
	}
	
	public void setWalletRelationsListener(getWalletRelationsListener _walletListener) {
        this.walletListener = _walletListener;
    }
	
	@Override
	protected void onPreExecute(){ 
		walletListener.getWalletRelationsPreExecute();
	}
	
	@Override
	protected ArrayList<WalletRelation> doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userID.toString()));
		nameValuePairs.add(new BasicNameValuePair("wallet_id", walletID.toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", publicToken));
		nameValuePairs.add(new BasicNameValuePair("private_token", privateToken));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", timeStamp));

		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("GetWalletRelations Result", "WalletRelations: " +  result);
		
		ArrayList<WalletRelation> WalletRelations = new ArrayList<WalletRelation>();
		try {
			JSONObject jObj = new JSONObject(result);
			if(jObj.getString("result").contains("success")){
				JSONArray jArr = jObj.getJSONArray("walletRelations");
				for(int i = 0; i < jArr.length(); i++){
					JSONObject jObject = jArr.getJSONObject(i);
					JSONObject jObjWR = jObject.getJSONObject("WalletRelation");
					WalletRelations.add(new WalletRelation(
							jObjWR.getInt("id"),
							jObjWR.getInt("wallet_id"),
							jObjWR.getInt("user_id")
						)
					);
				}
				return WalletRelations;
			}
			return null;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(final ArrayList<WalletRelation> result) {
		walletListener.getWalletRelationsComplete(result);
	}

	@Override
	protected void onCancelled() {
		walletListener.getWalletRelationsCancelled();
	}
	
	public interface getWalletRelationsListener{
		public void getWalletRelationsPreExecute();
		public void getWalletRelationsComplete(ArrayList<WalletRelation> _amount);
		public void getWalletRelationsCancelled();
	}
}

