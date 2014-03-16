package com.whereone.groupWallet.fragments;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.customAdapters.WalletListAdapter;
import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupwalletcake.R;

public class WalletsFragment extends ListFragment{
	
	private SelfListener selfListener;
	private WalletFragmentListener walletFragmentListener;
	private ArrayList<Wallet> wallets;
	
	public void walletsUpdated(){
		if(selfListener != null){
			selfListener.walletsUpdated();
		}
	}
	
	private void setSelfListener(SelfListener listener){
		selfListener = listener;
	}
	
	private interface SelfListener{
		public void walletsUpdated();
	}
	
	public void setWalletFragmentListener(WalletFragmentListener listener){
		walletFragmentListener = listener;
	}
	
	public interface WalletFragmentListener{
		public void walletClicked(Integer walletID);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_wallets, null);
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Context context = getActivity();
		
		final SharedPreferences profile = context.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		final WalletsController walletTable = new WalletsController(context);
		final TransactionsController recordTable = new TransactionsController(context, null);
		
		wallets = walletTable.getWallets();
		if(wallets != null){
			WalletListAdapter wadapter = new WalletListAdapter(context, R.layout.wallet_row, wallets, recordTable, profile.getInt("id", 0));
			setListAdapter(wadapter);
		}
		
		this.setSelfListener(new SelfListener(){

			@Override
			public void walletsUpdated() {
				wallets = walletTable.getWallets();
				if(wallets != null){
					WalletListAdapter wadapter = new WalletListAdapter(context, R.layout.wallet_row, wallets, recordTable, profile.getInt("id", 0));
					setListAdapter(wadapter);
				}
			}
			
		});
		
	}
	
	@Override  
	public void onListItemClick(ListView l, View v, int position, long id) { 
		walletFragmentListener.walletClicked( wallets.get(position).getID() );
	}  
	
	
}
