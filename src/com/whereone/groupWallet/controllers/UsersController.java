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

import com.whereone.groupWallet.GetUser;
import com.whereone.groupWallet.GetUser.getUserListener;
import com.whereone.groupWallet.GetWRUsers;
import com.whereone.groupWallet.GetWRUsers.UserWRListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.User;
import com.whereone.groupWallet.models.WalletRelation;

public class UsersController extends SQLiteOpenHelper {
	private static UsersController instance;
	
	private final Semaphore insertAccess = new Semaphore(1, true);
	private final Semaphore checkAccess = new Semaphore(1, true);
	
	private UsersGetListener getListener;
	private UsersGetWRListener getWRListener;
	
	public static final String TABLE_USERS = "users";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_USERNAME = "username";
	public static final String COLUMN_FIRSTNAME = "firstname";
	public static final String COLUMN_LASTNAME = "lastname";
	public static final String COLUMN_EMAIL = "email";
	public static final String COLUMN_FBID = "fbID";
	
	private Context context;

	private static final String DATABASE_NAME = "users.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
	      + TABLE_USERS + "(" + COLUMN_ID
	      + " integer primary key, " + COLUMN_USERNAME
	      + " text not null," + COLUMN_FIRSTNAME
	      + " text not null," + COLUMN_LASTNAME
	      + " text not null," + COLUMN_EMAIL
	      + " text not null," + COLUMN_FBID 
	      + " text);";

	private UsersController(Context _context) {
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}
	
	public static void init(Context _context){
		if(instance == null){
			instance = new UsersController(_context);
			instance.context = _context;
		}
	}
	
	public static UsersController getInstance(){
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
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		onCreate(database);
	}
	
	public void setUsersGetListener(UsersGetListener _listener) {
        instance.getListener = _listener;
    }
	
	public interface UsersGetListener{
		// > 0 == Success => user id
		// 0 == Empty Result
		// -1 == Null Result -> due to bad access token(s)
		// -2 == timeout
		// -3 == unknown host
		// -4 == cancelled
		public void getUserComplete(Integer result);
	}
	
	public void setUsersGetWRListener(UsersGetWRListener listener){
		instance.getWRListener = listener;
	}
	
	public interface UsersGetWRListener{
		public void getWRUsersComplete(Integer result, ArrayList<Integer> resultUsers);
	}
	
	public void removeAll(){
		SQLiteDatabase databaseW = this.getWritableDatabase();
		databaseW.delete(TABLE_USERS, null, null);
		databaseW.close();
	}
	
	public User getUserFromUserName(String username){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID,
				COLUMN_USERNAME,
				COLUMN_FIRSTNAME,
				COLUMN_LASTNAME
		};
		
		String whereClause = "username = ?";
		String[] whereArgs = new String[]{
				username
		};
		
