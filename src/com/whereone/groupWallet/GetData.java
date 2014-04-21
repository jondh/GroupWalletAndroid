package com.whereone.groupWallet;

import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;

import com.whereone.GetGravitarImage;
import com.whereone.GetGravitarImage.GravitarImageListener;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.TransactionsController.TransactionsGetListener;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.UsersController.UsersGetWRListener;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletRelationsController.WalletRelationsGetListener;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.controllers.WalletsController.WalletInviteListener;
import com.whereone.groupWallet.controllers.WalletsController.walletsControllerListener;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.WalletRelation;

public class GetData {
	
	private getDataListener listener;
	
	private DBhttpRequest httpRequest;
	private Profile profile;
	private TransactionsController transactionsController;
	private WalletRelationsController walletRelationsController;
	private WalletsController walletsController;
	private UsersController usersController;
	private Boolean walletFlag;
	private Boolean relationFlag;
	private Boolean transactionFlag;
	private Boolean walletInvitesFlag;
	private Boolean usersFlag;
	
	private Completion completion;
	
	private final Semaphore checkAccess = new Semaphore(1, true);

	public GetData(DBhttpRequest httpRequest, Profile profile,
			TransactionsController transactionsController,
			WalletRelationsController walletRelationsController,
			WalletsController walletsController,
			UsersController usersController){
		
		this.httpRequest = httpRequest;
		this.profile = profile;
		this.transactionsController = transactionsController;
		this.walletRelationsController = walletRelationsController;
		this.walletsController = walletsController;
		this.usersController = usersController;
		
		this.walletFlag = false;
		this.relationFlag = false;
		this.transactionFlag = false;
		this.walletInvitesFlag = false;
	}
	
	public void setGetDataListener(getDataListener _listener){
		this.listener = _listener;
	}
	
	public void checkForNewData(){
		completion = new Completion();
		
		if(completion.cancel){
			completion.override = true;
			completion.check();
			return;
		}
		
		walletsController.findWallets(httpRequest, walletRelationsController, profile, profile.getUserID());
		
		walletsController.setWalletsControllerListener(new walletsControllerListener(){

			@Override
			public void getWalletComplete(Integer result) {
				listener.getWalletComplete(result);
				
				if(result == -2 || result == -3){
					walletFlag = true;
				}
				
				completion.gotWallets = true;
				
				if(walletsController.getWalletsUserID(profile.getUserID(), walletRelationsController, 1).size() == 0){
					completion.override = true;
					getWalletInvites(profile.getUserID(), completion);
				}
				else{
					getWalletInvites(profile.getUserID(), completion);
					getRelations(completion, null);
					getTransactions(completion);
				}
			}
			
		}); // END walletsControllerListener
	} // END checkForNewData()
	
	public void checkForNewData(final LruCache<Integer, Drawable> drawableCache){
		
		final Completion completion = new Completion();
		
		if(completion.cancel){
			completion.override = true;
			completion.check();
			return;
		}
		
		walletsController.findWallets(httpRequest, walletRelationsController, profile, profile.getUserID());
		
		walletsController.setWalletsControllerListener(new walletsControllerListener(){
			
			@Override
			public void getWalletComplete(Integer result) {
				listener.getWalletComplete(result);
				
				if(result == -2 || result == -3){
					walletFlag = true;
				}
				
				completion.gotWallets = true;
				if(walletsController.getWalletsUserID(profile.getUserID(), walletRelationsController, 1).size() == 0){
					completion.override = true;
					getWalletInvites(profile.getUserID(), completion);
				}
				else{
					getWalletInvites(profile.getUserID(), completion);
					getRelations(completion, drawableCache);
					getTransactions(completion);
				}
			}
			
		}); // END walletsControllerListener
	} // END checkForNewData()
	
	public void getWallets(Integer userIDfor){
		
		walletsController.findWallets(httpRequest, walletRelationsController, profile, userIDfor);
		
		walletsController.setWalletsControllerListener(new walletsControllerListener(){

			@Override
			public void getWalletComplete(Integer result) {
				listener.getWalletComplete(result);
			}
			
		}); // END walletsControllerListener
	}
	
