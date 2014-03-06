/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is used to represent a user. It contains all the necessary information
 * 				about a user.
 */

package com.whereone.groupWallet.models;

import android.graphics.Bitmap;

public class User {
	protected Integer userID;
	protected Integer fbID;
	protected String email;
	protected String userName;
	protected String firstName;
	protected String lastName;
	private String picURL;
	private Bitmap picture;
	private Double walletAmount;
	private Double totalAmount;
	private Boolean walletAmountRefresh;
	private Boolean totalAmountRefresh;
	
	public User(){
		userID = 0;
		fbID = 0;
		email = "";
		userName = "";
		firstName = "";
		lastName = "";
		picURL = "";
		picture = null;
		walletAmount = 0.0;
		totalAmount = 0.0;
		walletAmountRefresh = true;
		totalAmountRefresh = true;
	}
	
	public User(Integer _userID, String _userName, String _firstName, String _lastName, String _email, Integer _fbID){
		userID = _userID;
		fbID = _fbID;
		email = _email;
		userName = _userName;
		firstName = _firstName;
		lastName = _lastName;
		walletAmount = 0.0;
		totalAmount = 0.0;
		picURL = "";
		picture = null;
		walletAmountRefresh = true;
		totalAmountRefresh = true;
	}
	
	public User(Integer _userID, String _userName, String _firstName, String _lastName, String _email, Integer _fbID, Double _walletAmount, Double _totalAmount){
		userID = _userID;
		fbID = _fbID;
		email = _email;
		userName = _userName;
		firstName = _firstName;
		lastName = _lastName;
		walletAmount = _walletAmount;
		totalAmount = _totalAmount;
		picURL = "";
		picture = null;
		walletAmountRefresh = true;
		totalAmountRefresh = true;
	}
	
	public void setUserID(Integer _userID){
		userID = _userID;
	}
	public void setFB(Integer _fbID){
		fbID = _fbID;
	}
	public void setUserName(String _userName){
		userName = _userName;
	}
	public void setFirstName(String _firstName){
		firstName = _firstName;
	}
	public void setLastName(String _lastName){
		lastName = _lastName;
	}
	public void setEmail(String _email){
		email = _email;
	}
	public void setPicURL(String _picURL){
		picURL = _picURL;
	}
	public void setPicture(Bitmap _picture){
		picture = _picture;
	}
	public void setWalletAmount(Double _walletAmount){
		walletAmount = _walletAmount;
	}
	public void setTotalAmount(Double _totalAmount){
		totalAmount = _totalAmount;
	}
	public void setWalletAmountRefresh(Boolean _walletAmountRefresh){
		walletAmountRefresh = _walletAmountRefresh;
	}
	public void setTotalAmountRefresh(Boolean _totalAmountRefresh){
		totalAmountRefresh = _totalAmountRefresh;
	}
	
	public Integer getUserID(){
		return userID;
	}
	public Integer getFbID(){
		return fbID;
	}
	public String getUserName(){
		return userName;
	}
	public String getFirstName(){
		return firstName;
	}
	public String getLastName(){
		return lastName;
	}
	public String getName(){
		return firstName + " " + lastName;
	}
	public String getEmail(){
		return email;
	}
	public String getPicURL(){
		return picURL;
	}
	public Double getWalletAmount(){
		return walletAmount;
	}
	public Double getTotalAmount(){
		return totalAmount;
	}
	public Bitmap getPicture(){
		return picture;
	}
	public Boolean isWalletAmountRefresh(){
		return walletAmountRefresh;
	}
	public Boolean isTotalAmountRefresh(){
		return totalAmountRefresh;
	}
	
	public void findPicURL(){
		if(fbID > 0){
			picURL = "http://graph.facebook.com/"+fbID+"/picture?type=normal";
		}
		else{
			picURL = "http://jondh.com/GroupWallet/userPhotos/user"+userID+".jpg";
		}
	}
}
