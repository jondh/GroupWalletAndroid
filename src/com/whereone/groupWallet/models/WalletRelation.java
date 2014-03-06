package com.whereone.groupWallet.models;

public class WalletRelation {
	private Integer id;
	private Integer wallet_id;
	private Integer user_id;
	
	public WalletRelation(Integer _id, Integer _wallet_id, Integer _user_id){
		id = _id;
		wallet_id = _wallet_id;
		user_id = _user_id;
	}
	
	public Integer getID(){
		return id;
	}
	public Integer getWalletID(){
		return wallet_id;
	}
	public Integer getUserID(){
		return user_id;
	}
}
