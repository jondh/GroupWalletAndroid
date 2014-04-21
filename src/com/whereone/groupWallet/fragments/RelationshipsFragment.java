package com.whereone.groupWallet.fragments;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.customAdapters.RelationshipsListAdapter;
import com.whereone.groupWallet.customAdapters.RelationshipsListAdapter.RelationshipListAdapterListener;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.User;
import com.whereone.groupWallet.models.Wallet;

public class RelationshipsFragment extends ListFragment{
	
	private SelfListener selfListener;
	private RelationshipListener relationshipListener;
	private WalletsController walletsController;
	private WalletRelationsController walletRelationsController;
	private Wallet wallet;
	private Profile profile;
	private RelativeLayout loading;
	private Boolean loadingFlag;
	
	public void walletRelationshipsUpdated(){
		if(selfListener != null){
			selfListener.walletRelationshipsUpdated();
		}
	}
	
	private void setSelfListener(SelfListener listener){
		selfListener = listener;
	}
	
	private interface SelfListener{
		public void walletRelationshipsUpdated();
	}
	
	public void setRelationshipListener(RelationshipListener listener){
		relationshipListener = listener;
	}
	
	public interface RelationshipListener{
		public void walletNameClicked();
		public void relationClicked(User user, Wallet wallet);
		public void addUserClicked(Wallet wallet);
		public void load(Wallet wallet);
	}
	
	public void setWallet(Wallet wallet, Integer walletID){
		if(wallet == null && walletID != null){
			this.wallet = walletsController.getWalletFromId(walletID);
		}
		else{
			this.wallet = wallet;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_relationships, null);
		
		loading = (RelativeLayout) view.findViewById(R.id.relationship_loading);
		TextView walletName = (TextView) view.findViewById(R.id.relationship_walletName);
		Button addUser = (Button) view.findViewById(R.id.relationships_add);
		
		if( loadingFlag ){
			loading.setVisibility(View.VISIBLE);
		}
		else{
			loading.setVisibility(View.GONE);
		}
		
		if(wallet != null){
			walletName.setText( wallet.getName() );
		}
		else{
			walletName.setText( "No Wallets" );
		}
		
		walletName.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				relationshipListener.walletNameClicked();
			}
			
		});
		
		addUser.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				relationshipListener.addUserClicked(wallet);
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
		loadingFlag = false;
		
		if(wallet == null){
			ArrayList<Wallet> tempWallets = walletsController.getWalletsUserID(profile.getUserID(), walletRelationsController, 1);
			if(tempWallets.size() > 0){
				wallet = tempWallets.get(0);
			}
		}
		
		if(wallet != null){
			relationshipListener.load(wallet);
			loadingFlag = true;
		}
		
		setList(context);
		
		this.setSelfListener(new SelfListener(){

			@Override
			public void walletRelationshipsUpdated() {
				loadingFlag = false;
				getActivity().runOnUiThread(new Runnable(){

					@Override
					public void run() {
						if( loading != null){
							loading.setVisibility(View.GONE);
						}
					}
					
				});
				
				setList(context);
			}
			
		});
		
	}
	
	private void setList(final Context context){
		if(wallet != null){

			final ArrayList<User> users = UsersController.getInstance().getUserssFromIds( WalletRelationsController.getInstance().getUsersForWallet( wallet.getID(), profile.getUserID(), 1 ) );
			
				if(users != null){
					
					getActivity().runOnUiThread(new Runnable(){

						@Override
						public void run() {
							RelationshipsListAdapter radapter = new RelationshipsListAdapter(context, R.layout.relationship_row, users, TransactionsController.getInstance(), profile.getUserID(), wallet.getID());

							setListAdapter(radapter);
							
							radapter.setRelationshipListAdapterListener(new RelationshipListAdapterListener(){
				
								@Override
								public void rowClicked(User user) {
									relationshipListener.relationClicked(user, wallet);
								}
								
							});
						}
						
					});
					
				}
			}
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
