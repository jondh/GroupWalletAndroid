package com.whereone.groupWallet.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.whereone.groupWallet.GetData;
import com.whereone.groupWallet.GetData.getDataListener;
import com.whereone.groupWallet.LogOutCurrent;
import com.whereone.groupWallet.LogOutCurrent.CheckUserListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.WalletRelation;
import com.whereone.groupwalletcake.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class StartupActivity extends Activity {
	
	protected GWApplication application;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_startup);
		
		application = (GWApplication) getApplication();
		final Context context = this;
		
		final ImageView mySpinner = (ImageView) findViewById(R.id.startup_spinner);
		
		final Spin spinImage = new Spin(context, mySpinner, 100);
		

		final TransactionsController transactionsController = TransactionsController.getInstance();
		final WalletsController walletsController = WalletsController.getInstance();
		final UsersController usersController = UsersController.getInstance();
		final WalletRelationsController walletRelationsController = WalletRelationsController.getInstance();
		
		final DBhttpRequest httpRequest = DBhttpRequest.getInstance();
		
		final Profile profileL = Profile.getInstance();
		
		final GetData getData = new GetData(httpRequest, profileL, transactionsController, walletRelationsController, walletsController, usersController);
		
		final Intent mainIntent = new Intent(this, MainActivity.class);
		final Intent loginIntent = new Intent(this, LoginActivity.class);
	
		
		LogOutCurrent logOut = new LogOutCurrent(httpRequest, profileL, application);
		//logOut.logOut();
		logOut.setCheckUserListener(new CheckUserListener(){

			@Override
			public void checkResult(Boolean result, String reason) {
				System.out.println(result + reason);
				if(result){
					
					mySpinner.setVisibility(View.VISIBLE);
					spinImage.start();
					
					getData.setGetDataListener(new getDataListener(){

						@Override
						public void getWalletComplete(Integer result) {
							
						}

						@Override
						public void getRecordsComplete(Integer result) {
							
						}

						@Override
						public void inviteWalletComplete(Integer result) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void getRelationsComplete(
								ArrayList<Integer> result,
								ArrayList<WalletRelation> newRelations) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void getUsersComplete(Integer result, ArrayList<Integer> results) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void getDataComplete(Boolean walletFailFlag,
								Boolean relationFailFlag,
								Boolean transactionFailFlag,
								Boolean walletInviteFailFlag,
								Boolean usersFailFlag) {
							spinImage.stopRunning();
							startActivity(mainIntent);
						}
						
					});
					getData.checkForNewData();
					
				}
				else{
					spinImage.stopRunning();
					mySpinner.setVisibility(View.INVISIBLE);
					System.out.println("Login Screen");
					startActivity(loginIntent);
				}
			}
			
		});
		logOut.checkUser();
		
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

	}
	
	private class MyInt{
		public Integer myInt;
	}
	
	private class Spin extends Thread{
		private Context context;
		private ImageView imageView;
		private Integer intervalMillis;
		private Boolean start;

		public Spin(Context context, ImageView mySpinner, Integer intervalMillis){
			this.context = context;
			this.imageView = mySpinner;
			this.intervalMillis = intervalMillis;
			((Activity) context).runOnUiThread(new Runnable(){

				@Override
				public void run() {
					imageView.setVisibility(View.INVISIBLE);
				}
				
			});
		}
		
		@Override
		public void run() {
			if(context != null && imageView != null){
				((Activity) context).runOnUiThread(new Runnable(){

					@Override
					public void run() {
						imageView.setVisibility(View.VISIBLE);
					}
					
				});
				start = true;
				final MyInt myInt = new MyInt();
				for(;;){
					if(!start) break;
					for(float i = 0; i < 360; i += 10){
						if(!start) break;
						myInt.myInt = (int) i;
						
				        ((Activity) context).runOnUiThread(new Runnable(){
		
							@Override
							public void run() {
								imageView.setRotation(myInt.myInt);
							}
				        	
				        });
			        
						try {
							Thread.sleep(intervalMillis);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		public void stopRunning(){
			start = false;
			((Activity) context).runOnUiThread(new Runnable(){

				@Override
				public void run() {
					imageView.setVisibility(View.INVISIBLE);
				}
				
			});
			
		}
	}

}
