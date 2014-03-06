package com.whereone.groupWallet.controllers;

import android.app.ProgressDialog;
import android.content.Context;

import com.whereone.groupwalletcake.InsertRecord;
import com.whereone.groupwalletcake.InsertRecord.insertRecordListener;
import com.whereone.groupwalletcake.LogOutCurrent;
import com.whereone.groupwalletcake.R;

public class TransactionsController {
	private LogOutCurrent logOut;
	private Context context;
	private insertCompleteListener listener;
	
	public TransactionsController(Context _context, LogOutCurrent _logOut){
		logOut = _logOut;
		context = _context;
	}
	
	public void setInsertCompleteListener(insertCompleteListener _listener) {
        this.listener = _listener;
    }
	
	public void removeAll(){
		
	}
	
	public void insert(Integer _userID, Integer _otherUID, Double _amount, Integer _walletID, String _Comments, Boolean _owe, final DBhttpRequest _httpRequest, final ProgressDialog mPDialog,
			final String public_token, final String private_tokenH, final String _timeStamp){
		
		InsertRecord insertRecord = new InsertRecord(_userID, _otherUID, _amount, _walletID, _Comments,
				_owe, _httpRequest, public_token, private_tokenH, _timeStamp);
		
		mPDialog.setMessage("Loading...");
	    mPDialog.show(); 
		
	   	insertRecord.setInsertRecordListener(new insertRecordListener(){

			@Override
			public void insertRecordPreExecute() {
				
			}

			@Override
			public void insertRecordComplete(Boolean result) {
				if(result){
					System.out.println("SUCCESSFULLY inserted record");
					mPDialog.hide(); 
					listener.insertComplete();
				}
				else{
					System.out.println("Insert record FAILED");
					mPDialog.hide(); 
					logOut.logOut(context, _httpRequest, context.getString(R.string.logOutURL), public_token, private_tokenH, _timeStamp);
				}
			}

			@Override
			public void insertRecordCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   insertRecord.execute(context.getString(R.string.insertRecordURL));
	}
	
	public interface insertCompleteListener{
		public void insertComplete();
	}
}
