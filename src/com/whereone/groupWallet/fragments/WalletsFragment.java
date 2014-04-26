package com.whereone.groupWallet.fragments;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.customAdapters.WalletListAdapter;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.Wallet;

public class WalletsFragment extends ListFragment{
	
	private SelfListener selfListener;
	private WalletFragmentListener listener;
	private WalletsController walletsController;
	private WalletRelationsController walletRelationsController;
	private Profile profile;
	private ArrayList<Wallet> wallets;
	private Boolean loadingFlag;
	private RelativeLayout loading;
	
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
		this.listener = listener;
	}
	
	public interface WalletFragmentListener{
		public void walletClicked(Wallet wallet);
		public void addWallet();
		public void load();
	}
	
	public void update(){
		loadingFlag = true;
		loading.setVisibility(View.VISIBLE);
		listener.load();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_wallets, null);
		
		loading = (RelativeLayout) view.findViewById(R.id.wallets_loading);
		Button addWallet = (Button) view.findViewById(R.id.wallets_new);
		
		if(loadingFlag){
			loading.setVisibility(View.VISIBLE);
		}
		else{
			loading.setVisibility(View.GONE);
		}
		
		addWallet.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.addWallet();
			}
		});
		
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Context context = getActivity();
		profile = Profile.getInstance();
		walletsController = WalletsController.getInstance();
		walletRelationsController = WalletRelationsController.getInstance();
		
		loadingFlag = true;
		
		listener.load();
		
		setList(context);

		this.setSelfListener(new SelfListener(){

			@Override
			public void walletsUpdated() {
				
				getActivity().runOnUiThread(new Runnable(){

					@Override
					public void run() {
						if(loading != null && loadingFlag){
							loadingFlag = false;
							loading.setVisibility(View.GONE);
						}
					}
					
				});
				
				setList(context);
			}
			
		});
	}
	
	public void setList(Context context){
		wallets = walletsController.getWalletsUserID(profile.getUserID(), walletRelationsController, 1);
		if(wallets != null){
			WalletListAdapter wadapter = new WalletListAdapter(context, R.layout.wallet_row, wallets, profile.getUserID());
			setListAdapter(wadapter);
		}
	}
	
	@Override  
	public void onListItemClick(ListView l, View v, int position, long id) { 
		listener.walletClicked( wallets.get(position) );
	}  
	
	@Override
	public void onPause(){
		super.onPause();
		this.setSelfListener(null);
		loadingFlag = false;
		if( loading != null ){
			loading.setVisibility(View.GONE);
		}
	}
}
