package com.whereone.groupWallet.controllers;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.whereone.groupWallet.models.User;
import com.whereone.groupwalletcake.GetUser;
import com.whereone.groupwalletcake.LogOutCurrent;
import com.whereone.groupwalletcake.R;
import com.whereone.groupwalletcake.GetUser.getUserListener;

public class UsersController extends SQLiteOpenHelper {

	private usersControllerListener listener;
	
	public static final String TABLE_USERS = "users";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_USERNAME = "username";
	public static final String COLUMN_FIRSTNAME = "firstname";
	public static final String COLUMN_LASTNAME = "lastname";
	public static final String COLUMN_EMAIL = "email";
	
	private SQLiteDatabase databaseW;
	private SQLiteDatabase databaseR;
	private LogOutCurrent logOut;
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
	      + " test not null);";

	public UsersController(Context _context, LogOutCurrent _logOut) {
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
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		    onCreate(database);
	}
	
	public void setUsersControllerListener(usersControllerListener _listener) {
        this.listener = _listener;
    }
	
	public interface usersControllerListener{
		public void insertUserAsyncComplete();
	}
	
	public void removeAll(){
		databaseW.delete(TABLE_USERS, null, null);
	}
	
	public void insertUsers(ArrayList<User> users){
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
	}
	
	public User getUserFromUserName(String username){
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
			return user;  
        }
        else
        {
            return null;
        }
	}
	
	public Integer getIdFromUserName(String username){
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
			return cursor.getInt(cursor.getColumnIndex(COLUMN_ID)) ;  
        }
        else
        {
            return 0;
        }
	}
	
	public ArrayList<String> getUsersFromIds(ArrayList<Integer> userIDs){
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
		return user_name;
	}
	
	public void insertUser(User user){
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
	}
	
	public ArrayList<String> getUserNames(){
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
            return str;
        }
        else
        {
            return null;
        }
	}
	
	public Boolean containsId(Integer id){
		String[] columns = {
				COLUMN_ID
		};
		
		Cursor cursor = databaseR.query(TABLE_USERS, columns, null, null, null, null, null);
		
		if(cursor.getCount() > 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void getUserAndInsert(Integer id, String getUserURL, final DBhttpRequest httpRequest, final String public_token, final String private_tokenH, final String _timeStamp){
		GetUser getUser = new GetUser(httpRequest, id, public_token, private_tokenH, _timeStamp);
	   	getUser.setUserListener(new getUserListener(){

			@Override
			public void getUserPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getUserComplete(User user) {
				if(user != null){
					insertUserAsync(user);
				}
				else{
					logOut.logOut(context, httpRequest, context.getString(R.string.logOutURL), public_token, private_tokenH, _timeStamp);
				}
			}

			@Override
			public void getUserCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	getUser.execute(getUserURL);
	}
	
	protected void insertUserAsync(final User user){
		final UsersController userTable = this;
		class putUser implements Runnable{
			@Override
			public void run(){
				userTable.insertUser(user);
				listener.insertUserAsyncComplete();
			}
		}
		
		Thread insertUser = new Thread(new putUser());
		insertUser.start();
	}
}
