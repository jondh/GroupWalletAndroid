package com.whereone.groupWallet.controllers;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.whereone.groupWallet.models.Record;
import com.whereone.groupwalletcake.GetRecords;
import com.whereone.groupwalletcake.GetRecords.getRecordsListener;
import com.whereone.groupwalletcake.InsertRecord;
import com.whereone.groupwalletcake.InsertRecord.insertRecordListener;
import com.whereone.groupwalletcake.LogOutCurrent;
import com.whereone.groupwalletcake.R;

public class TransactionsController extends SQLiteOpenHelper {
	private transactionsControllerListener listener;
	
	public static final String TABLE_TRANSACTIONS = "transactions";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_OWE = "oweUID";
	public static final String COLUMN_OWED = "owedUID";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_WALLET = "walletID";
	public static final String COLUMN_COMMENTS = "comments";
	public static final String COLUMN_DATETIME = "dateTime";
	
	private LogOutCurrent logOut;
	private Context context;

	private static final String DATABASE_NAME = "transactions.db";
	private static final int DATABASE_VERSION = 1;
	
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
	      + TABLE_TRANSACTIONS + "(" + COLUMN_ID
	      + " integer primary key, " + COLUMN_OWE
	      + " integer not null," + COLUMN_OWED
	      + " integer not null," + COLUMN_AMOUNT
	      + " real not null," + COLUMN_WALLET
	      + " integer not null," + COLUMN_COMMENTS
	      + " text not null," + COLUMN_DATETIME
	      + " text not null);";
	
	public TransactionsController(Context _context, LogOutCurrent _logOut){
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);
		
		logOut = _logOut;
		context = _context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(UsersController.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
		onCreate(database);
	}
	
	public void setTransactionsControllerListener(transactionsControllerListener _listener) {
        this.listener = _listener;
    }
	
	public interface transactionsControllerListener{
		public void insertComplete();
		// 1 == Add Success
		// 0 == Empty Result
		// -1 == Null Result -> due to bad access token(s)
		public void getComplete(Integer result);
	}
	
	public void removeAll(){
		
	}
	
	protected void insertRecords(ArrayList<Record> records){
		SQLiteDatabase databaseW = this.getWritableDatabase();
		for(Integer i = 0; i < records.size(); i++){
			Record curRecord = records.get(i);
			ContentValues values = new ContentValues();
			values.put("id", curRecord.getID());
			values.put("oweUID", curRecord.getOweId());
			values.put("owedUID", curRecord.getOwedId());
			values.put("amount", curRecord.getAmount());
			values.put("walletID", curRecord.getWalletId());
			values.put("comments", curRecord.getComments());
			values.put("dateTime", curRecord.getDateTime());
			try{
				databaseW.insertOrThrow(TABLE_TRANSACTIONS, null, values);
			}
			catch (SQLiteConstraintException e){
				Log.i("Wallet Constraint Exception", curRecord.getID() + " duplicate");
			}
		}
		databaseW.close();
	}
	
