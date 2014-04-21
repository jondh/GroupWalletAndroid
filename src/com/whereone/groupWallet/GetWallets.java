/**
 * Author: Jonathan Harrison
 * Date: 2/23/14
 * Description: 
 * 
 * 
 * 
 * 
 
 		GetWallets getWallets = new GetWallets(httpRequest, profile.getInt("id", 0));
	   	getWallets.setWalletsListener(new getWalletsListener(){

			@Override
			public void getWalletsPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getWalletsComplete(ArrayList<Wallet> _amount) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getWalletsCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	getWallets.execute("<url>");
 
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
import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupWallet.models.WalletRelation;

public class GetWallets extends AsyncTask<String, Void, ArrayList<Wallet>>{
	private DBhttpRequest httpRequest;
	private Profile profile;
	private getWalletsListener walletListener;
	private ArrayList<Integer> currentWallets;
	private ArrayList<WalletRelation> walletRelations;
	private Integer userID;
	private Integer accept;
	
	private String resultType;
	
	public GetWallets(DBhttpRequest httpRequest, Profile profile, ArrayList<Integer> _currentWallets, Integer _userID, Integer accept){
		this.httpRequest = httpRequest;
		this.profile = profile;
		currentWallets = _currentWallets;
		userID = _userID;
		this.accept = accept;
	}
	
	public void setWalletsListener(getWalletsListener _walletListener) {
        this.walletListener = _walletListener;
    }
	
	@Override
	protected void onPreExecute(){ 
		walletListener.getWalletsPreExecute();
	}
	
	@Override
	protected ArrayList<Wallet> doInBackground(String... arg0) {
		String url = arg0[0];
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		if(currentWallets != null){
			for(int i = 0; i < currentWallets.size(); i++){
				nameValuePairs.add(new BasicNameValuePair("currentWallets[]", currentWallets.get(i).toString()));
			}
		}
		else{
			nameValuePairs.add(new BasicNameValuePair("currentWallets[]", "0"));
		}
		nameValuePairs.add(new BasicNameValuePair("user_id", userID.toString()));
		nameValuePairs.add(new BasicNameValuePair("accept", accept.toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", profile.getPublicToken()));
		nameValuePairs.add(new BasicNameValuePair("private_token", profile.hashedPrivate()));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", profile.getCurrentDate()));

		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("GetWallets Result "+accept, "wallets: " +  result);
		
		ArrayList<Wallet> wallets = new ArrayList<Wallet>();
		try {
			JSONObject jObjR = new JSONObject(result);
			String jResult = jObjR.getString("result");
			if(jResult.contains( "success" )){
				if(!jObjR.getBoolean("empty")){
				JSONArray jArr = jObjR.getJSONArray("wallets");
					walletRelations = new ArrayList<WalletRelation>();
					for(int i = 0; i < jArr.length(); i++){
						JSONObject jObject = jArr.getJSONObject(i);
						JSONObject jObj = jObject.getJSONObject("Wallet");
						wallets.add(new Wallet(
								jObj.getInt("id"),
								jObj.getString("name"),
								jObj.getString("date"),
								jObj.getInt("user_id")
							)
						);
						
						JSONObject jWR = jObject.getJSONObject("WalletRelation");
						walletRelations.add( new WalletRelation(jWR.getInt("id"),
								jWR.getInt("wallet_id"),
								jWR.getInt("user_id"),
								jWR.getBoolean("accept")) );
					}
					resultType = "success";
					return wallets;
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
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(final ArrayList<Wallet> result) {
		walletListener.getWalletsComplete(result, walletRelations, resultType);
	}

	@Override
	protected void onCancelled() {
		walletListener.getWalletsCancelled();
	}
	
	public interface getWalletsListener{
		public void getWalletsPreExecute();
		public void getWalletsComplete(ArrayList<Wallet> _amount, ArrayList<WalletRelation> wrs, String resultString);
		public void getWalletsCancelled();
	}
}

