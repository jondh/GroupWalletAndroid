/**
 * Author: Jonathan Harrison
 * Date: 4/23/13
 * Description: 
 */

package com.whereone.groupWallet.models;


public class Friend {
	private Integer id;
	private Integer user1;
	private Integer user2;
	private Boolean accept;
	
	public Friend(){
		
	}
	
	public Friend(Integer _id, Integer user1, Integer user2, Boolean accept){
		this.id = _id;
		this.user1 = user1;
		this.user2 = user2;
		this.accept = accept;
	}
	
	public void setID(Integer _id){
		this.id = _id;
	}
	public void setUser1(Integer userID){
		this.user1 = userID;
	}
	public void setUser2(Integer userID){
		this.user2 = userID;
	}
	public void setAccept(Boolean accept){
		this.accept = accept;
	}
	
	public Integer getID(){
		return id;
	}
	public Integer getUser1(){
		return user1;
	}
	public Integer getUser2(){
		return user2;
	}
	public Boolean getAccept(){
		return accept;
	}
}
