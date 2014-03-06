/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is an extension of the User class. It is intended to be
 * 				the class for the logged in user and provide additional functions.
 */

package com.whereone.groupWallet.models;


public class Profile extends User{
	private String password;
	private String private_token;
	private String public_token;
	
	public Profile(Integer _userID, String _userName, String _password, String _firstName, String _lastName, String _email, Integer _fbID){
		userID = _userID;
		password = _password;
		email = _email;
		userName = _userName;
		firstName = _firstName;
		lastName = _lastName;
		fbID = _fbID;
	}
	
	public Profile(Integer _userID, String _userName, String _password, String _firstName, String _lastName, String _email, Integer _fbID, String _private_token, String _public_token){
		userID = _userID;
		password = _password;
		email = _email;
		userName = _userName;
		firstName = _firstName;
		lastName = _lastName;
		fbID = _fbID;
		private_token = _private_token;
		public_token = _public_token;
	}
	
	public void setPassword(String _password){
		password = _password;
	}
		
	public String getPassword(){
		return password;
	}
	
	public String getPrivateToken(){
		return private_token;
	}
	public String getPublicToken(){
		return public_token;
	}
}
