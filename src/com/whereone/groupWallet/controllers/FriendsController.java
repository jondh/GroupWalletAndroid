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

import com.whereone.groupWallet.AcceptDeclineFriend;
import com.whereone.groupWallet.AcceptDeclineFriend.AcceptDeclineFriendListener;
import com.whereone.groupWallet.GetFriends;
import com.whereone.groupWallet.GetFriends.GetFriendsListener;
import com.whereone.groupWallet.InsertFriend;
import com.whereone.groupWallet.InsertFriend.InsertFriendListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.UsersController.UsersGetListener;
import com.whereone.groupWallet.models.Friend;
import com.whereone.groupWallet.models.Profile;

public class FriendsController extends SQLiteOpenHelper {
	public static final String TABLE_FRIENDS = "friends";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_USER_1 = "user_1";
	public static final String COLUMN_USER_2 = "user_2";
	public static final String COLUMN_ACCEPT = "accept";
	
	private Context context;
	private FriendGetListener getListener;
	private FriendInsertListener insertListener;
	private UpdateFriendsListener updateListener;
	private static FriendsController instance;

	private static final String DATABASE_NAME = "friends.db";
	private static final int DATABASE_VERSION = 1;
	
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
	      + TABLE_FRIENDS + "(" + COLUMN_ID
	      + " integer primary key, " + COLUMN_USER_1
	      + " integer not null," + COLUMN_USER_2
	      + " integer not null," + COLUMN_ACCEPT
	      + " integer not null);";

	private FriendsController(Context _context) {
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}
	
	public static void init(Context _context){
		if(instance == null){
			instance = new FriendsController(_context);
			instance.context = _context;
		}
	}
	
	public static FriendsController getInstance(){
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
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
		    onCreate(database);
	}
	
	public void removeAll(){
		SQLiteDatabase databaseW = this.getWritableDatabase();
			databaseW.delete(TABLE_FRIENDS, null, null);
		databaseW.close();
	}
	
	public void setFriendGetListener(FriendGetListener _listener) {
		instance.getListener = _listener;
    }
	
	public interface FriendGetListener{
		// 1 == Add Success
		// 0 == Empty Result
		// -1 == Null Result -> due to bad access token(s)
		public void getRelationsComplete(Integer result, ArrayList<Friend> newFriends);
	}
	
	public void setFriendInsertListener(FriendInsertListener listener){
		instance.insertListener = listener;
	}
	
	public interface FriendInsertListener{
		public void insertPutComplete(Integer result);
	}
	
	public void setUpdateListener(UpdateFriendsListener listener){
		instance.updateListener = listener;
	}
	
	public interface UpdateFriendsListener{
		public void acceptDeclineComplete(Integer result);
	}
	
