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

import com.whereone.groupWallet.GetWallets;
import com.whereone.groupWallet.GetWallets.getWalletsListener;
import com.whereone.groupWallet.InsertWallet;
import com.whereone.groupWallet.InsertWallet.InsertWalletListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupWallet.models.WalletRelation;

public class WalletsController extends SQLiteOpenHelper {

	private walletsControllerListener listener;
	private WalletInviteListener inviteListener;
	private static WalletsController instance;

	private Context context;
	
	public static final String TABLE_WALLETS = "wallets";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_USER_ID = "user_id";
	

	private static final String DATABASE_NAME = "wallets.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
	      + TABLE_WALLETS + "(" + COLUMN_ID
	      + " integer primary key, " + COLUMN_NAME
	      + " text not null," + COLUMN_DATE
	      + " text not null," + COLUMN_USER_ID 
	      + " integer not null);";

	private WalletsController(Context _context) {
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);

	}
	
	public static void init(Context _context){
		if(instance == null){
			instance = new WalletsController(_context);
			instance.context = _context;
		}
	}
	
	public static WalletsController getInstance(){
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
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLETS);
		    onCreate(database);
	}
	
	public void setWalletsControllerListener(walletsControllerListener _listener) {
		instance.listener = _listener;
    }
	
	public interface walletsControllerListener{
		// 1 == Add Success
		// 0 == Empty Result
		// -1 == Null Result -> due to bad access token(s)
		public void getWalletComplete(Integer result);
		
	}
	
	public void setWalletInviteListener(WalletInviteListener listener){
		instance.inviteListener = listener;
	}
	
	public interface WalletInviteListener{
		public void getWalletInvitesComplete(Integer result);
	}
	
	protected void insertWallets(ArrayList<Wallet> wallets){
		SQLiteDatabase databaseW = this.getWritableDatabase();
		for(Integer i = 0; i < wallets.size(); i++){
			Wallet curWallet = wallets.get(i);
			ContentValues values = new ContentValues();
			values.put("id", curWallet.getID());
			values.put("name", curWallet.getName());
			values.put("date", curWallet.getDate());
			values.put("user_id", curWallet.getUserID());
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
				COLUMN_DATE,
				COLUMN_USER_ID
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
                		 cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                		 cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID))
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
	
	public Wallet getWalletFromId(Integer wallet_id){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID,
				COLUMN_NAME,
				COLUMN_DATE,
				COLUMN_USER_ID
		};
		
		String whereClause = "id = ?";
		String[] whereArgs = new String[]{
				wallet_id.toString()
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLETS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
            
            if (cursor.moveToNext())
            {
            	databaseR.close();
            	return new Wallet(
               		 cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
               		 cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
               		 cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
               		 cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID))
               		 );
             }
            databaseR.close();
            return null;
        }
        else
        {
        	databaseR.close();
            return null;
        }
	}

	public ArrayList<Wallet> getWalletsUserID(Integer userID, WalletRelationsController walletRelationsController, Integer accept){
	
		SQLiteDatabase databaseR = this.getReadableDatabase();
		
		ArrayList<Integer> walletIds = walletRelationsController.getWalletsForUser(userID, accept);
		ArrayList<Wallet> wallets = new ArrayList<Wallet>();
		
		if(walletIds != null){
			for(int i = 0; i < walletIds.size(); i++){
			
				String[] columns = {
						COLUMN_ID,
						COLUMN_NAME,
						COLUMN_DATE,
						COLUMN_USER_ID
				};
				
				String whereClause = "id = ?";
				String[] whereArgs = new String[]{
						walletIds.get(i).toString()
				};
				
				Cursor cursor = databaseR.query(TABLE_WALLETS, columns, whereClause, whereArgs, null, null, null);
				
				if(cursor.getCount() >0)
		        {
		            
		            if (cursor.moveToNext())
		            {
		            	wallets.add( new Wallet(
		               		 cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
		               		 cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
		               		 cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
		               		 cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID))
		               		 ) );
		             }
		            
		        }
			}
		}
		databaseR.close();
        return wallets;
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
	
	public Boolean containsWalletName(String name){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_NAME
		};
		
		String whereClause = "name LIKE ?";
		String[] whereArgs = new String[]{
				name
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLETS, columns, whereClause, whereArgs, null, null, null);
		
		if(cursor.getCount() >0)
        {
            
            
            while (cursor.moveToNext())
            {
                 System.out.println( cursor.getString(cursor.getColumnIndex(COLUMN_NAME)) );
             }
            databaseR.close();
            return true;
        }
        else
        {
        	databaseR.close();
            return false;
        }
	}
	
	public Integer getNumberWallets(){
		SQLiteDatabase databaseR = this.getReadableDatabase();
		String[] columns = {
				COLUMN_ID
		};
		
		Cursor cursor = databaseR.query(TABLE_WALLETS, columns, null, null, null, null, null);
		
		if(cursor.getCount() > 0)
        {
            Integer integ = 0;
            
            while (cursor.moveToNext())
            {
                 integ++;
             }
            databaseR.close();
            return integ;
        }
        else
        {
        	databaseR.close();
            return 0;
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

	public void findWallets(DBhttpRequest httpRequest, final WalletRelationsController walletRelationsController, Profile profile, Integer userID){
		GetWallets getWallets = new GetWallets(httpRequest, profile, this.getWalletIds(), userID, 1);
		
	   	getWallets.setWalletsListener(new getWalletsListener(){

			@Override
			public void getWalletsPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getWalletsComplete(final ArrayList<Wallet> _wallets, ArrayList<WalletRelation> wrs, String resultString) {
				Integer result;
				if(_wallets != null || wrs != null){
					if(_wallets != null){
						instance.insertWallets(_wallets);
					}
					if(wrs != null){
						walletRelationsController.insertWalletRelations(wrs);
					}
					instance.listener.getWalletComplete(1);
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
					if(instance.listener != null){
						instance.listener.getWalletComplete(result);
					}
				}
				
			}

			@Override
			public void getWalletsCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
	   		getWallets.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instance.context.getString(R.string.getWalletsURL));
	   	}
	   	else {
	   		getWallets.execute(instance.context.getString(R.string.getWalletsURL));
	   	}
	}
	
	public void findWalletInvites(DBhttpRequest httpRequest, final WalletRelationsController walletRelationsController, Profile profile, Integer userID){
		GetWallets getWallets = new GetWallets(httpRequest, profile, this.getWalletIds(), userID, 0);
		
	   	getWallets.setWalletsListener(new getWalletsListener(){

			@Override
			public void getWalletsPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getWalletsComplete(final ArrayList<Wallet> _wallets, ArrayList<WalletRelation> wrs, String resultString) {
				Integer result;
				if(_wallets != null || wrs != null){
					if(_wallets != null){
						instance.insertWallets(_wallets);
					
						instance.inviteListener.getWalletInvitesComplete(1);
						
					}
					if(wrs != null){
						walletRelationsController.insertWalletRelations(wrs);
					}
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
					if(instance.listener != null){
						instance.inviteListener.getWalletInvitesComplete(result);
					}
				}
				
			}

			@Override
			public void getWalletsCancelled() {
				// TODO Auto-generated method stub
				
			}
	   		
	   	});
	   	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
	   		getWallets.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instance.context.getString(R.string.getWalletsURL));
	   	}
	   	else {
	   		getWallets.execute(instance.context.getString(R.string.getWalletsURL));
	   	}
	}
	
	public void insertPutWallet(DBhttpRequest httpRequest, Profile profile, String name, final WalletRelationsController walletRelationsController){
		InsertWallet insertWallet = new InsertWallet(httpRequest, profile, name);
		insertWallet.setInsertWalletListener(new InsertWalletListener(){

			@Override
			public void insertWalletPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertWalletComplete(Wallet wallet, WalletRelation walletR, String resultString) {
				Integer result;
				if(wallet != null && walletR != null){
					ArrayList<Wallet> walletTemp = new ArrayList<Wallet>();
					walletTemp.add(wallet);
					
					instance.insertWallets(walletTemp);
					
					ArrayList<WalletRelation> wrTemp = new ArrayList<WalletRelation>();
					wrTemp.add(walletR);
					walletRelationsController.insertWalletRelations(wrTemp);
					result = 1;
				}
				else{
					if(resultString.contains("timeout")){
						result = -2;
					}
					else if(resultString.contains("unknownHost")){
						result = -3;
					}
					else{
						result = -1;
					}
				}
				instance.listener.getWalletComplete(result);
			}

			@Override
			public void insertWalletCancelled() {
				// TODO Auto-generated method stub
				
			}
			
		});
		insertWallet.execute(context.getString(R.string.insertWalletURL));
	}

}
