package com.whereone.groupWallet.controllers;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.whereone.groupWallet.controllers.UsersController.usersControllerListener;
import com.whereone.groupWallet.models.WalletRelation;
import com.whereone.groupwalletcake.GetWalletRelations;
import com.whereone.groupwalletcake.GetWalletRelations.getWalletRelationsListener;
import com.whereone.groupwalletcake.R;

public class WalletRelationsController extends SQLiteOpenHelper {
	public static final String TABLE_WALLET_RELATIONS = "walletRelations";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_WALLET_ID = "wallet_id";
	public static final String COLUMN_USER_ID = "user_id";
	
	private Context context;
	private walletRelationsControllerListener listener;
	

	private static final String DATABASE_NAME = "walletRelations.db";
	private static final int DATABASE_VERSION = 1;
	

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
	      + TABLE_WALLET_RELATIONS + "(" + COLUMN_ID
	      + " integer primary key, " + COLUMN_WALLET_ID
	      + " integer not null," + COLUMN_USER_ID
	      + " integer not null);";

	public WalletRelationsController(Context _context) {
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);
		
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
		SQLiteDatabase databaseW = this.getWritableDatabase();
			databaseW.delete(TABLE_WALLET_RELATIONS, null, null);
		databaseW.close();
	}
	
	public void setWalletRelationsControllerListener(walletRelationsControllerListener _listener) {
        this.listener = _listener;
    }
	
	public interface walletRelationsControllerListener{
		public void insertRelationAsyncComplete(ArrayList<Integer> newRelations);
		// 1 == Add Success
		// 0 == Empty Result
		// -1 == Null Result -> due to bad access token(s)
		public void getRelationComplete(Integer result);
		public void insertUserComplete(Integer userID);
		public void getUserComplete(Integer result);
	}
	
	public void insertWalletRelations(ArrayList<WalletRelation> walletRelations){
		SQLiteDatabase databaseW = this.getWritableDatabase();
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
		databaseW.close();
	}
	
	public ArrayList<Integer> getUsersForWallet(Integer walletId){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		if(walletId == null) return null;
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
            databaseR.close();
            return user_ids;
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}
	
	public ArrayList<Integer> getRelationIds(){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLET_RELATIONS, columns, null, null, null, null, null);
		
		if(cursor.getCount() >0)
        {
			ArrayList<Integer> relations = new ArrayList<Integer>();
           
            while (cursor.moveToNext())
            {
                relations.add( cursor.getInt(cursor.getColumnIndex(COLUMN_ID) ));
            }
            databaseR.close();
            return relations;
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}
	
	public Integer getUserFromId(Integer id){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_USER_ID
		};
		
		String whereClause = "id = ?";
		String[] whereArgs = new String[]{
				id.toString()
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLET_RELATIONS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
            Integer user = 0;
            
            cursor.moveToNext();
            
            user = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            databaseR.close();
            return user;
        }
        else
        {
        	databaseR.close();
            return 0;
        }
	}
	
	public Boolean containsUser(Integer walletId, Integer userId){
		SQLiteDatabase databaseR = this.getReadableDatabase();
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
			databaseR.close();
            return true;
        }
        else
        {
        	databaseR.close();
            return false;
        }
	}
	
	public void findWalletRelations(ArrayList<Integer> wallet_ids, Integer userID, final UsersController userTable, final DBhttpRequest httpRequest,
			final String public_token, final String private_tokenH, final String _timeStamp){
		if(wallet_ids == null) return;
		for(int i = 0; i < wallet_ids.size(); i++){
	   		GetWalletRelations getWalletRelations = new GetWalletRelations(httpRequest, this.getRelationIds(), userID, wallet_ids.get(i), public_token, private_tokenH, _timeStamp);
		   	getWalletRelations.setWalletRelationsListener(new getWalletRelationsListener(){
	
				@Override
				public void getWalletRelationsPreExecute() {
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void getWalletRelationsComplete(final ArrayList<WalletRelation> _walletRelations) {
					Integer result;
					if(_walletRelations != null){
						if(_walletRelations.get(0).getID() == -1 && _walletRelations.get(0).getUserID() == -1 && _walletRelations.get(0).getWalletID() == -1){
							Log.i("WalletRelationsController", "Empty Result");
							result = 0;
						}
						else{
							insertWalletRelationAndUser(_walletRelations, userTable, context.getString(R.string.getUserURL), httpRequest, public_token, private_tokenH, _timeStamp);
							result = 1;
						}
					}
					else{
						Log.i("WalletRelationsController", "result is null -> possibly should log out");
						result = -1;
					}
					listener.getRelationComplete(result);
				}
	
				@Override
				public void getWalletRelationsCancelled() {
					// TODO Auto-generated method stub
					
				}
		   		
		   	});
		   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
		   		getWalletRelations.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getString(R.string.getWalletRelationsURL));
		   	}
		   	else {
		   		getWalletRelations.execute(context.getString(R.string.getWalletRelationsURL));
		   	}
	   	}
	}
	
	protected void insertWalletRelationAndUser(final ArrayList<WalletRelation> walletR, UsersController userTable, String getUserURL, 
			DBhttpRequest httpRequest, String public_token, String private_tokenH, String _timeStamp){
		final WalletRelationsController walletRTable = this;
		class putWalletRelations implements Runnable{
			@Override
			public void run(){
				walletRTable.insertWalletRelations(walletR);
				ArrayList<Integer> walletRelations = new ArrayList<Integer>();
				for(int i = 0; i < walletR.size(); i++){
					walletRelations.add(walletR.get(i).getID());
				}
				listener.insertRelationAsyncComplete(walletRelations);
			}
		}
		
		Thread insertWalletRs = new Thread(new putWalletRelations());
		insertWalletRs.start();
		for(int i = 0; i < walletR.size(); i++){
		//	if(!userTable.containsId(walletR.get(i).getUserID())){
				userTable.getUserAndInsert(walletR.get(i).getUserID(), httpRequest, public_token, private_tokenH, _timeStamp);
		//	}
		}
		userTable.setUsersControllerListener(new usersControllerListener(){

			@Override
			public void insertUserAsyncComplete(Integer userID) {
				listener.insertUserComplete(userID);
			}

			@Override
			public void getUserComplete(Integer result) {
				listener.getUserComplete(result);
			}
			
		});
	}
}