	public void getWalletInvites(final Integer userIDfor, final Completion completion){
		
		if(completion.cancel){
			completion.gotWalletInvites = true;
			completion.check();
			return;
		}
		
		walletsController.findWalletInvites(httpRequest, walletRelationsController, profile, userIDfor);
		
		walletsController.setWalletInviteListener(new WalletInviteListener(){

			@Override
			public void getWalletInvitesComplete(Integer result) {
				listener.inviteWalletComplete(result);
				
				if(completion != null){
					if(result == -2 || result == -3){
						walletInvitesFlag = true;
					}
					completion.gotWalletInvites = true;
					completion.check();
				}
			}
			
		});
	}
	
	public void getTransactions(){
	
		transactionsController.findTransactions(httpRequest, profile, walletRelationsController.getWalletsForUser(profile.getUserID(), 1));
	
		
		transactionsController.setTransactionsGetListener(new TransactionsGetListener(){

			@Override
			public void getComplete(Integer result) {
				listener.getRecordsComplete(result);
			}
			
		}); // END transactionsControllerListener
	}
	
	private void getTransactions(final Completion completion){
		
		if(completion.cancel){
			completion.gotTransactions = true;
			completion.check();
			return;
		}
		
		transactionsController.findTransactions(httpRequest, profile, walletRelationsController.getWalletsForUser(profile.getUserID(), 1));
	
		transactionsController.setTransactionsGetListener(new TransactionsGetListener(){

			@Override
			public void getComplete(Integer result) {
				listener.getRecordsComplete(result);
				Log.i("trans getComplete", result+"");
				if(result == 0 || result == 1){
					completion.gotTransactions = true;
					completion.check();
				}
				if(result == -2 || result == -3){
					transactionFlag = true;
					completion.gotTransactions = true;
					completion.check();
				}
			}
			
		}); // END transactionsControllerListener
	}
	
	public void getRelations(){
		
		walletRelationsController.setWalletRelationsGetListener(new WalletRelationsGetListener(){

			@Override
			public void getRelationsComplete(ArrayList<Integer> result, ArrayList<WalletRelation> newRelations) {
				listener.getRelationsComplete(result, newRelations);
			}

			
		}); // END walletRelationsControllerListener

		walletRelationsController.findWalletRelations(httpRequest, profile, walletRelationsController.getWalletsForUser(profile.getUserID(), 1));
		
	}
	
	public void getRelations(ArrayList<Integer> wallets){
		
		walletRelationsController.setWalletRelationsGetListener(new WalletRelationsGetListener(){

			@Override
			public void getRelationsComplete(ArrayList<Integer> result, ArrayList<WalletRelation> newRelations) {
				listener.getRelationsComplete(result, newRelations);
			}

			
		}); // END walletRelationsControllerListener

		walletRelationsController.findWalletRelations(httpRequest, profile, wallets);
	}
	
	public void getRelationsAndUsers(ArrayList<Integer> wallets){
		if(wallets == null) return;
		walletRelationsController.setWalletRelationsGetListener(new WalletRelationsGetListener(){

			@Override
			public void getRelationsComplete(ArrayList<Integer> result, ArrayList<WalletRelation> newRelations) {
				listener.getRelationsComplete(result, newRelations);
				
				getUsersForWR(newRelations);
			}

			
		}); // END walletRelationsControllerListener

		walletRelationsController.findWalletRelations(httpRequest, profile, wallets);
	}

	private void getRelations(final Completion completion, final LruCache<Integer, Drawable> drawableCache){
		
		if(completion.cancel){
			completion.gotRelations = true;
			completion.gotUsers = true;
			completion.check();
			return;
		}
		
	
		walletRelationsController.setWalletRelationsGetListener(new WalletRelationsGetListener(){

			@Override
			public void getRelationsComplete(ArrayList<Integer> result,	ArrayList<WalletRelation> newRelations) {
			
				listener.getRelationsComplete(result, newRelations);

				getUsersForWR(completion,newRelations);
				
				if(result.contains(-2) || result.contains(-3)){
					relationFlag = true;
				}
				
				completion.gotRelations = true;
				completion.check();
					
			}
			
		}); // END walletRelationsControllerListener
		
		// get two sets of relations, the active ones and the pending ones
		walletRelationsController.findWalletRelations(httpRequest, profile, walletRelationsController.getWalletsForUser(profile.getUserID(), 1));
		//walletRelationsController.findPendingWalletRelations(httpRequest, profile, walletRelationsController.getWalletsForUser(profile.getUserID(), 1));
	}
	
