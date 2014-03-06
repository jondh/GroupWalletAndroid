package com.whereone.groupWallet.controllers;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupwalletcake.GetWallets;
import com.whereone.groupwalletcake.GetWallets.getWalletsListener;
import com.whereone.groupwalletcake.LogOutCurrent;
import com.whereone.groupwalletcake.R;

public class WalletsController extends SQLiteOpenHelper {

	private walletsControllerListener listener;
	private SQLiteDatabase databaseW;
	private SQLiteDatabase databaseR;
	private LogOutCurrent logOut;
	private Context context;
	
	public static final String TABLE_WALLETS = "wallets";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DATE = "date";
	

	private static final String DATABASE_NAME = "wallets.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
	      + TABLE_WALLETS + "(" + COLUMN_ID
	      + " integer primary key, " + COLUMN_NAME
	      + " text not null," + COLUMN_DATE
	      + " test not null);";

	public WalletsController(Context _context, LogOutCurrent _logOut) {
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
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLETS);
		    onCreate(database);
	}
	
	public void setWalletsControllerListener(walletsControllerListener _listener) {
        this.listener = _listener;
    }
	
	public interface walletsControllerListener{
		public void insertWalletsAsyncComplete();
	}
	
	protected void insertWallets(ArrayList<Wallet> wallets){
		for(Integer i = 0; i < wallets.size(); i++){
			Wallet curWallet = wallets.get(i);
			ContentValues values = new ContentValues();
			values.put("id", curWallet.getID());
			values.put("name", curWallet.getName());
			values.put("date", curWallet.getDate());
			try{
				databaseW.insertOrThrow(TABLE_WALLETS, null, values);
			}
			catch (SQLiteConstraintException e){
				Log.i("Wallet Constraint Exception", curWallet.getID() + " duplicate");
			}
		}
	}
	
	public void removeAll(){
		databaseW.delete(TABLE_WALLETS, null, null);
	}
	
	public ArrayList<String> getWalletNames(){
		String[] columns = {
				COLUMN_NAME
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLETS, columns, null, null, null, null, null);
		
		if(cursor.getCount() >0)
        {
			ArrayList<String> str = new ArrayList<String>();
           
            while (cursor.moveToNext())
            {
                 str.add( cursor.getString(cursor.getColumnIndex(COLUMN_NAME)) );
            }
            return str;
        }
        else
        {
            return null;
        }
	}
	
	public ArrayList<Integer> getWalletIds(){
		String[] columns = {
				COLUMN_ID
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLETS, columns, null, null, null, null, null);
		
		if(cursor.getCount() > 0)
        {
            ArrayList<Integer> integ = new ArrayList<Integer>();
            
            while (cursor.moveToNext())
            {
                 integ.add( cursor.getInt(cursor.getColumnIndex(COLUMN_ID)) );
             }
            return integ;
        }
        else
        {
            return null;
        }
	}
	
	public Integer getWalletIdFromName(String name){
		String[] columns = {
				COLUMN_ID
		};
		
		String whereClause = "name = ?";
		String[] whereArgs = new String[]{
				name
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLETS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
            Integer id = 0;
            
            while (cursor.moveToNext())
            {
                 id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
             }
            return id;
        }
        else
        {
            return 0;
        }
	}

	public void getWalletsAndInsert(Integer userID, final DBhttpRequest httpRequest,
			final String public_token, final String private_tokenH, final String _timeStamp){
		GetWallets getWallets = new GetWallets(httpRequest, userID, public_token, private_tokenH, _timeStamp);
		
	   	getWallets.setWalletsListener(new getWalletsListener(){

			@Override
			public void getWalletsPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getWalletsComplete(final ArrayList<Wallet> _wallets) {
				if(_wallets != null){
					insertWalletsAsync(_wallets);
				}
				else{
					logOut.logOut(context, httpRequest, context.getString(R.string.logOutURL), public_token, private_tokenH, _timeStamp);
				}
			}

			@Override
			public void getWalletsCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	getWallets.execute(context.getString(R.string.getWalletsURL));
	}
	
	protected void insertWalletsAsync(final ArrayList<Wallet> wallets){
		final WalletsController walletTable = this;
		class putWallets implements Runnable{
			@Override
			public void run(){
				walletTable.insertWallets(wallets);
				listener.insertWalletsAsyncComplete();
			}
		}
		
		Thread insertWallets = new Thread(new putWallets());
		insertWallets.start();
	}

}
