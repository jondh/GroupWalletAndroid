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
import com.whereone.groupWallet.models.WalletRelation;

public class GetWalletRelations extends AsyncTask<String, Void, ArrayList<WalletRelation>>{
	private DBhttpRequest httpRequest;
	private Profile profile;
	private getWalletRelationsListener walletListener;
	private ArrayList<Integer> currentWRs;
	private Integer walletID;
	
	private String resultType;
	
	public GetWalletRelations(DBhttpRequest httpRequest, Profile profile, ArrayList<Integer> _currentWRs, Integer _walletID){
		this.httpRequest = httpRequest;
		currentWRs = _currentWRs;
		walletID = _walletID;
		this.profile = profile;
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
		if(currentWRs != null){
			for(int i = 0; i < currentWRs.size(); i++){
				nameValuePairs.add(new BasicNameValuePair("currentRelations[]", currentWRs.get(i).toString()));
			}
		}
		else{
			nameValuePairs.add(new BasicNameValuePair("currentRelations[]", "0"));
		}
		nameValuePairs.add(new BasicNameValuePair("user_id", profile.getUserID().toString()));
		nameValuePairs.add(new BasicNameValuePair("wallet_id", walletID.toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));

		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("GetWalletRelations Result", "WalletRelations: " +  result);
		
		ArrayList<WalletRelation> WalletRelations = new ArrayList<WalletRelation>();
		try {
			JSONObject jObj = new JSONObject(result);
			String jResult = jObj.getString("result");
			if(jResult.contains("success")){
				if(!jObj.getBoolean("empty")){
					JSONArray jArr = jObj.getJSONArray("walletRelations");
					for(int i = 0; i < jArr.length(); i++){
						JSONObject jObject = jArr.getJSONObject(i);
						JSONObject jObjWR = jObject.getJSONObject("WalletRelation");
						WalletRelations.add(new WalletRelation(
								jObjWR.getInt("id"),
								jObjWR.getInt("wallet_id"),
								jObjWR.getInt("user_id"),
								jObjWR.getBoolean("accept")
							)
						);
					}
					resultType = "success";
					return WalletRelations;
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
			resultType = "failure";
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(final ArrayList<WalletRelation> result) {
		walletListener.getWalletRelationsComplete(result, resultType);
	}

	@Override
	protected void onCancelled() {
		walletListener.getWalletRelationsCancelled();
	}
	
	public interface getWalletRelationsListener{
		public void getWalletRelationsPreExecute();
		public void getWalletRelationsComplete(ArrayList<WalletRelation> _amount, String resultString);
		public void getWalletRelationsCancelled();
	}
}

