package com.whereone.groupwalletcake;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.TransactionsController.insertCompleteListener;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.UsersController.usersControllerListener;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.controllers.WalletsController.walletsControllerListener;
import com.whereone.groupWallet.models.User;
import com.whereone.groupwalletcake.RecordConfirmDialog.onSelect;

public class QuickPayActivity extends FragmentActivity {

	ArrayList<String> wallets = new ArrayList<String>();
	ArrayList<String> users = new ArrayList<String>();
	ArrayList<Integer> user_id = new ArrayList<Integer>();
	AutoCompleteTextView userFind;
	AutoCompleteTextView walletFind;
	Integer selectedWallet = 0;
	
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_quick_pay);
		
		final DBhttpRequest httpRequest = new DBhttpRequest();
		final Context context = this.getApplicationContext();
		final ProgressDialog mPDialog = new ProgressDialog(this);
		
		final SharedPreferences profile = context.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		final LogOutCurrent logOut = new LogOutCurrent(profile, null, null, null, null);
		
		final WalletsController walletTable = new WalletsController(context, logOut);
		final WalletRelationsController walletRelationTable = new WalletRelationsController(context, logOut);
		final UsersController userTable = new UsersController(context, logOut);
		final TransactionsController transactionController = new TransactionsController(context, logOut);
		
		/*
		 *  Check if user is logged in
		 */
		SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
		String currentDate = s.format(new Date());
		CheckLogIn checkLogin = new CheckLogIn(profile, transactionController, userTable, walletRelationTable, walletTable);
		try {
			checkLogin.checkUser(context, httpRequest, profile.getString("publicToken", ""), computeHash((profile.getString("privateToken", "") + currentDate)), currentDate);
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		logOut.setControllers(transactionController, userTable, walletRelationTable, walletTable);
		
		userFind = (AutoCompleteTextView) findViewById(R.id.userAuto);
		walletFind = (AutoCompleteTextView) findViewById(R.id.walletAuto);
		final RadioGroup whoPay = (RadioGroup) findViewById(R.id.whoPayQuickPay);
		final EditText amount = (EditText) findViewById(R.id.amountQuickPay);
		final EditText comments = (EditText) findViewById(R.id.commentsQuickPay);
		Button submit = (Button) findViewById(R.id.submitQuickPay);
		Button logoutButton = (Button) findViewById(R.id.logoutQuickPay);
		
		if(profile.getInt("id", 0) == 0){
			walletTable.removeAll();
			walletRelationTable.removeAll();
			userTable.removeAll();
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
		}
		
		
		final ArrayAdapter<String> adapterWallets = new ArrayAdapter<String>(context, R.layout.list_item, wallets);
	   	walletFind.setAdapter(adapterWallets);
	   	
	   	final ArrayAdapter<String> adapterUsers = new ArrayAdapter<String>(context, R.layout.list_item, users);
	    userFind.setAdapter(adapterUsers);
		
	    SimpleDateFormat s1 = new SimpleDateFormat("ddMMyyyyhhmmss");
		String currentDate1 = s1.format(new Date());
		try {
			walletTable.getWalletsAndInsert(profile.getInt("id", 0), httpRequest,
					profile.getString("publicToken", ""), computeHash((profile.getString("privateToken", "") + currentDate1)), currentDate1);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   	
		walletTable.setWalletsControllerListener(new walletsControllerListener(){

			@Override
			public void insertWalletsAsyncComplete() {
				SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
				String currentDate = s.format(new Date());
				try {
					walletRelationTable.getWalletRelationsAndUsersAndInsert(walletTable.getWalletIds(), profile.getInt("id", 0), 
							getString(R.string.getWalletRelationsURL), userTable, getString(R.string.getUserURL), httpRequest,
							profile.getString("publicToken", ""), computeHash((profile.getString("privateToken", "") + currentDate)), currentDate);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ArrayList<String> tempWallets = walletTable.getWalletNames();
				if(tempWallets != null){
					wallets.addAll ( tempWallets );
				}
				
				QuickPayActivity.this.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						adapterWallets.notifyDataSetChanged();
						walletFind.showDropDown();
					}
					
				});
				
			}
			
		});
		
		/*
		 *  A listener inside of the UsersController
		 */
		userTable.setUsersControllerListener(new usersControllerListener(){
			/*
			 * Responds whenever a new user is inserted into the local database
			 * 	Currently -> a user is requested from the master database from the user id, after it is
			 * 				 retrieved and put into the local database, this function is fired
			 * 
			 * (non-Javadoc)
			 * @see com.whereone.groupWallet.controllers.UsersController.usersControllerListener#insertUserAsyncComplete()
			 */
			@Override
			public void insertUserAsyncComplete() {
				/*
				 *  Gets entire list of usernames, checks if any belong in selected wallet, add them if they do, show updated dropdown if focused
				 *  TODO: get ONLY new usernames
				 */
				ArrayList<String> temp = userTable.getUserNames();
				for(int i = 0; i < temp.size(); i++){
					if( walletRelationTable.containsUser(selectedWallet, userTable.getIdFromUserName(temp.get(i))) ){
						if( !users.contains(temp.get(i))){
							users.add(temp.get(i));
							QuickPayActivity.this.runOnUiThread(new Runnable(){
								@Override
								public void run() {
									userFind.setAdapter(new ArrayAdapter<String>(context, R.layout.list_item, users));
									if(userFind.hasFocus()){
										userFind.showDropDown();
									}
								}
							});
							
						}
					}
				}
				QuickPayActivity.this.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						adapterUsers.notifyDataSetChanged();
					}
					
				});
			}
			
		});
	   	
		
	    userFind.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(final View arg0) {
            	userFind.showDropDown();
	        }
	    });
	   	userFind.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
            	
            	userFind.showDropDown();
                return false;
            }
        });
	   	
	   	
	   	
	   	userFind.setOnFocusChangeListener(new OnFocusChangeListener() {          

	        public void onFocusChange(View v, boolean hasFocus) {
	            if(!hasFocus && users != null){
	               if( !users.contains( userFind.getText().toString() )){
	            	   userFind.setText("");
	               }
	            }
	        }
	    });
	   	
	   	walletFind.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(final View arg0) {
	        	if(wallets != null){
	        		walletFind.showDropDown();
	        	}
	        }
	    });
		walletFind.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
            	if(wallets != null){
            		walletFind.showDropDown();
            	}
                return false;
            }
        });
		walletFind.setOnFocusChangeListener(new OnFocusChangeListener() {          

	        public void onFocusChange(View v, boolean hasFocus) {
	            if(!hasFocus && wallets != null){
	               if( !wallets.contains( walletFind.getText().toString() )){
	            	   walletFind.setText("");
	               }
	               else{
	            	   userFind.setAdapter(null);
	            	   selectedWallet = walletTable.getWalletIdFromName( walletFind.getText().toString() );
	            	   ArrayList<Integer> tempUserIds = walletRelationTable.getUsersForWallet(selectedWallet);
	            	   if( tempUserIds != null ){
		            	   final ArrayList<String> temp = userTable.getUsersFromIds( tempUserIds );
		            	   users.clear(); users.addAll(temp);
		            	   
		            	   QuickPayActivity.this.runOnUiThread(new Runnable(){
		
			   					@Override
			   					public void run() {
			   						
			   		            	userFind.setAdapter(new ArrayAdapter<String>(context, R.layout.list_item, users));
			   		            	if(userFind.hasFocus()){
			   		            		userFind.showDropDown();
			   		            	}
			   		            	if(!walletRelationTable.containsUser(selectedWallet, userTable.getIdFromUserName( userFind.getText().toString() ))){
			   		            		userFind.setText("");
			   		            	}
			   					}
			   					
			   				});
	            	   }
	               }
	            }
	        }
	    });
		
		amount.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			/*
			 *  Only input in currency form. Code from stackOverflow:
			 *  	http://stackoverflow.com/questions/5107901/better-way-to-format-currency-input-edittext
			 */
			
			private String current = "";
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if(!s.toString().equals(current)){
				       amount.removeTextChangedListener(this);

				       String cleanString = s.toString().replaceAll("[$,.]", "");

				       double parsed = Double.parseDouble(cleanString);
				       String formated = NumberFormat.getCurrencyInstance().format((parsed/100));

				       current = formated;
				       amount.setText(formated);
				       amount.setSelection(formated.length());

				       amount.addTextChangedListener(this);
				    }
				
			}
			
		});
		
		submit.setOnClickListener( new View.OnClickListener(){
			
			public void submitForm(final Boolean owe){
				String amountCash = amount.getText().toString();
				String amountNoCash = amountCash.replace("$", "").replaceAll(",", "");
				System.out.println(amountNoCash);
				if(amountNoCash != ""){
					final Double amountD = Double.parseDouble(amountNoCash);
					final String commentsText = comments.getText().toString();
					final User user = userTable.getUserFromUserName( userFind.getText().toString() );
					if(user != null){
						RecordConfirmDialog recordConfirm = RecordConfirmDialog.newInstance(
								user.getFirstName(), user.getLastName(), user.getUserName(), amountCash, owe);
						
						recordConfirm.show(getSupportFragmentManager(), "Confirm Record");
						
						recordConfirm.setSelectListener(new onSelect(){

							@Override
							public void comfirmPressed() {
								SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
								String currentDate = s.format(new Date());
								try {
									transactionController.insert((Integer)profile.getInt("id", 0), user.getUserID(), amountD, selectedWallet, commentsText, owe, httpRequest, mPDialog,
											profile.getString("publicToken", ""), computeHash((profile.getString("privateToken", "") + currentDate)), currentDate);
								} catch (NoSuchAlgorithmException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void cancelPressed() {
								System.out.println("Cancel");
							}
							
						});
					}
				}
			}
			
			@Override
			public void onClick(View v) {
				if(whoPay.getCheckedRadioButtonId() == R.id.owedQuickPay){
					submitForm(false);
				}
				else if(whoPay.getCheckedRadioButtonId() == R.id.oweQuickPay){
					submitForm(true);
				}
			}
		});
		
		logoutButton.setOnClickListener( new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
				String currentDate = s.format(new Date());
				try {
					logOut.logOut(getApplicationContext(), httpRequest, getString(R.string.logOutURL),
							profile.getString("publicToken", ""), computeHash((profile.getString("privateToken", "") + currentDate)), currentDate);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		transactionController.setInsertCompleteListener(new insertCompleteListener(){

			@Override
			public void insertComplete() {
				userFind.setText("");
				walletFind.setText("");
				whoPay.clearCheck();
				amount.setText("0.00");
				comments.setText("");
			}
			
		});
		
	} 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.quick_pay, menu);
		return true;
	}
	
	public String computeHash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException{
	    MessageDigest digest = MessageDigest.getInstance("SHA-256");
	    digest.reset();

	    byte[] byteData = digest.digest(input.getBytes("UTF-8"));
	    StringBuffer sb = new StringBuffer();

	    for (int i = 0; i < byteData.length; i++){
	      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
}
