package com.whereone.groupWallet.models;

public class WalletRelation {
	private Integer id;
	private Integer wallet_id;
	private Integer user_id;
	private Boolean accept;
	
	public WalletRelation(Integer id, Integer _wallet_id, Integer _user_id, Boolean accept){
		this.id = id;
		wallet_id = _wallet_id;
		user_id = _user_id;
		this.accept = accept;
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
	public Boolean getAccept(){
		return accept;
	}
}