		Cursor cursor = databaseR.query(TABLE_USERS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
			cursor.moveToNext();
			User user = new User();
			user.setFirstName(cursor.getString(cursor.getColumnIndex(COLUMN_FIRSTNAME)));
			user.setLastName(cursor.getString(cursor.getColumnIndex(COLUMN_LASTNAME)));
			user.setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)));
			user.setUserID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
			databaseR.close();
			return user;  
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}
	
	public Integer getIdFromUserName(String username){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		String whereClause = "username = ?";
		String[] whereArgs = new String[]{
				username
		};
		
		Cursor cursor = databaseR.query(TABLE_USERS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
			cursor.moveToNext();
			databaseR.close();
			return cursor.getInt(cursor.getColumnIndex(COLUMN_ID)) ;  
        }
        else
        {
        	databaseR.close();
            return 0;
        }
	}
	
	public String getUserNameFromId(Integer userID){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_USERNAME
		};
		
		String whereClause = "id = ?";
		String[] whereArgs = new String[]{
				userID.toString()
		};
			
		Cursor cursor = databaseR.query(TABLE_USERS, columns, whereClause, whereArgs, null, null, null);
			
		if(cursor.getCount() >0)
        {
            cursor.moveToNext();
            databaseR.close();
           return cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
             
        }
		databaseR.close();
		return "";
	}
	
	public ArrayList<String> getUsersFromIds(ArrayList<Integer> userIDs){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_USERNAME
		};
		
		ArrayList<String> user_name = new ArrayList<String>();
		
		for(int i = 0; i < userIDs.size(); i++){
		
			String whereClause = "id = ?";
			String[] whereArgs = new String[]{
					userIDs.get(i).toString()
			};
			
			Cursor cursor = databaseR.query(TABLE_USERS, columns, whereClause, whereArgs, null, null, null);
			
			if(cursor.getCount() >0)
	        {
	            while (cursor.moveToNext())
	            {
	                 user_name.add( cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)) );
	             }
	        }
		}
		databaseR.close();
		return user_name;
	}
	
	public ArrayList<User> getUserssFromIds(ArrayList<Integer> userIDs){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		if(userIDs == null) return null;
		String[] columns = {
				COLUMN_ID,
				COLUMN_USERNAME,
				COLUMN_FIRSTNAME,
				COLUMN_LASTNAME,
				COLUMN_EMAIL
		};
		
		ArrayList<User> users = new ArrayList<User>();
		
		for(int i = 0; i < userIDs.size(); i++){
		
			String whereClause = "id = ?";
			String[] whereArgs = new String[]{
					userIDs.get(i).toString()
			};
			
			Cursor cursor = databaseR.query(TABLE_USERS, columns, whereClause, whereArgs, null, null, null);
			
			if(cursor.getCount() >0)
	        {
	            while (cursor.moveToNext())
	            {
	                 users.add( new User(
	                		 cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
	                		 cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)),
	                		 cursor.getString(cursor.getColumnIndex(COLUMN_FIRSTNAME)),
	                		 cursor.getString(cursor.getColumnIndex(COLUMN_LASTNAME)),
	                		 cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)),
	                		 null
	                	));
	             }
	        }
		}
		databaseR.close();
		return users;
	}
	
	public User getUserFromId(Integer userID){
		SQLiteDatabase databaseR = this.getReadableDatabase();

		String[] columns = {
				COLUMN_ID,
				COLUMN_USERNAME,
				COLUMN_FIRSTNAME,
				COLUMN_LASTNAME,
				COLUMN_EMAIL
		};
		
		String whereClause = "id = ?";
		String[] whereArgs = new String[]{
				userID.toString()
		};
		
		Cursor cursor = databaseR.query(TABLE_USERS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
            cursor.moveToNext();
            databaseR.close();
            return ( new User(
        		 cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
        		 cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)),
        		 cursor.getString(cursor.getColumnIndex(COLUMN_FIRSTNAME)),
        		 cursor.getString(cursor.getColumnIndex(COLUMN_LASTNAME)),
        		 cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)),
        		 null
        	));
             
        }
		
		databaseR.close();
		return null;
	}
	
	public void insertUser(User user){
		try {
			insertAccess.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SQLiteDatabase databaseW = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id", user.getUserID());
		values.put("username", user.getUserName());
		values.put("firstname", user.getFirstName());
		values.put("lastname", user.getLastName());
		values.put("email", user.getEmail());
		try{
			databaseW.insertOrThrow(TABLE_USERS, null, values);
		}
		catch (SQLiteConstraintException e){
			Log.i("Wallet Constraint Exception", user.getUserID() + " duplicate");
		}
		catch (NullPointerException e){
			Log.i("UserCont insertUser", "NullPointerException " + user.getUserID() + " " + user.getName());
		}
		databaseW.close();
		insertAccess.release();
	}
	
	public void insertUsers(ArrayList<User> users){
		SQLiteDatabase databaseW = this.getWritableDatabase();
		for(Integer i = 0; i < users.size(); i++){
			User curUser = users.get(i);
			ContentValues values = new ContentValues();
			values.put("id", curUser.getUserID());
			values.put("username", curUser.getUserName());
			values.put("firstname", curUser.getFirstName());
			values.put("lastname", curUser.getLastName());
			values.put("email", curUser.getEmail());
			try{
				databaseW.insertOrThrow(TABLE_USERS, null, values);
			}
			catch (SQLiteConstraintException e){
				Log.i("Wallet Constraint Exception", curUser.getUserID() + " duplicate");
			}
		}
		databaseW.close();
	}
	
	public ArrayList<String> getUserNames(){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_USERNAME
		};
		
		Cursor cursor = databaseR.query(TABLE_USERS, columns, null, null, null, null, null);
		
		if(cursor.getCount() >0)
        {
            ArrayList<String> str = new ArrayList<String>();
            
            while (cursor.moveToNext())
            {
                 str.add( cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)) );
             }
            databaseR.close();
            return str;
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}
	
	public Boolean containsId(Integer id){
		try {
			checkAccess.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		String whereClause = "id = ?";
		String[] whereArgs = new String[]{
				id.toString()
		};
		
		Cursor cursor = databaseR.query(TABLE_USERS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() > 0){
			databaseR.close();
			checkAccess.release();
			return true;
		}
		else{
			databaseR.close();
			checkAccess.release();
			return false;
		}
	}

	private Integer numNewUsers;
	public void findUsersForWRs(DBhttpRequest httpRequest, Profile profile, ArrayList<WalletRelation> walletR){
		
		final ArrayList<Integer> users = new ArrayList<Integer>();
		
		if(numNewUsers == null){
			numNewUsers = 0;
		}
		
		for(int i = 0; i < walletR.size(); i++){
			if( !instance.containsId( walletR.get(i).getUserID() ) && !users.contains(walletR.get(i).getUserID()) ){
				users.add( walletR.get(i).getUserID() );
				numNewUsers++;
			}
		}
		
		if(users.size() == 0){
			instance.getWRListener.getWRUsersComplete(0, null);
		}
		else{
			getUsersAndInsert(httpRequest, profile, users);
		}
	}
	
	public void getUsersAndInsert(DBhttpRequest httpRequest, Profile profile, ArrayList<Integer> uids){
		GetWRUsers getWRUsers = new GetWRUsers(httpRequest, profile, uids);
	   	getWRUsers.setWRUsersListener(new UserWRListener(){

			@Override
			public void getUsersPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getUsersCompleted(final ArrayList<User> users, String resultString) {
				Integer result;
				if(users != null){
					
					new Thread(){
						
						@Override
						public void run(){
							instance.insertUsers(users);
							ArrayList<Integer> returnUsers = new ArrayList<Integer>();
							for(int i = 0; i < users.size(); i++){
								returnUsers.add( users.get(i).getUserID() );
							}
							instance.getWRListener.getWRUsersComplete(1, returnUsers);
						}
						
					}.start();
					
				}
				else{
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
					
					instance.getWRListener.getWRUsersComplete(result, null);
				}
				
			}

			@Override
			public void getUsersCancelled() {
				instance.getListener.getUserComplete(-4);
				Log.i("UsersController", "getUsersCanelled" );
			}
	   		
	   	});
	   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
	   		getWRUsers.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instance.context.getString(R.string.getWRUsersURL));
	   	}
	   	else {
	   		getWRUsers.execute(instance.context.getString(R.string.getWRUsersURL));
	   	}
	}
	
	public void getUserAndInsert(DBhttpRequest httpRequest, Profile profile, Integer id){
		GetUser getUser = new GetUser(httpRequest, profile, id);
	   	getUser.setUserListener(new getUserListener(){

			@Override
			public void getUserPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getUserCompleted(User user, String resultString) {
				Integer result;
				if(user != null){
					instance.insertUser(user);
					instance.getListener.getUserComplete(user.getUserID());
				}
				else{
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
					Log.i("UsersController", "result is null : " + resultString);
					instance.getListener.getUserComplete(result);
				}
				
			}

			@Override
			public void getUserCancelled() {
				instance.getListener.getUserComplete(-4);
				Log.i("UsersController", "getUserCanelled" );
			}
	   		
	   	});
	   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
	   		getUser.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instance.context.getString(R.string.getUserURL));
	   	}
	   	else {
	   		getUser.execute(instance.context.getString(R.string.getUserURL));
	   	}
	}
	
}
