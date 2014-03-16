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

import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupwalletcake.GetWallets;
import com.whereone.groupwalletcake.GetWallets.getWalletsListener;
import com.whereone.groupwalletcake.R;

public class WalletsController extends SQLiteOpenHelper {

	private walletsControllerListener listener;

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
	      + " text not null);";

	public WalletsController(Context _context) {
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
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLETS);
		    onCreate(database);
	}
	
	public void setWalletsControllerListener(walletsControllerListener _listener) {
        this.listener = _listener;
    }
	
	public interface walletsControllerListener{
		public void insertWalletsAsyncComplete();
		// 1 == Add Success
		// 0 == Empty Result
		// -1 == Null Result -> due to bad access token(s)
		public void getWalletComplete(Integer result);
	}
	
	protected void insertWallets(ArrayList<Wallet> wallets){
		SQLiteDatabase databaseW = this.getWritableDatabase();
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
		databaseW.close();
	}
	
	public void removeAll(){
		SQLiteDatabase databaseW = this.getWritableDatabase();
		databaseW.delete(TABLE_WALLETS, null, null);
		databaseW.close();
	}
	
	public ArrayList<String> getWalletNames(){
		SQLiteDatabase databaseR = this.getReadableDatabase();
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
            databaseR.close();
            return str;
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}
	
	public ArrayList<Wallet> getWallets(){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID,
				COLUMN_NAME,
				COLUMN_DATE
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLETS, columns, null, null, null, null, null);
		
		if(cursor.getCount() >0)
        {
			ArrayList<Wallet> wallet = new ArrayList<Wallet>();
           
            while (cursor.moveToNext())
            {
                 wallet.add( new Wallet(
                		 cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                		 cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                		 cursor.getString(cursor.getColumnIndex(COLUMN_DATE)) 
                		 )
                 );
            }
            databaseR.close();
            return wallet;
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}
	
	public ArrayList<Integer> getWalletIds(){
		SQLiteDatabase databaseR = this.getReadableDatabase();
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
            databaseR.close();
            return integ;
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}
	
	public Integer getWalletIdFromName(String name){
		SQLiteDatabase databaseR = this.getReadableDatabase();
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
            databaseR.close();
            return id;
        }
        else
        {
        	databaseR.close();
            return 0;
        }
	}
	
	public String getWalletNameFromId(Integer wallet_id){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_NAME
		};
		
		String whereClause = "id = ?";
		String[] whereArgs = new String[]{
				wallet_id.toString()
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLETS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
            String name = "";
            
            while (cursor.moveToNext())
            {
                 name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
             }
            databaseR.close();
            return name;
        }
        else
        {
        	databaseR.close();
            return "";
        }
	}

	public void findWallets(Integer userID, final DBhttpRequest httpRequest,
			final String public_token, final String private_tokenH, final String _timeStamp){
		GetWallets getWallets = new GetWallets(httpRequest, this.getWalletIds(), userID, public_token, private_tokenH, _timeStamp);
		
	   	getWallets.setWalletsListener(new getWalletsListener(){

			@Override
			public void getWalletsPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getWalletsComplete(final ArrayList<Wallet> _wallets) {
				Integer result;
				if(_wallets != null){
					if(_wallets.get(0).getID() == -1 && _wallets.get(0).getName() == "-1" && _wallets.get(0).getDate() == "-1"){
						Log.i("WalletsController", "Empty Result");
						result = 0;
					}
					else{
						insertWalletsAsync(_wallets);
						result = 1;
					}
				}
				else{
					Log.i("WalletsController", "result is null -> possibly should log out");
					result = -1;
					//logOut.logOut(context, httpRequest, context.getString(R.string.logOutURL), public_token, private_tokenH, _timeStamp);
				}
				listener.getWalletComplete(result);
			}

			@Override
			public void getWalletsCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
	   		getWallets.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getString(R.string.getWalletsURL));
	   	}
	   	else {
	   		getWallets.execute(context.getString(R.string.getWalletsURL));
	   	}
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
