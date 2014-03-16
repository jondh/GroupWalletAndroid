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

import com.whereone.groupWallet.models.User;
import com.whereone.groupwalletcake.GetUser;
import com.whereone.groupwalletcake.GetUser.getUserListener;
import com.whereone.groupwalletcake.R;

public class UsersController extends SQLiteOpenHelper {

	private usersControllerListener listener;
	
	public static final String TABLE_USERS = "users";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_USERNAME = "username";
	public static final String COLUMN_FIRSTNAME = "firstname";
	public static final String COLUMN_LASTNAME = "lastname";
	public static final String COLUMN_EMAIL = "email";
	
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
	      + " text not null);";

	public UsersController(Context _context) {
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
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		onCreate(database);
	}
	
	public void setUsersControllerListener(usersControllerListener _listener) {
        this.listener = _listener;
    }
	
	public interface usersControllerListener{
		public void insertUserAsyncComplete(Integer userID);
		// 1 == Add Success
		// 0 == Empty Result
		// -1 == Null Result -> due to bad access token(s)
		public void getUserComplete(Integer result);
	}
	
	public void removeAll(){
		SQLiteDatabase databaseW = this.getWritableDatabase();
		databaseW.delete(TABLE_USERS, null, null);
		databaseW.close();
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
	
	public String getUserFromId(Integer userID){
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
	                		 0
	                	));
	             }
	        }
		}
		databaseR.close();
		return users;
	}
	
	public void insertUser(User user){
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
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		Cursor cursor = databaseR.query(TABLE_USERS, columns, null, null, null, null, null);
		
		if(cursor.getCount() > 0){
			databaseR.close();
			return true;
		}
		else{
			databaseR.close();
			return false;
		}
	}
	
	public void getUserAndInsert(Integer id, final DBhttpRequest httpRequest, final String public_token, final String private_tokenH, final String _timeStamp){
		GetUser getUser = new GetUser(httpRequest, id, public_token, private_tokenH, _timeStamp);
	   	getUser.setUserListener(new getUserListener(){

			@Override
			public void getUserPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getUserCompleted(User user) {
				if(user != null){
				Log.i("User Result", user.getUserName());}
				Integer result;
				if(user != null){
					if(user.getUserID() == -1 && user.getUserName() == "-1" && user.getFirstName() == "-1"){
						Log.i("UsersController", "Empty Result");
						result = 0;
					}
					else{
						insertUserAsync(user);
						result = 1;
					}
				}
				else{
					Log.i("UsersController", "result is null -> possibly should log out");
					result = -1;
					//logOut.logOut(context, httpRequest, context.getString(R.string.logOutURL), public_token, private_tokenH, _timeStamp);
				}
				listener.getUserComplete(result);
			}

			@Override
			public void getUserCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
	   		getUser.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getString(R.string.getUserURL));
	   	}
	   	else {
	   		getUser.execute(context.getString(R.string.getUserURL));
	   	}
	}
	
	protected void insertUserAsync(final User user){
		final UsersController userTable = this;
		class putUser implements Runnable{
			@Override
			public void run(){
				userTable.insertUser(user);
				listener.insertUserAsyncComplete(user.getUserID());
			}
		}
		
		Thread insertUser = new Thread(new putUser());
		insertUser.start();
	}
	
	
}
