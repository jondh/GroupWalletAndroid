/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is an extension of the User class. It is intended to be
 * 				the class for the logged in user and provide additional functions.
 */

package com.whereone.groupWallet.models;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;


public class Profile extends User{
	private String password;
	private String private_token;
	private String public_token;
	private String currentDate;
	private Bitmap pic;
	private static Profile instance;
	
	private Profile(){
		
	}
	
	public static void init(){
		if(instance == null){
			instance = new Profile();
			instance.userID = 0;
			instance.password = "";
			instance.email = "";
			instance.userName = "";
			instance.firstName = "";
			instance.lastName = "";
			instance.fbID = null;
			instance.private_token = "";
			instance.public_token = "";
		}
	}
	
	public static Profile getInstance(){
		return instance;
	}
	
	public void setProfile(Integer _userID, String _userName, String _password, String _firstName, String _lastName, String _email, String _fbID){
		if(instance == null) init();
		instance.userID = _userID;
		instance.password = _password;
		instance.email = _email;
		instance.userName = _userName;
		instance.firstName = _firstName;
		instance.lastName = _lastName;
		instance.fbID = _fbID;
	}
	
	public void setProfile(Integer _userID, String _userName, String _password, String _firstName, String _lastName, String _email, String _fbID, String _private_token, String _public_token){
		if(instance == null) init();
		instance.userID = _userID;
		instance.password = _password;
		instance.email = _email;
		instance.userName = _userName;
		instance.firstName = _firstName;
		instance.lastName = _lastName;
		instance.fbID = _fbID;
		instance.private_token = _private_token;
		instance.public_token = _public_token;
	}
	
	public void setPassword(String _password){
		if(instance == null) init();
		instance.password = _password;
	}
	public void setPic(Bitmap _pic){
		if(instance == null) init();
		instance.pic = _pic;
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
	public Bitmap getPic(){
		return pic;
	}
	public String getCurrentDate(){
		if(currentDate == null) return "";
		return currentDate;
	}
	
	public String hashedPrivate(){
		SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
		currentDate = s.format(new Date());
		try {
			return computeHash(this.private_token + currentDate);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public String computeHash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException{
	    MessageDigest digest = MessageDigest.getInstance("SHA-256");
	    digest.reset();

	    byte[] byteData = digest.digest(input.getBytes("UTF-8"));
	    StringBuffer sb = new StringBuffer();

	    for (int i = 0; i < byteData.length; i++){
	      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
}
