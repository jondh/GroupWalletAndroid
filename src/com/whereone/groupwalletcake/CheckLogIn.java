package com.whereone.groupwalletcake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.whereone.LogOut;
import com.whereone.LogOut.LogOutListener;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;

public class CheckLogIn {
	private SharedPreferences profile;
	private TransactionsController transaction;
	private UsersController user;
	private WalletRelationsController walletRelation;
	private WalletsController wallet;

	public CheckLogIn(SharedPreferences _profile, TransactionsController tc, UsersController uc, 
			WalletRelationsController wrc, WalletsController wc){
		profile = _profile;
		transaction = tc;
		user = uc;
		walletRelation = wrc;
		wallet = wc;
	}

	
	public void checkUser(final Context context, DBhttpRequest httpRequest, String public_token, String private_tokenH, String _timeStamp){
		
		LogOut LogOut = new LogOut(httpRequest, profile.getInt("id", 0), public_token, private_tokenH, _timeStamp);
	   	LogOut.setLogOutListener(new LogOutListener(){
	   		@Override
	   		public void LogOutComplete(Boolean result){
	   			if(!result){
		   			profile.edit().clear();
		   			profile.edit().commit();
		   			transaction.removeAll();
		   			user.removeAll();
		   			walletRelation.removeAll();
		   			wallet.removeAll();
		   			Intent intent = new Intent(context, LoginActivity.class);
		   			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   			context.startActivity(intent);
	   			}
	   		}
	   		@Override
	   		public void LogOutCancelled(){
	   		
	   		}
	   	});
	   	LogOut.execute(context.getString(R.string.checkLogInURL));
	}
}
