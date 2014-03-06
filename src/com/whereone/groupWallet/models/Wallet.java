/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is used to represent a wallet.
 */

package com.whereone.groupWallet.models;

public class Wallet {
	private Integer id;
	private String name;
	private String date;
	
	public Wallet(Integer _id, String _name, String _date){
		id = _id;
		name = _name;
		date = _date;
	}
	
	public Integer getID(){
		return id;
	}
	public String getName(){
		return name;
	}
	public String getDate(){
		return date;
	}
}
