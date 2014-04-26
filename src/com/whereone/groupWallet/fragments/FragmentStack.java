package com.whereone.groupWallet.fragments;

import java.util.Stack;

import com.whereone.groupWallet.models.Record;
import com.whereone.groupWallet.models.User;
import com.whereone.groupWallet.models.Wallet;

public class FragmentStack {
	
	private Stack<FragmentType> fragmentStack;
	private FragmentType holdFrag;
	private Integer MAX_IN_STACK = 20;
	
	public FragmentStack(){
		fragmentStack = new Stack<FragmentType>();
	}
	
	public enum FRAGMENT{
		Profile,
		QuickPay,
		Transaction,
		Wallet,
		Relation,
		DetailedTransaction,
		Add
	}
	
	public void add(FRAGMENT type, User user, Wallet wallet, Integer walletID, Record record, AddFragment.Type addType){
		if(holdFrag == null){
			holdFrag = new FragmentType(type, user, wallet, walletID, record, addType);
		}
		else{
			fragmentStack.push(holdFrag);
			holdFrag = new FragmentType(type, user, wallet, walletID, record, addType);
			if(fragmentStack.size() > MAX_IN_STACK){
				fragmentStack.remove(fragmentStack.size() - 1);
			}
		}
	}
	
	public FragmentType remove(){
		if(fragmentStack.size() > 0){
			return fragmentStack.pop();
		}
		else return null;
	}
	
	
	public class FragmentType{
		public FRAGMENT type;
		public User user;
		public Wallet wallet;
		public Integer walletID;
		public Record record;
		public AddFragment.Type addType;
		
		FragmentType(FRAGMENT type, User user, Wallet wallet, Integer walletID, Record record, AddFragment.Type addType){
			this.type = type;
			this.user = user;
			this.wallet = wallet;
			this.walletID = walletID;
			this.record = record;
			this.addType = addType;
		}
	}
}
