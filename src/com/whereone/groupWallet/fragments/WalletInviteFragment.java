package com.whereone.groupWallet.fragments;

import java.util.ArrayList;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.whereone.groupWallet.AcceptDeclineFriend;
import com.whereone.groupWallet.AcceptDeclineWallet;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.FriendsController;
import com.whereone.groupWallet.controllers.FriendsController.UpdateFriendsListener;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletRelationsController.UpdateListener;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.customAdapters.WalletInviteListAdapter;
import com.whereone.groupWallet.customAdapters.WalletInviteListAdapter.WalletInviteListButtonListener;
import com.whereone.groupWallet.customAdapters.WalletInviteListAdapter.WalletInviteResourceListener;
import com.whereone.groupWallet.models.Friend;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupWallet.models.WalletFriend;

public class WalletInviteFragment extends ListFragment{
	
	private SelfListener selfListener;
	private WalletInviteFragmentListener listener;
	private UsersController usersController;
	private WalletsController walletsController;
	private WalletRelationsController walletRelationsController;
	private FriendsController friendsController;
	private Profile profile;
	private DBhttpRequest httpRequest;
	private ArrayList<WalletFriend> invites;
	private ProgressDialog mpDialog;
	
	public void walletInvitesUpdated(){
		if(selfListener != null){
			selfListener.walletInvitesUpdated();
		}
	}
	
	private void setSelfListener(SelfListener listener){
		selfListener = listener;
	}
	
	private interface SelfListener{
		public void walletInvitesUpdated();
	}
	
	public void setWalletInviteFragmentListener(WalletInviteFragmentListener listener){
		this.listener = listener;
	}
	
	public interface WalletInviteFragmentListener{
		public void walletClicked(Wallet wallet, Friend friend);
		public void accpeted(Wallet wallet, Friend friend);
		public void failure(Integer result);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_wallet_invite, null);
		
		
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Context context = getActivity();
		profile = Profile.getInstance();
		walletsController = WalletsController.getInstance();
		walletRelationsController = WalletRelationsController.getInstance();
		usersController = UsersController.getInstance();
		friendsController = FriendsController.getInstance();
		httpRequest = DBhttpRequest.getInstance();
		
		invites = new ArrayList<WalletFriend>();
		
		mpDialog = new ProgressDialog(context);
		mpDialog.setMessage("loading");
		
		setList(context);
		
		this.setSelfListener(new SelfListener(){

			@Override
			public void walletInvitesUpdated() {
				setList(context);
			}
			
		});
		
	}
	
	private void setList(final Context context){
		ArrayList<Wallet> wallets = walletsController.getWalletsUserID(profile.getUserID(), walletRelationsController, 0);
		ArrayList<Friend> friends = friendsController.getFriendRequests(profile.getUserID());
		for(int i = 0; i < wallets.size(); i++){
			invites.add( new WalletFriend(wallets.get(i), null) );
		}
		for(int i = 0; i < friends.size(); i++){
			invites.add( new WalletFriend(null, friends.get(i)) );
		}
		if(wallets != null){
			final WalletInviteListAdapter wadapter = new WalletInviteListAdapter(context, R.layout.wallet_invite_row, invites, usersController);
			
			wadapter.setWalletInviteResourceListener(new WalletInviteResourceListener(){

				@Override
				public void gotResources() {
					getActivity().runOnUiThread(new Runnable(){

						@Override
						public void run() {
							wadapter.setWalletInviteListButtonListener(new WalletInviteListButtonListener(){

								@Override
								public void acceptClicked(final Wallet wallet, final Friend friend) {
									mpDialog.show();
									
									if(wallet != null){
									
										walletRelationsController.setUpdateListener(new UpdateListener(){
	
											@Override
											public void acceptDeclineComplete(Integer result) {
												mpDialog.hide();
												if(result == 1){
													listener.accpeted(wallet, friend);
												}
												else{
													listener.failure(result);
												}
											}
											
										});
										
										walletRelationsController.acceptDeclineWallet(httpRequest, profile, wallet.getID(), AcceptDeclineWallet.Type.ACCEPT);
									}
									else if(friend != null){	
										friendsController.setUpdateListener(new UpdateFriendsListener(){

											@Override
											public void acceptDeclineComplete(Integer result) {
												mpDialog.hide();
												if(result == 1){
													listener.accpeted(wallet, friend);
												}
												else{
													listener.failure(result);
												}
											}
											
										});
										
										friendsController.acceptDeclineFriend(httpRequest, profile, friend, AcceptDeclineFriend.Type.ACCEPT);
									}
								}
								@Override
								public void declineClicked(Wallet wallet, Friend friend) {
									mpDialog.show();
									
									if(wallet != null){
										walletRelationsController.setUpdateListener(new UpdateListener(){
	
											@Override
											public void acceptDeclineComplete(Integer result) {
												mpDialog.hide();
												if(result == 1){
													setList(context);
												}
												else{
													listener.failure(result);
												}
											}
											
										});
										walletRelationsController.acceptDeclineWallet(httpRequest, profile, wallet.getID(), AcceptDeclineWallet.Type.DECLINE);
									}
									else if(friend != null){
										friendsController.setUpdateListener(new UpdateFriendsListener(){

											@Override
											public void acceptDeclineComplete(Integer result) {
												mpDialog.hide();
												if(result == 1){
													setList(context);
												}
												else{
													listener.failure(result);
												}
											}
											
										});
										
										friendsController.acceptDeclineFriend(httpRequest, profile, friend, AcceptDeclineFriend.Type.DECLINE);
									}
								}
								
							});
							
							setListAdapter(wadapter);
						}
						
					});
				}
				
			});
			wadapter.loadResources(httpRequest, profile);
		}
	}
	
	@Override  
	public void onListItemClick(ListView l, View v, int position, long id) { 
		WalletFriend walletFriend = invites.get(position);
		listener.walletClicked( walletFriend.wallet, walletFriend.friend );
	}  
	
	
}
