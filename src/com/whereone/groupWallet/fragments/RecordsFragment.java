package com.whereone.groupWallet.fragments;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.customAdapters.RecordListAdapter;
import com.whereone.groupWallet.models.Record;
import com.whereone.groupwalletcake.LogOutCurrent;
import com.whereone.groupwalletcake.R;

public class RecordsFragment extends ListFragment{
	
	private SelfListener selfListener;
	
	public void transactionsUpdated(){
		if(selfListener != null){
			selfListener.transactionsUpdated();
		}
	}
	
	private void setSelfListener(SelfListener listener){
		selfListener = listener;
	}
	
	private interface SelfListener{
		public void transactionsUpdated();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_records, null);
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Context context = getActivity();
		
		final SharedPreferences profile = context.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		final LogOutCurrent logOut = new LogOutCurrent(profile, null, null, null, null);
		
		final TransactionsController transactionTable = new TransactionsController(context, logOut);
		final UsersController userTable = new UsersController(context);
		
		ArrayList<Record> records = transactionTable.getRecords();
		for(int i = 0; i < records.size(); i++){
			records.get(i).findDate();
		}
		Collections.sort(records, Record.Comparitors.DATE);
		if(records != null){
			RecordListAdapter radapter = new RecordListAdapter(context, R.layout.record_row, userTable, records);
			setListAdapter(radapter);
		}
		
		this.setSelfListener(new SelfListener(){

			@Override
			public void transactionsUpdated() {
				ArrayList<Record> records = transactionTable.getRecords();
				for(int i = 0; i < records.size(); i++){
					records.get(i).findDate();
				}
				Collections.sort(records, Record.Comparitors.DATE);
				if(records != null){
					RecordListAdapter radapter = new RecordListAdapter(context, R.layout.record_row, userTable, records);
					setListAdapter(radapter);
				}
			}
			
		});
		
	}
	
	public String computeHash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException{
	    MessageDigest digest = MessageDigest.getInstance("SHA-256");
	    digest.reset();

	    byte[] byteData = digest.digest(input.getBytes("UTF-8"));
	    StringBuffer sb = new StringBuffer();

	    for (int i = 0; i < byteData.length; i++){
	      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
}
