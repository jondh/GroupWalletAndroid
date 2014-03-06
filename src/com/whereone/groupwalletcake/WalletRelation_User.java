package com.whereone.groupwalletcake;

import com.whereone.groupWallet.models.User;
import com.whereone.groupWallet.models.WalletRelation;

public class WalletRelation_User {
	private WalletRelation walletRelation;
	private User user;
	
	WalletRelation_User(WalletRelation _wr, User _user){
		walletRelation = _wr;
		user = _user;
	}
	
	public WalletRelation getWalletRelation(){
		return walletRelation;
	}
	public User getUser(){
		return user;
	}
}