	public Boolean containsUser(Integer userID, Integer accept){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		String whereClause;
		String[] whereArgs;
		
		whereClause = "user_1 = ? OR user_2 = ? AND accept = ?";
		whereArgs = new String[]{
			userID.toString(),	
			userID.toString(),
			accept.toString()
		};
		
		Cursor cursor = databaseR.query(TABLE_FRIENDS, columns, whereClause, whereArgs, null, null, null);
		
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
	
	public ArrayList<Integer> getFriendIds(Integer accept){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		Cursor cursor;
		
		if(accept != null){
			String whereClause = "accept = ?";
			String[] whereArgs = new String[]{
					accept.toString()
			};
			
			cursor = databaseR.query(TABLE_FRIENDS, columns, whereClause, whereArgs, null, null, null);
		}
		else{
			cursor = databaseR.query(TABLE_FRIENDS, columns, null, null, null, null, null);
		}
		
		ArrayList<Integer> friends = new ArrayList<Integer>();
		if(cursor.getCount() >0)
        {
            while (cursor.moveToNext())
            {
                friends.add( cursor.getInt(cursor.getColumnIndex(COLUMN_ID) ));
            }
            databaseR.close();
            return friends;
        }
        else
        {
        	databaseR.close();
            return friends;
        }
	}
	
	public ArrayList<Integer> getUserIds(Integer profileID, Integer accept){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		
		String[] columns = {
				COLUMN_USER_1,
				COLUMN_USER_2
		};
		
		String whereClause;
		String[] whereArgs;
		
		Cursor cursor;
		
		if(accept != null){
			whereClause = "accept = ?";
			whereArgs = new String[]{
					accept.toString()
			};
			
			cursor = databaseR.query(TABLE_FRIENDS, columns, whereClause, whereArgs, null, null, null);
		}
		else{
			cursor = databaseR.query(TABLE_FRIENDS, columns, null, null, null, null, null);
		}
		
		ArrayList<Integer> user_ids = new ArrayList<Integer>();
		
		if(cursor.getCount() >0)
        {
            while (cursor.moveToNext())
            {
            	Integer user1 = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_1));
            	Integer user2 = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_2));
            	if( user1 == profileID ){
            		user_ids.add( user2 );
            	}
            	else{
            		user_ids.add( user1 );
            	}
             }
            databaseR.close();
            return user_ids;
        }
        else
        {
        	databaseR.close();
            return user_ids;
        }
	}
	
	public ArrayList<Friend> getFriendRequests(Integer userID){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		
		String[] columns = {
				COLUMN_ID,
				COLUMN_USER_1,
				COLUMN_USER_2,
				COLUMN_ACCEPT
		};
		
		String whereClause;
		String[] whereArgs;
		
		whereClause = "user_2 = ? AND accept = ?";
		whereArgs = new String[]{
			userID.toString(),	
			"0"
		};
		
		Cursor cursor = databaseR.query(TABLE_FRIENDS, columns, whereClause, whereArgs, null, null, null);
		
		
		ArrayList<Friend> friends = new ArrayList<Friend>();
		
		if(cursor.getCount() >0)
        {
            while (cursor.moveToNext())
            {
            	friends.add( new Friend(
            			cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
            			cursor.getInt(cursor.getColumnIndex(COLUMN_USER_1)),
            			cursor.getInt(cursor.getColumnIndex(COLUMN_USER_2)),
            			false));
             }
            databaseR.close();
            return friends;
        }
        else
        {
        	databaseR.close();
            return friends;
        }
	}
	
	public void insertFriends(ArrayList<Friend> friends){
		SQLiteDatabase databaseW = this.getWritableDatabase();
		
		for(Integer i = 0; i < friends.size(); i++){
			Friend curFriend = friends.get(i);
			ContentValues values = new ContentValues();
			values.put(COLUMN_ID, curFriend.getID() );
			values.put(COLUMN_USER_1, curFriend.getUser1() );
			values.put(COLUMN_USER_2, curFriend.getUser2() );
			values.put(COLUMN_ACCEPT, curFriend.getAccept() );
			try{
			//	databaseW.insertOrThrow(TABLE_FRIENDS, null, values);
				databaseW.insertWithOnConflict(TABLE_FRIENDS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			}
			catch (SQLiteConstraintException e){
				Log.i("Wallet Constraint Exception", curFriend.getID() + " duplicate");
			}
			catch (NullPointerException e){
				Log.i("WalletRelation Null Ptr", "NullPointerException");
			}
		}
		databaseW.close();
	}
	
	
	public void updateAccept(Integer user1, Integer user2, Integer accept){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		
		String whereClause = "user_1 = ? AND user_2 = ?";
		String[] whereArgs = new String[]{
				user1.toString(),
				user2.toString()
		};
		
		ContentValues args = new ContentValues();
		args.put(COLUMN_ACCEPT, accept);
		
		databaseR.update(TABLE_FRIENDS, args, whereClause, whereArgs);
		databaseR.close();
	}
	
	public void deleteRow(Integer user1, Integer user2){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		
		String whereClause = "user_1 = ? AND user_2 = ?";
		String[] whereArgs = new String[]{
				user1.toString(),
				user2.toString()
		};
		
		databaseR.delete(TABLE_FRIENDS, whereClause, whereArgs);
		databaseR.close();
	}
	
	public void findFriends(final DBhttpRequest httpRequest, final Profile profile){

   		GetFriends getFriends = new GetFriends(httpRequest, profile, instance.getFriendIds(1) );
	   	getFriends.setGetFriendsListener(new GetFriendsListener(){

			@Override
			public void getFriendsPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getFriendsComplete(final ArrayList<Friend> friends, String resultString) {
				Integer result;
				if(friends != null){ 
					instance.insertFriends(friends);
					result = 1;
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
				}
				if(instance.getListener != null){
					instance.getListener.getRelationsComplete(result, friends);
				}
			}

			@Override
			public void getFriendsCancelled() {
				instance.getListener.getRelationsComplete(-4, null);
			}
	   		
	   	});
	   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
	   		getFriends.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instance.context.getString(R.string.getFriendsURL));
	   	}
	   	else {
	   		getFriends.execute(instance.context.getString(R.string.getFriendsURL));
	   	}
	}
	
	public void insertPutFriend(final DBhttpRequest httpRequest, final Profile profile, final Friend friend, final UsersController usersController){
		
		InsertFriend insertFriend = new InsertFriend(httpRequest, profile, friend);
		
		insertFriend.setInsertFriendListener(new InsertFriendListener(){

			@Override
			public void insertFriendPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertFriendComplete(Friend resultFriend, String resultString) {
				
				if(resultFriend != null){
					ArrayList<Friend> friends = new ArrayList<Friend>();
					friends.add(resultFriend);
					instance.insertFriends(friends);
					
					usersController.setUsersGetListener(new UsersGetListener(){

						@Override
						public void getUserComplete(Integer result) {
							System.out.println(result+"");
							if(instance.insertListener != null){
								instance.insertListener.insertPutComplete(result);
							}
						}
						
					});
					
					Boolean newUser = false;
					
					if( profile.getUserID() == resultFriend.getUser1() ){
						if( !usersController.containsId( friend.getUser2() )){
							newUser = true;
							usersController.getUserAndInsert( httpRequest, profile, friend.getUser2() );
						}
					}
					else{
						if( !usersController.containsId( friend.getUser1() )){
							newUser = true;
							usersController.getUserAndInsert( httpRequest, profile, friend.getUser1() );
						}
					}
					
					if(instance.insertListener != null && !newUser){
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
			public void insertFriendCancelled() {
				// TODO Auto-generated method stub
				
			}
			
		});
		insertFriend.execute(context.getString(R.string.insertFriendURL));
		
	}
	
	public void acceptDeclineFriend(DBhttpRequest httpRequest, final Profile profile, final Friend friend, final AcceptDeclineFriend.Type type){
		AcceptDeclineFriend acceptDeclineFriend = new AcceptDeclineFriend(httpRequest, profile, friend, type);
		acceptDeclineFriend.setAcceptDeclineListener(new AcceptDeclineFriendListener(){

			@Override
			public void onPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete(String resultString) {
				Integer result;
				if(resultString.contains("success")){
					result = 1;
					if(type == AcceptDeclineFriend.Type.ACCEPT){
						instance.updateAccept(friend.getUser1(), friend.getUser2(), type.ordinal());
					}
					else if(type == AcceptDeclineFriend.Type.DECLINE){
						instance.deleteRow(friend.getUser1(), friend.getUser2());
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
		acceptDeclineFriend.execute( context.getString(R.string.acceptDeclineFriendURL) );
	}
}