	public ArrayList<Integer> getRecordIds(){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		Cursor cursor = databaseR.query(TABLE_TRANSACTIONS, columns, null, null, null, null, null);
		
		if(cursor.getCount() >0)
        {
			ArrayList<Integer> records = new ArrayList<Integer>();
           
            while (cursor.moveToNext())
            {
                records.add( cursor.getInt(cursor.getColumnIndex(COLUMN_ID) ));
            }
            databaseR.close();
            return records;
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}
	
	public ArrayList<Record> getRecords(){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID,
				COLUMN_OWE,
				COLUMN_OWED,
				COLUMN_AMOUNT,
				COLUMN_WALLET,
				COLUMN_COMMENTS,
				COLUMN_DATETIME
		};
		
		Cursor cursor = databaseR.query(TABLE_TRANSACTIONS, columns, null, null, null, null, null);
		
		if(cursor.getCount() >0)
        {
			ArrayList<Record> records = new ArrayList<Record>();
           
            while (cursor.moveToNext())
            {
            	Record temp = new Record();
                temp.setID( cursor.getInt(cursor.getColumnIndex(COLUMN_ID) ));
                temp.setOweId( cursor.getInt(cursor.getColumnIndex(COLUMN_OWE) ));
                temp.setOwedId( cursor.getInt(cursor.getColumnIndex(COLUMN_OWED) ));
                temp.setAmount( cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT) ));
                temp.setWalletId( cursor.getInt(cursor.getColumnIndex(COLUMN_WALLET) ));
                temp.setComments( cursor.getString(cursor.getColumnIndex(COLUMN_COMMENTS) ));
                temp.setDateTime( cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME) ));
                records.add(temp);
            }
            databaseR.close();
            return records;
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}
	
	public Double getOweUser(Integer oweId, Integer owedId){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_AMOUNT
		};
		
		String whereClause = "oweUID = ? AND owedUID = ?";
		String[] whereArgs = new String[]{
				oweId.toString(),
				owedId.toString()
		};
			
		Cursor cursor = databaseR.query(TABLE_TRANSACTIONS, columns, whereClause, whereArgs, null, null, null);
			
		if(cursor.getCount() >0)
        {
			Double amount = 0.0;
			while( cursor.moveToNext() ){
        	   amount += cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
			}
			databaseR.close();
			return amount;
        }
		databaseR.close();
		return 0.0;
	}
	
	public Double getOweWallet(Integer userId, Integer walletId){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_AMOUNT
		};
		
		String whereClause = "oweUID = ? AND walletID = ?";
		String[] whereArgs = new String[]{
				userId.toString(),
				walletId.toString()
		};
			
		Cursor cursor = databaseR.query(TABLE_TRANSACTIONS, columns, whereClause, whereArgs, null, null, null);
			
		if(cursor.getCount() >0)
        {
			Double amount = 0.0;
			while( cursor.moveToNext() ){
        	   amount += cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
			}
			databaseR.close();
			return amount;
        }
		databaseR.close();
		return 0.0;
	}
	
	public Double getOwedWallet(Integer userId, Integer walletId){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_AMOUNT
		};
		
		String whereClause = "owedUID = ? AND walletID = ?";
		String[] whereArgs = new String[]{
				userId.toString(),
				walletId.toString()
		};
			
		Cursor cursor = databaseR.query(TABLE_TRANSACTIONS, columns, whereClause, whereArgs, null, null, null);
			
		if(cursor.getCount() >0)
        {
			Double amount = 0.0;
			while( cursor.moveToNext() ){
        	   amount += cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
			}
			databaseR.close();
			return amount;
        }
		databaseR.close();
		return 0.0;
	}
	
	public Double getOweUserWallet(Integer oweId, Integer owedId, Integer walletId){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_AMOUNT
		};
		
		String whereClause = "oweUID = ? AND owedUID = ? AND walletID = ?";
		String[] whereArgs = new String[]{
				oweId.toString(),
				owedId.toString(),
				walletId.toString()
		};
			
		Cursor cursor = databaseR.query(TABLE_TRANSACTIONS, columns, whereClause, whereArgs, null, null, null);
			
		if(cursor.getCount() >0)
        {
			Double amount = 0.0;
			while( cursor.moveToNext() ){
        	   amount += cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
			}
			databaseR.close();
			return amount;
        }
		databaseR.close();
		return 0.0;
	}
	
	public Double getTotalOwe(Integer oweId){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_AMOUNT
		};
		
		String whereClause = "oweUID = ?";
		String[] whereArgs = new String[]{
				oweId.toString()
		};
			
		Cursor cursor = databaseR.query(TABLE_TRANSACTIONS, columns, whereClause, whereArgs, null, null, null);
			
		if(cursor.getCount() >0)
        {
			Double amount = 0.0;
			while( cursor.moveToNext() ){
        	   amount += cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
			}
			databaseR.close();
			return amount;
        }
		databaseR.close();
		return 0.0;
	}
	
	public Double getTotalOwed(Integer owedId){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_AMOUNT
		};
		
		String whereClause = "owedUID = ?";
		String[] whereArgs = new String[]{
				owedId.toString()
		};
			
		Cursor cursor = databaseR.query(TABLE_TRANSACTIONS, columns, whereClause, whereArgs, null, null, null);
			
		if(cursor.getCount() >0)
        {
			Double amount = 0.0;
			while( cursor.moveToNext() ){
        	   amount += cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
			}
			databaseR.close();
			return amount;
        }
		databaseR.close();
		return 0.0;
	}
	
	public void insert(Integer _userID, Integer _otherUID, Double _amount, Integer _walletID, String _Comments, Boolean _owe, final DBhttpRequest _httpRequest, final ProgressDialog mPDialog,
			final String public_token, final String private_tokenH, final String _timeStamp){
		
		InsertRecord insertRecord = new InsertRecord(_userID, _otherUID, _amount, _walletID, _Comments,
				_owe, _httpRequest, public_token, private_tokenH, _timeStamp);
		
		mPDialog.setMessage("Loading...");
	    mPDialog.show(); 
		
	   	insertRecord.setInsertRecordListener(new insertRecordListener(){

			@Override
			public void insertRecordPreExecute() {
				
			}

			@Override
			public void insertRecordComplete(Boolean result) {
				if(result){
					System.out.println("SUCCESSFULLY inserted record");
					mPDialog.hide(); 
					listener.insertComplete();
				}
				else{
					System.out.println("Insert record FAILED");
					mPDialog.hide(); 
					logOut.logOut(context, _httpRequest, public_token, private_tokenH, _timeStamp);
				}
			}

			@Override
			public void insertRecordCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
	   		insertRecord.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getString(R.string.insertRecordURL));
	   	}
	   	else {
	   		insertRecord.execute(context.getString(R.string.insertRecordURL));
	   	}
	}
	
	public void findTransactions(ArrayList<Integer> wallet_ids, final DBhttpRequest httpRequest,
			final String public_token, final String private_tokenH, final String _timeStamp){
		if(wallet_ids == null) return;
   		GetRecords getRecords = new GetRecords(httpRequest, wallet_ids, this.getRecordIds(), public_token, private_tokenH, _timeStamp);
	    final TransactionsController transactionController = this;
   		
   		getRecords.setRecordsListener(new getRecordsListener(){
	   		
			@Override
			public void getRecordsPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getRecordsComplete(ArrayList<Record> records) {
				Integer result;
				if(records != null){
					if(records.get(0).getID() == -1 && records.get(0).getOwedId() == -1 && records.get(0).getOwedId() == -1){
						Log.i("TransactionsController", "Empty Result");
						result = 0;
					}
					else{
						transactionController.insertRecords(records);
						result = 1;
					}
				}
				else{
					Log.i("TransactionsController", "result is null -> possibly should log out");
					result = -1;
				}
				listener.getComplete(result);
			}

			@Override
			public void getRecordsCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	getRecords.execute(context.getString(R.string.getRecordsURL));
	   	
	}
}
