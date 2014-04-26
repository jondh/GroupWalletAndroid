package com.whereone.groupWallet.controllers;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.whereone.groupWallet.AcceptDeclineWallet;
import com.whereone.groupWallet.AcceptDeclineWallet.AcceptDeclineListener;
import com.whereone.groupWallet.GetWalletRelations;
import com.whereone.groupWallet.GetWalletRelations.getWalletRelationsListener;
import com.whereone.groupWallet.InsertWalletRelation;
import com.whereone.groupWallet.InsertWalletRelation.InsertWalletRelationListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.UsersController.UsersGetListener;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.WalletRelation;

public class WalletRelationsController extends SQLiteOpenHelper {
	public static final String TABLE_WALLET_RELATIONS = "walletRelations";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_WALLET_ID = "wallet_id";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_ACCEPT = "accept";
	
	private final Semaphore insertWRAccess = new Semaphore(1, true);
	
	private Context context;
	private WalletRelationsGetListener getListener;
	private WalletRelationInsertListener insertListener;
	private UpdateListener updateListener;
	private static WalletRelationsController instance;

	private static final String DATABASE_NAME = "walletRelations.db";
	private static final int DATABASE_VERSION = 1;
	
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
	      + TABLE_WALLET_RELATIONS + "(" + COLUMN_ID
	      + " integer primary key, " + COLUMN_WALLET_ID
	      + " integer not null," + COLUMN_USER_ID
	      + " integer not null," + COLUMN_ACCEPT
	      + " integer not null);";

	private WalletRelationsController(Context _context) {
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}
	
	public static void init(Context _context){
		if(instance == null){
			instance = new WalletRelationsController(_context);
			instance.context = _context;
		}
	}
	
