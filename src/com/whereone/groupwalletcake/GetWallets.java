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
import com.whereone.groupWallet.models.Wallet;

public class GetWallets extends AsyncTask<String, Void, ArrayList<Wallet>>{
	private DBhttpRequest httpRequest;
	private getWalletsListener walletListener;
	private Integer userID;
	private String publicToken;
	private String privateToken;
	private String timeStamp;
	
	public GetWallets(DBhttpRequest _httpRequest, Integer _userID, String public_token, String private_tokenH, String _timeStamp){
		httpRequest = _httpRequest;
		userID = _userID;
		publicToken = public_token;
		privateToken = private_tokenH;
		timeStamp = _timeStamp;
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
		nameValuePairs.add(new BasicNameValuePair("user_id", userID.toString()));
		nameValuePairs.add(new BasicNameValuePair("public_token", publicToken));
		nameValuePairs.add(new BasicNameValuePair("private_token", privateToken));
		nameValuePairs.add(new BasicNameValuePair("timeStamp", timeStamp));

		String result = httpRequest.sendRequest(nameValuePairs, url);
		Log.i("GetWallets Result", "wallets: " +  result);
		
		ArrayList<Wallet> wallets = new ArrayList<Wallet>();
		try {
			JSONObject jObjR = new JSONObject(result);
			String res = jObjR.getString("result");
			if(res.contains( "success" )){
				JSONArray jArr = jObjR.getJSONArray("wallets");
				for(int i = 0; i < jArr.length(); i++){
					JSONObject jObject = jArr.getJSONObject(i);
					JSONObject jObj = jObject.getJSONObject("Wallet");
					wallets.add(new Wallet(
							jObj.getInt("id"),
							jObj.getString("name"),
							jObj.getString("date")
						)
					);
				}
				return wallets;
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
		walletListener.getWalletsComplete(result);
	}

	@Override
	protected void onCancelled() {
		walletListener.getWalletsCancelled();
	}
	
	public interface getWalletsListener{
		public void getWalletsPreExecute();
		public void getWalletsComplete(ArrayList<Wallet> _amount);
		public void getWalletsCancelled();
	}
}

