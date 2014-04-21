package com.whereone.groupWallet.activities;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import com.whereone.groupWallet.LogOutCurrent;
import com.whereone.groupWallet.LogOutCurrent.CheckUserListener;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.models.Profile;

public class GWApplication extends Application{

	private SharedPreferences storedProfile;
	private TransactionsController transactionsController;
	private WalletsController walletsController;
	private UsersController usersController;
	private WalletRelationsController walletRelationsController;
	
	public LruCache<Integer, Drawable> drawableCache;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		storedProfile = this.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		initSingletons();
		initProfile();
		
		transactionsController = TransactionsController.getInstance();
		walletsController = WalletsController.getInstance();
		usersController = UsersController.getInstance();
		walletRelationsController = WalletRelationsController.getInstance();
		
		drawableCache = new LruCache<Integer, Drawable>(20);
	}
	
	public void initSingletons(){
		Profile.init();
		DBhttpRequest.init();
		TransactionsController.init(this);
		UsersController.init(this);
		WalletRelationsController.init(this);
		WalletsController.init(this);
	}
	
	public void clearData(){
		transactionsController.removeAll();
		walletsController.removeAll();
		usersController.removeAll();
		walletRelationsController.removeAll();
		storedProfile.edit().clear();
		storedProfile.edit().commit();
	}
	
	public void checkLoginStatus(DBhttpRequest httpRequest, Profile profile){
		LogOutCurrent logOut = new LogOutCurrent(httpRequest, profile, this);
		
		logOut.setCheckUserListener(new CheckUserListener(){

			@Override
			public void checkResult(Boolean result, String reason) {
				System.out.println(result + reason);
			}
			
		});
		logOut.checkUser();
	}
	
	public void initProfile(){
		Profile.getInstance().setProfile(storedProfile.getInt("id", 0),
		storedProfile.getString("username", ""),
		storedProfile.getString("password", ""),
		storedProfile.getString("firstName", ""),
		storedProfile.getString("lastName", ""),
		storedProfile.getString("email", ""),
		storedProfile.getString("fbID", ""),
		storedProfile.getString("privateToken", ""),
		storedProfile.getString("publicToken", "") );
	}
	
}