	public static WalletRelationsController getInstance(){
		return instance;
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
	
	public void setWalletRelationsGetListener(WalletRelationsGetListener _listener) {
		instance.getListener = _listener;
    }
	
	public interface WalletRelationsGetListener{
		// 1 == Add Success
		// 0 == Empty Result
		// -1 == Null Result -> due to bad access token(s)
		public void getRelationsComplete(ArrayList<Integer> result, ArrayList<WalletRelation> newRelations);
	}
	
	public void setWalletRelationInsertListener(WalletRelationInsertListener listener){
		instance.insertListener = listener;
	}
	
	public interface WalletRelationInsertListener{
		public void insertPutComplete(Integer result);
	}
	
	public void setUpdateListener(UpdateListener listener){
		instance.updateListener = listener;
	}
	
	public interface UpdateListener{
		public void acceptDeclineComplete(Integer result);
	}
	
	public void insertWalletRelations(ArrayList<WalletRelation> walletRelations){
		try {
			insertWRAccess.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SQLiteDatabase databaseW = this.getWritableDatabase();
		for(Integer i = 0; i < walletRelations.size(); i++){
			WalletRelation curWalletRelation = walletRelations.get(i);
			ContentValues values = new ContentValues();
			values.put("id", curWalletRelation.getID());
			values.put("wallet_id", curWalletRelation.getWalletID());
			values.put("user_id", curWalletRelation.getUserID());
			values.put("accept", curWalletRelation.getAccept() );
			try{
				//databaseW.insertOrThrow(TABLE_WALLET_RELATIONS, null, values);
				databaseW.insertWithOnConflict(TABLE_WALLET_RELATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			}
			catch (SQLiteConstraintException e){
				Log.i("Wallet Constraint Exception", curWalletRelation.getID() + " duplicate");
			}
			catch (NullPointerException e){
				Log.i("WalletRelation Null Ptr", "NullPointerException");
			}
		}
		databaseW.close();
		insertWRAccess.release();
	}
	
	public ArrayList<Integer> getUsersForWallet(Integer walletId, Integer userID, Integer accept){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		if(walletId == null) return null;
		
		String[] columns = {
				COLUMN_USER_ID
		};
		
		String whereClause;
		String[] whereArgs;
		
		if(userID == null){
			if(accept == null){
				whereClause = "wallet_id = ?";
				whereArgs = new String[]{
						walletId.toString()
				};
			}
			else{
				whereClause = "wallet_id = ? AND accept = ?";
				whereArgs = new String[]{
						walletId.toString(),
						accept.toString()
				};
			}
		}
		else{
			if(accept == null){
				whereClause = "wallet_id = ? AND user_id <> ?";
				whereArgs = new String[]{
						walletId.toString(),
						userID.toString()
				};
			}
			else{
				whereClause = "wallet_id = ? AND user_id <> ? AND accept = ?";
				whereArgs = new String[]{
						walletId.toString(),
						userID.toString(),
						accept.toString()
				};
			}
		}
		
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
	
	public ArrayList<Integer> getWalletsForUser(Integer userID, Integer accept){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		ArrayList<Integer> wallet_ids = new ArrayList<Integer>();
		if(userID == null) return wallet_ids;
		String[] columns = {
				COLUMN_WALLET_ID
		};
		
		String whereClause = "user_id = ? AND accept = ?";
		String[] whereArgs = new String[]{
				userID.toString(),
				accept.toString()
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLET_RELATIONS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
            
            
            while (cursor.moveToNext())
            {
                 wallet_ids.add( cursor.getInt(cursor.getColumnIndex(COLUMN_WALLET_ID)) );
             }
            databaseR.close();
            return wallet_ids;
        }
        else
        {
        	databaseR.close();
        	return wallet_ids;
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
	
	public ArrayList<Integer> getRelationIdsForWallet(Integer walletID, Integer accept){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		String whereClause;
		String[] whereArgs;
		
		if(accept != null){
			whereClause = "wallet_id = ? AND accept = ?";
			whereArgs = new String[]{
				walletID.toString(),
				accept.toString()
			};
		}
		else{
			whereClause = "wallet_id = ?";
			whereArgs = new String[]{
				walletID.toString()
			};
		}
		
		Cursor cursor = databaseR.query(TABLE_WALLET_RELATIONS, columns, whereClause, whereArgs, null, null, null);
		
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
	
	public Boolean containsID(Integer id){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		String whereClause = "id = ?";
		String[] whereArgs = new String[]{
				id.toString()
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
	
	public void updateAccept(Integer walletId, Integer userId, Integer accept){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		
		String whereClause = "wallet_id = ? AND user_id = ?";
		String[] whereArgs = new String[]{
				walletId.toString(),
				userId.toString()
		};
		
		ContentValues args = new ContentValues();
		args.put(COLUMN_ACCEPT, accept);
		
		databaseR.update(TABLE_WALLET_RELATIONS, args, whereClause, whereArgs);
		databaseR.close();
	}
	
	public void deleteRow(Integer walletId, Integer userId){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		
		String whereClause = "wallet_id = ? AND user_id = ?";
		String[] whereArgs = new String[]{
				walletId.toString(),
				userId.toString()
		};
		
		databaseR.delete(TABLE_WALLET_RELATIONS, whereClause, whereArgs);
		databaseR.close();
	}
	
	public void findWalletRelations(final DBhttpRequest httpRequest, final Profile profile, final ArrayList<Integer> wallet_ids){

		final ArrayList<Integer> resultNumber = new ArrayList<Integer>();
		final ArrayList<WalletRelation> totalWR = new ArrayList<WalletRelation>();
		if(wallet_ids == null) return;
		for(int i = 0; i < wallet_ids.size(); i++){
			System.out.println(wallet_ids.get(i));
	   		GetWalletRelations getWalletRelations = new GetWalletRelations(httpRequest, profile, instance.getRelationIdsForWallet(wallet_ids.get(i), 1), wallet_ids.get(i));
		   	getWalletRelations.setWalletRelationsListener(new getWalletRelationsListener(){
	
				@Override
				public void getWalletRelationsPreExecute() {
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void getWalletRelationsComplete(final ArrayList<WalletRelation> _walletRelations, String resultString) {
					Integer result;
					if(_walletRelations != null){ // if wallet relations found, put into db and find users if not in db
						instance.insertWalletRelations(_walletRelations);
						totalWR.addAll(_walletRelations);
						result = 1;
					}
					else{ // if nothing found, trigger the listener with the result code
						if(resultString.contains("timeout")){
							result = -2;
						}
						else if(resultString.contains("unknownHost")){
							result = -3;
						}
						else if(resultString.contains("empty")){
							result = 0;
						}
						else{
							result = -1;
						}
					}
					resultNumber.add(result);
					System.out.println("current: " + resultNumber.size() + " " + wallet_ids.size());
					if(resultNumber.size() == wallet_ids.size()){
						instance.getListener.getRelationsComplete(resultNumber, totalWR);
					}
				}
	
				@Override
				public void getWalletRelationsCancelled() {
					ArrayList<Integer> ttem = new ArrayList<Integer>();
					ttem.add(-4);
					instance.getListener.getRelationsComplete(ttem, null);
				}
		   		
		   	});
		   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
		   		getWalletRelations.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instance.context.getString(R.string.getWalletRelationsURL));
		   	}
		   	else {
		   		getWalletRelations.execute(instance.context.getString(R.string.getWalletRelationsURL));
		   	}
	   	}
	}
	/*
	public void findPendingWalletRelations(final DBhttpRequest httpRequest, final Profile profile, final ArrayList<Integer> wallet_ids){
		final ArrayList<Integer> pendingResultNumber = new ArrayList<Integer>();
		final ArrayList<WalletRelation> totalWR = new ArrayList<WalletRelation>();
		if(wallet_ids == null){
			ArrayList<Integer> ttem = new ArrayList<Integer>();
			ttem.add(0);
			instance.getListener.getRelationsComplete(ttem, null);
			return;
		}
		for(int i = 0; i < wallet_ids.size(); i++){
	   		GetWalletRelations getWalletRelations = new GetWalletRelations(httpRequest, profile, this.getRelationIdWallet( wallet_ids.get(i) ), wallet_ids.get(i));
		   	getWalletRelations.setWalletRelationsListener(new getWalletRelationsListener(){
	
				@Override
				public void getWalletRelationsPreExecute() {
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void getWalletRelationsComplete(final ArrayList<WalletRelation> _walletRelations, String resultString) {
					Integer result;
					if(_walletRelations != null){ // if wallet relations found, put into db and find users if not in db
						instance.insertWalletRelations(_walletRelations);
						totalWR.addAll(_walletRelations);
						result = 1;
					}
					else{ // if nothing found, trigger the listener with the result code
						if(resultString.contains("timeout")){
							result = -2;
						}
						else if(resultString.contains("unknownHost")){
							result = -3;
						}
						else if(resultString.contains("empty")){
							result = 0;
						}
						else{
							result = -1;
						}
					}
					pendingResultNumber.add(result);
					System.out.println("pending: " + pendingResultNumber.size() + " " + wallet_ids.size());
					if(pendingResultNumber.size() == wallet_ids.size()){
						instance.getListener.getRelationsComplete(pendingResultNumber, totalWR);
					}
				}
	
				@Override
				public void getWalletRelationsCancelled() {
					ArrayList<Integer> ttem = new ArrayList<Integer>();
					ttem.add(-4);
					instance.getListener.getRelationsComplete(ttem, null);
				}
		   		
		   	});
		   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
		   		getWalletRelations.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instance.context.getString(R.string.getPendingWalletRelationsURL));
		   	}
		   	else {
		   		getWalletRelations.execute(instance.context.getString(R.string.getPendingWalletRelationsURL));
		   	}
	   	}
	}
	*/
	public void insertPutWalletRelation(final DBhttpRequest httpRequest, final Profile profile, final WalletRelation walletRelation, final UsersController usersController){
		
		InsertWalletRelation insertWalletRelation = new InsertWalletRelation(httpRequest, profile, walletRelation);
		
		insertWalletRelation.setinsertWalletRelationListener(new InsertWalletRelationListener(){

			@Override
			public void insertWalletRelationPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertWalletRelationComplete(WalletRelation walletRelation, String resultString) {
				
				if(walletRelation != null){
					ArrayList<WalletRelation> walletRelations = new ArrayList<WalletRelation>();
					walletRelations.add(walletRelation);
					instance.insertWalletRelations(walletRelations);
					
					usersController.setUsersGetListener(new UsersGetListener(){

						@Override
						public void getUserComplete(Integer result) {
							System.out.println(result+"");
							if(instance.insertListener != null){
								instance.insertListener.insertPutComplete(result);
							}
						}
						
					});
					if( !usersController.containsId( walletRelation.getUserID() )){
						usersController.getUserAndInsert( httpRequest, profile, walletRelation.getUserID() );
					}
					else if(instance.insertListener != null){
						instance.insertListener.insertPutComplete(0);
					}
					
				}
				else{
					Integer result;
					if(resultString.contains("timeout")){
						result = -2;
					}
					else if(resultString.contains("unknownHost")){
						result = -3;
					}
					else{
						result = -1;
					}
					if(instance.insertListener != null){
						instance.insertListener.insertPutComplete(result);
					}
				}
			}

			@Override
			public void insertWalletRelationCancelled() {
				// TODO Auto-generated method stub
				
			}
			
		});
		insertWalletRelation.execute(context.getString(R.string.insertWalletRelationURL));
		
	}
	
	public void acceptDeclineWallet(DBhttpRequest httpRequest, final Profile profile, final Integer walletID, final AcceptDeclineWallet.Type type){
		AcceptDeclineWallet acceptDeclineWallet = new AcceptDeclineWallet(httpRequest, profile, walletID, type);
		acceptDeclineWallet.setAcceptDeclineListener(new AcceptDeclineListener(){

			@Override
			public void onPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete(String resultString) {
				Integer result;
				if(resultString.contains("success")){
					result = 1;
					if(type == AcceptDeclineWallet.Type.ACCEPT){
						instance.updateAccept(walletID, profile.getUserID(), type.ordinal());
					}
					else if(type == AcceptDeclineWallet.Type.DECLINE){
						instance.deleteRow(walletID, profile.getUserID());
					}
				}
				else if(resultString.contains("timeout")){
					result = -2;
				}
				else if(resultString.contains("unknownHost")){
					result = -3;
				}
				else{
					result = -1;
				}
				if( instance.updateListener != null){
					instance.updateListener.acceptDeclineComplete(result);
				}
			}

			@Override
			public void onCancelled() {
				// TODO Auto-generated method stub
				
			}
			
		});
		acceptDeclineWallet.execute( context.getString(R.string.acceptDeclineWalletURL) );
	}
}
