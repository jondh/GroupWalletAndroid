package com.whereone.groupWallet.controllers;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.whereone.groupWallet.models.WalletRelation;
import com.whereone.groupwalletcake.GetWalletRelations;
import com.whereone.groupwalletcake.LogOutCurrent;
import com.whereone.groupwalletcake.R;
import com.whereone.groupwalletcake.GetWalletRelations.getWalletRelationsListener;

public class WalletRelationsController extends SQLiteOpenHelper {
	public static final String TABLE_WALLET_RELATIONS = "walletRelations";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_WALLET_ID = "wallet_id";
	public static final String COLUMN_USER_ID = "user_id";
	
	private SQLiteDatabase databaseW;
	private SQLiteDatabase databaseR;
	private LogOutCurrent logOut;
	private Context context;
	

	private static final String DATABASE_NAME = "walletRelations.db";
	private static final int DATABASE_VERSION = 1;
	

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
	      + TABLE_WALLET_RELATIONS + "(" + COLUMN_ID
	      + " integer primary key, " + COLUMN_WALLET_ID
	      + " text not null," + COLUMN_USER_ID
	      + " test not null);";

	public WalletRelationsController(Context _context, LogOutCurrent _logOut) {
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);
		
		databaseW = this.getWritableDatabase();
		databaseR = this.getReadableDatabase();
		logOut = _logOut;
		context = _context;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(WalletsController.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLET_RELATIONS);
		    onCreate(database);
	}
	
	public void removeAll(){
		databaseW.delete(TABLE_WALLET_RELATIONS, null, null);
	}
	
	public void insertWalletRelations(ArrayList<WalletRelation> walletRelations){
		for(Integer i = 0; i < walletRelations.size(); i++){
			WalletRelation curWalletRelation = walletRelations.get(i);
			ContentValues values = new ContentValues();
			values.put("id", curWalletRelation.getID());
			values.put("wallet_id", curWalletRelation.getWalletID());
			values.put("user_id", curWalletRelation.getUserID());
			try{
				databaseW.insertOrThrow(TABLE_WALLET_RELATIONS, null, values);
			}
			catch (SQLiteConstraintException e){
				Log.i("Wallet Constraint Exception", curWalletRelation.getID() + " duplicate");
			}
			catch (NullPointerException e){
				Log.i("WalletRelation Null Ptr", "NullPointerException");
			}
		}
	}
	
	public ArrayList<Integer> getUsersForWallet(Integer walletId){
		String[] columns = {
				COLUMN_USER_ID
		};
		
		String whereClause = "wallet_id = ?";
		String[] whereArgs = new String[]{
				walletId.toString()
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLET_RELATIONS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
            ArrayList<Integer> user_ids = new ArrayList<Integer>();
            
            while (cursor.moveToNext())
            {
                 user_ids.add( cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID)) );
             }
            return user_ids;
        }
        else
        {
            return null;
        }
	}
	
	public Boolean containsUser(Integer walletId, Integer userId){
		String[] columns = {
				COLUMN_ID
		};
		
		String whereClause = "wallet_id = ? AND user_id = ?";
		String[] whereArgs = new String[]{
				walletId.toString(),
				userId.toString()
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLET_RELATIONS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
            return true;
        }
        else
        {
            return false;
        }
	}
	
	public void getWalletRelationsAndUsersAndInsert(ArrayList<Integer> wallet_ids, Integer userID, String getWalletRelationsURL,
			final UsersController userTable, final String getUserURL, final DBhttpRequest httpRequest,
			final String public_token, final String private_tokenH, final String _timeStamp){
		if(wallet_ids == null) return;
		for(int i = 0; i < wallet_ids.size(); i++){
	   		GetWalletRelations getWalletRelations = new GetWalletRelations(httpRequest, userID, wallet_ids.get(i), public_token, private_tokenH, _timeStamp);
		   	getWalletRelations.setWalletRelationsListener(new getWalletRelationsListener(){
	
				@Override
				public void getWalletRelationsPreExecute() {
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void getWalletRelationsComplete(final ArrayList<WalletRelation> _walletRelations) {
					if(_walletRelations != null){
						insertWalletRelationAndUser(_walletRelations, userTable, getUserURL, httpRequest, public_token, private_tokenH, _timeStamp);
					}
					else{
						logOut.logOut(context, httpRequest, context.getString(R.string.logOutURL), public_token, private_tokenH, _timeStamp);
					}
				}
	
				@Override
				public void getWalletRelationsCancelled() {
					// TODO Auto-generated method stub
					
				}
		   		
		   	});
		   	getWalletRelations.execute(getWalletRelationsURL);
	   	}
	}
	
	protected void insertWalletRelationAndUser(final ArrayList<WalletRelation> walletR, UsersController userTable, String getUserURL, 
			DBhttpRequest httpRequest, String public_token, String private_tokenH, String _timeStamp){
		final WalletRelationsController walletRTable = this;
		class putWalletRelations implements Runnable{
			@Override
			public void run(){
				walletRTable.insertWalletRelations(walletR);
			}
		}
		
		Thread insertWalletRs = new Thread(new putWalletRelations());
		insertWalletRs.start();
		
		for(int i = 0; i < walletR.size(); i++){
			if(!userTable.containsId(walletR.get(i).getUserID())){
				userTable.getUserAndInsert(walletR.get(i).getUserID(), getUserURL, httpRequest, public_token, private_tokenH, _timeStamp);
			}
		}
	}
}
