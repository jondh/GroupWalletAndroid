package com.whereone.groupWallet;

import android.content.Intent;

import com.facebook.Session;
import com.whereone.LogInStatus;
import com.whereone.LogInStatus.LogInStatusListener;
import com.whereone.LogOut;
import com.whereone.LogOut.LogOutListener;
import com.whereone.groupWallet.activities.GWApplication;
import com.whereone.groupWallet.activities.LoginActivity;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Profile;

public class LogOutCurrent {
	private GWApplication app;
	private DBhttpRequest httpRequest;
	private Profile profile;
	private CheckUserListener listener;

	public LogOutCurrent(DBhttpRequest httpRequest, Profile profile, GWApplication app){
		this.app = app;
		this.httpRequest = httpRequest;
		this.profile = profile;
	}
	
	public void setCheckUserListener(CheckUserListener listener){
		this.listener = listener;
	}

	public interface CheckUserListener{
		public void checkResult(Boolean result, String reason);
	}
	
	
	public void checkUser(){
		if((profile.getUserID() <= 0) || (profile.getUserName().contentEquals(""))){
			listener.checkResult(false, "bad stored profile");
			return;
		}
		
		LogInStatus status = new LogInStatus(httpRequest, profile);
		status.setLogInStatusListener(new LogInStatusListener(){

			@Override
			public void LogInStatusComplete(Boolean result, String resultString) {
				if(result){
					listener.checkResult(true, "success");
				}
				else listener.checkResult(false, resultString);
			}

			@Override
			public void LogInStatusCancelled() {
				listener.checkResult(true, "cancelled");
			}
			
		});
		status.execute(app.getString(R.string.checkLogInURL));
	}
	
	
	public void logOut(){
		
		LogOut LogOut = new LogOut(httpRequest, profile);
	   	LogOut.setLogOutListener(new LogOutListener(){
	   		@Override
	   		public void LogOutComplete(Boolean result){
	   			if(result){
	   				Session session = Session.getActiveSession();
	   				if(session != null){
	   					session.closeAndClearTokenInformation(); 
	   				}
		   			app.clearData();
		   			Intent intent = new Intent(app, LoginActivity.class);
		   			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   			app.startActivity(intent);
	   			}
	   		}
	   		@Override
	   		public void LogOutCancelled(){
	   		
	   		}
	   	});
	   	LogOut.execute(app.getString(R.string.logOutURL));
	}
}
