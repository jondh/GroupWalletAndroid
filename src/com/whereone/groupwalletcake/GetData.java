package com.whereone.groupwalletcake;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.SharedPreferences;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.TransactionsController.transactionsControllerListener;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletRelationsController.walletRelationsControllerListener;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.controllers.WalletsController.walletsControllerListener;

public class GetData {
	private DBhttpRequest httpRequest;
	private SharedPreferences profile;
	private TransactionsController transactionTable;
	private UsersController userTable;
	private WalletRelationsController walletRelationTable;
	private WalletsController walletTable;
	
	private getDataListener listener;

	public GetData(DBhttpRequest _httpRequest, SharedPreferences _profile, TransactionsController _transactionTable, UsersController _userTable, WalletRelationsController _walletRelationTable, WalletsController _walletTable){
		httpRequest = _httpRequest;
		profile = _profile;
		transactionTable = _transactionTable;
		userTable = _userTable;
		walletRelationTable = _walletRelationTable;
		walletTable = _walletTable;
	}
	
	public void setGetDataListener(getDataListener _listener){
		this.listener = _listener;
	}
	
	public void checkForNewData(){
		
		try {
			SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
			String currentDate = s.format(new Date());
			walletTable.findWallets(profile.getInt("id", 0), httpRequest, 
					profile.getString("publicToken", ""), computeHash((profile.getString("privateToken", "") + currentDate)), currentDate);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		walletTable.setWalletsControllerListener(new walletsControllerListener(){

			@Override
			public void insertWalletsAsyncComplete() {
				listener.insertWalletComplete();
				try {
					SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
					String currentDate = s.format(new Date());
					transactionTable.findTransactions(walletTable.getWalletIds(), httpRequest,
							profile.getString("publicToken", ""), computeHash((profile.getString("privateToken", "") + currentDate)), currentDate);
					walletRelationTable.findWalletRelations(walletTable.getWalletIds(), profile.getInt("id", 0), userTable, httpRequest,
							profile.getString("publicToken", ""), computeHash((profile.getString("privateToken", "") + currentDate)), currentDate);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				transactionTable.setTransactionsControllerListener(new transactionsControllerListener(){

					@Override
					public void insertComplete() {
						listener.insertRecordsComplete();
					}

					@Override
					public void getComplete(Integer result) {
						listener.getRecordsComplete(result);
					}
					
				}); // END transactionsControllerListener
				
				walletRelationTable.setWalletRelationsControllerListener(new walletRelationsControllerListener(){

					@Override
					public void insertRelationAsyncComplete(ArrayList<Integer> newRelations) {
						listener.insertWalletRelationsComplete(newRelations);
					}

					@Override
					public void getRelationComplete(Integer result) {
						listener.getWalletRelationsComplete(result);
					}

					@Override
					public void insertUserComplete(Integer userID) {
						listener.insertUserComplete(userID);
						
					}

					@Override
					public void getUserComplete(Integer result) {
						listener.getUserComplete(result);
					}
					
				}); // END walletRelationsControllerListener
				
			} // END walletTable insertWalletsAsyncComplete()

			@Override
			public void getWalletComplete(Integer result) {
				listener.getWalletComplete(result);
			}
			
		}); // END walletsControllerListener
	} // END checkForNewData()
	
	private String computeHash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException{
	    MessageDigest digest = MessageDigest.getInstance("SHA-256");
	    digest.reset();

	    byte[] byteData = digest.digest(input.getBytes("UTF-8"));
	    StringBuffer sb = new StringBuffer();

	    for (int i = 0; i < byteData.length; i++){
	      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
	
	public interface getDataListener{
		public void getWalletComplete(Integer result);
		public void insertWalletComplete();
		public void getRecordsComplete(Integer result);
		public void insertRecordsComplete();
		public void getWalletRelationsComplete(Integer result);
		public void insertWalletRelationsComplete(ArrayList<Integer> newRecords);
		public void getUserComplete(Integer result);
		public void insertUserComplete(Integer userID);
	}
}
