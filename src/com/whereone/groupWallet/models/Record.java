/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class abstracts a "record". The record is a record of a
 * 				transaction between the logged in user and some other user.
 */

package com.whereone.groupWallet.models;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class Record {
	private Integer id;
	private Integer owe_id;
	private Integer owed_id;
	private Double amount;
	private Integer wallet_id;
	private String comments;
	private String dateTime;
	private Date date;
	
	public Record(){
		
	}
	
	public Record(Integer _id, Integer oweUID, Integer owedUID, Double _amount, Integer wallet, String _comments, String _dateTime){
		id = _id;
		owe_id = oweUID;
		owed_id = owedUID;
		amount = _amount;
		wallet_id = wallet;
		comments = _comments;
		dateTime = _dateTime;
	}
	
	public void setID(Integer _id){
		id = _id;
	}
	public void setOweId(Integer oweUID){
		owe_id = oweUID;
	}
	public void setOwedId(Integer owedUID){
		owed_id = owedUID;
	}
	public void setAmount(Double _amount){
		amount = _amount;
	}
	public void setWalletId(Integer wallet){
		wallet_id = wallet;
	}
	public void setComments(String _comments){
		comments = _comments;
	}
	public void setDateTime(String _dateTime){
		dateTime = _dateTime;
	}
	
	public Integer getID(){
		return id;
	}
	public Integer getOweId(){
		return owe_id;
	}
	public Integer getOwedId(){
		return owed_id;
	}
	public Double getAmount(){
		return amount;
	}
	public Integer getWalletId(){
		return wallet_id;
	}
	public String getComments(){
		return comments;
	}
	public String getDateTime(){
		return dateTime;
	}
	public Date getDate(){
		return date;
	}
	
	public void findDate(){
		this.date = dateFromString(this.dateTime);
	}
	
	private Date dateFromString(String _date){
		SimpleDateFormat iso8601Format = new SimpleDateFormat(
	            "yyyy-MM-dd HH:mm:ss", Locale.US);

	    try {
			return iso8601Format.parse(_date);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	
	public static class Comparitors{
		public static Comparator<Record> DATE = new Comparator<Record>() {
			@Override
			public int compare(Record arg0, Record arg1) {
				return (arg0.getDate().getTime() > arg1.getDate().getTime() ? -1 : 1); 
			}
        };
	}
}
