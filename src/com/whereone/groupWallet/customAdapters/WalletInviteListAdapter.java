/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is a list view adapter for a list of records
 */

package com.whereone.groupWallet.customAdapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.UsersController.UsersGetListener;
import com.whereone.groupWallet.models.Friend;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupWallet.models.WalletFriend;

public class WalletInviteListAdapter extends ArrayAdapter<WalletFriend> {

	private List<WalletFriend> invites;
	private UsersController usersController;
	private WalletInviteListButtonListener buttonListener;
	private WalletInviteResourceListener resourceListener;
	
	public void setWalletInviteListButtonListener(WalletInviteListButtonListener listener){
		this.buttonListener = listener;
	}
	
	public interface WalletInviteListButtonListener{
		public void acceptClicked(Wallet wallet, Friend friend);
		public void declineClicked(Wallet wallet, Friend friend);
	}
	
	public void setWalletInviteResourceListener(WalletInviteResourceListener listener){
		this.resourceListener = listener;
	}
	
	public interface WalletInviteResourceListener{
		public void gotResources();
	}
	
	public WalletInviteListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    
	}

	public WalletInviteListAdapter(Context context, int resource, List<WalletFriend> invites, UsersController usersController) {
		super(context, resource, invites);

	    this.invites = invites;
	    this.usersController = usersController;
	}
	
	public void loadResources(DBhttpRequest httpRequest, Profile profile){
		final ArrayList<Integer> usersGot = new ArrayList<Integer>();
		Integer temp = 0;
		for(int i = 0; i < invites.size(); i++){
			if( invites.get(i).wallet != null ){
				if ( !usersController.containsId(invites.get(i).wallet.getUserID() ) ){
					temp++;
				}
			}
			else{
				if ( !usersController.containsId(invites.get(i).friend.getUser1() ) ){
					temp++;
				}
			}
		}
		final Integer numFindUsers = temp;
		if(numFindUsers == 0){
			if( resourceListener != null ){
				resourceListener.gotResources();
			}
		}
		else{
			usersController.setUsersGetListener(new UsersGetListener(){
	
				@Override
				public void getUserComplete(Integer result) {
					usersGot.add(result);
					if(usersGot.size() == numFindUsers){
						if( resourceListener != null ){
							resourceListener.gotResources();
						}
					}
				}
				
			});
			for(int i = 0; i < invites.size(); i++){
				if(invites.get(i).wallet != null){
					if ( !usersController.containsId(invites.get(i).wallet.getUserID() ) ){
						usersController.getUserAndInsert( httpRequest, profile, invites.get(i).wallet.getUserID() );
					}
				}
				else{
					if ( !usersController.containsId(invites.get(i).friend.getUser1() ) ){
						usersController.getUserAndInsert( httpRequest, profile, invites.get(i).friend.getUser1() );
					}
				}
			}
		}
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    View v = convertView;

	    if (v == null) {

	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.wallet_invite_row, null);

	    }

	    final WalletFriend walletFriend = invites.get(position);
	    final Wallet wallet = walletFriend.wallet;
	    final Friend friend = walletFriend.friend;
	    
	    AutoResizeTextView walletName = (AutoResizeTextView) v.findViewById(R.id.wallet_invite_walletName); 
    	AutoResizeTextView byName = (AutoResizeTextView) v.findViewById(R.id.wallet_invite_byName);
        Button accept = (Button) v.findViewById(R.id.wallet_invite_accept);
        Button decline = (Button) v.findViewById(R.id.wallet_invite_decline);

	    if (wallet != null) {

	        if (walletName != null) {
	        	String wname = wallet.getName();
	        	walletName.setText(wname.trim());
	        }
	       
	        if (byName != null) {
	        	String bName = usersController.getUserNameFromId( wallet.getUserID() );
	        	byName.setText("By: @" + bName);
	        }
	        
	    }
	    else if (friend != null){
	    	
	    	if (walletName != null) {
	        	walletName.setText( "Friend Request" );
	        }
	       
	        if (byName != null) {
	        	String bName = usersController.getUserNameFromId( friend.getUser1() );
	        	byName.setText("From: @" + bName);
	        }
	    	
	    }
	    
	    accept.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(buttonListener != null){
					buttonListener.acceptClicked(wallet, friend);
				}
			}
		});
        
        decline.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(buttonListener != null){
					buttonListener.declineClicked(wallet, friend);
				}
			}
		});

	    return v;

	}
}
