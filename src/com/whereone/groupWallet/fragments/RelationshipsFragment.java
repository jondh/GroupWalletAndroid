package com.whereone.groupWallet.fragments;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.customAdapters.RelationshipsListAdapter;
import com.whereone.groupWallet.models.User;
import com.whereone.groupwalletcake.R;

public class RelationshipsFragment extends ListFragment{
	
	private SelfListener selfListener;
	private RelationshipListener relationshipListener;
	private Integer curWalletID = 0;
	private String curWalletName = "";
	
	public void walletRelationshipsUpdated(){
		if(selfListener != null){
			selfListener.walletRelationshipsUpdated();
		}
	}
	
	public void newWalletID(Integer walletID){
		if(selfListener != null){
			selfListener.newWalletID(walletID);
		}
	}
	
	private void setSelfListener(SelfListener listener){
		selfListener = listener;
	}
	
	private interface SelfListener{
		public void walletRelationshipsUpdated();
		public void newWalletID(Integer walletID);
	}
	
	public void setRelationshipListener(RelationshipListener listener){
		relationshipListener = listener;
	}
	
	public interface RelationshipListener{
		public void walletNameClicked();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_relationships, null);
		
		TextView walletName = (TextView) view.findViewById(R.id.relationship_walletName);
		
		walletName.setText(curWalletName);
		
		walletName.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				relationshipListener.walletNameClicked();
			}
			
		});
		
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Context context = getActivity();
		
		final SharedPreferences profile = context.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		final WalletsController walletTable = new WalletsController(context);
		final WalletRelationsController relationTable = new WalletRelationsController(context);
		final UsersController userTable = new UsersController(context);
		final TransactionsController recordTable = new TransactionsController(context, null);
		
		Log.i("RelationshipFrag", curWalletID.toString());
		if(curWalletID == 0){
			curWalletID = walletTable.getWalletIds().get(0);
			curWalletName = walletTable.getWalletNameFromId(curWalletID);
		}

		ArrayList<User> users = userTable.getUserssFromIds( relationTable.getUsersForWallet(curWalletID) );
		
		if(users != null){
			RelationshipsListAdapter radapter = new RelationshipsListAdapter(context, R.layout.relationship_row, users, recordTable, profile.getInt("id", 0), curWalletID);
			setListAdapter(radapter);
		}
		
		this.setSelfListener(new SelfListener(){

			@Override
			public void walletRelationshipsUpdated() {
				ArrayList<User> users = userTable.getUserssFromIds( relationTable.getUsersForWallet(curWalletID) );
				
				if(users != null){
					RelationshipsListAdapter radapter = new RelationshipsListAdapter(context, R.layout.wallet_row, users, recordTable, profile.getInt("id", 0), curWalletID);
					setListAdapter(radapter);
				}
			}

			@Override
			public void newWalletID(Integer walletID) {
				System.out.println(walletID);
				curWalletID = walletID;
				curWalletName = walletTable.getWalletNameFromId(walletID);
			}
			
		});
		
	}
}