	public void getUsersForWR(ArrayList<WalletRelation> wrs){
		usersController.setUsersGetWRListener(new UsersGetWRListener(){

			@Override
			public void getWRUsersComplete(Integer result, ArrayList<Integer> resultUsers) {
				listener.getUsersComplete(result, resultUsers);
			}
			
		});
		
		usersController.findUsersForWRs(httpRequest, profile, wrs);
	}
	
	public void getUsersForWR(final Completion completion, ArrayList<WalletRelation> wrs){
		
		if(completion.cancel){
			completion.gotUsers = true;
			completion.check();
			return;
		}

		usersController.setUsersGetWRListener(new UsersGetWRListener(){

			@Override
			public void getWRUsersComplete(Integer result, ArrayList<Integer> resultUsers) {
				listener.getUsersComplete(result, resultUsers);
				
				if(result == -2 || result == -3){
					usersFlag = true;	
				}
				
				completion.gotUsers = true;
				completion.check();
			}
			
		});
		
		usersController.findUsersForWRs(httpRequest, profile, wrs);
	}
	
	public void cancel(){
		if(completion != null){
			completion.cancel = true;
		}
	}
	
	public interface getDataListener{
		public void getWalletComplete(Integer result);
		public void inviteWalletComplete(Integer result);
		public void getRecordsComplete(Integer result);
		public void getRelationsComplete(ArrayList<Integer> result, ArrayList<WalletRelation> newRelations);
		public void getUsersComplete(Integer result, ArrayList<Integer> results);
		public void getDataComplete(Boolean walletFailFlag, Boolean relationFailFlag, Boolean transactionFailFlag, Boolean walletInviteFailFlag, Boolean usersFailFlag);
	}
	
	public void loadPic(String email, final Integer userID, final Completion completion, final LruCache<Integer, Drawable> drawableCache){
		if(drawableCache.get(userID) != null) return;
		GetGravitarImage getGravitarImage = new GetGravitarImage(email);
		getGravitarImage.setGravitarImageListener(new GravitarImageListener(){

			@Override
			public void getImageComplete(Drawable result) {
				if(result != null){
					drawableCache.put(userID, result);
				}
				completion.check();
			}
			
		});
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
			try{
				getGravitarImage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 100);
			}
			catch(RejectedExecutionException e){
				System.out.println("executorException");
			}
	   	}
	   	else {
	   		getGravitarImage.execute(100);
	   	}
	}
	
	private class Completion{
		public Boolean gotWallets = false;
		public Boolean gotWalletInvites = false;
		public Boolean gotTransactions = false;
		public Boolean gotRelations = false;
		public Boolean gotUsers = false;
		public Boolean override = false;
		public Boolean cancel = false;
		
		public Completion(){ 
			gotWallets = false;
			gotTransactions = false;
			gotRelations = false;
			gotWalletInvites = false;
			gotUsers = false;
			cancel = false;
		}
		
		public void check(){
			try {
				checkAccess.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("check", gotWallets + " " + gotRelations + " " + gotTransactions + " " + gotWalletInvites + " " + gotUsers);
			if(override){
				System.out.println("COMPLETE OVERRIDE");
				listener.getDataComplete(walletFlag, relationFlag, transactionFlag, walletInvitesFlag, usersFlag);
			}
			if(gotWallets && gotRelations && gotTransactions && gotWalletInvites && gotUsers){	
				System.out.println("COMPLETE COMPLETE");
				listener.getDataComplete(walletFlag, relationFlag, transactionFlag, walletInvitesFlag, usersFlag);	
			}
			checkAccess.release();
		}
	}
}
