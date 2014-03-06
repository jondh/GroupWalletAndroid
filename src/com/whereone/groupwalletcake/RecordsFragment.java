package com.whereone.groupwalletcake;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Record;

public class RecordsFragment extends ListFragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.records, null);
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final DBhttpRequest httpRequest = new DBhttpRequest();
		final Context context = getActivity();
		
		SharedPreferences profile = context.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		ArrayList<Record> records = new ArrayList<Record>();
		records.add(new Record("fname lname", 5.43, "comments"));
		
		RecordListAdapter radapter = new RecordListAdapter(context, R.layout.record_row, records);
		setListAdapter(radapter);
	}
}
