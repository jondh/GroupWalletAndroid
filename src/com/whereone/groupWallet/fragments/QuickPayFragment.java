package com.whereone.groupWallet.fragments;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.TransactionsController.transactionsControllerListener;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.models.User;
import com.whereone.groupwalletcake.LogOutCurrent;
import com.whereone.groupwalletcake.R;
import com.whereone.groupwalletcake.RecordConfirmDialog;
import com.whereone.groupwalletcake.RecordConfirmDialog.onSelect;

public class QuickPayFragment extends Fragment {

	ArrayList<String> wallets = new ArrayList<String>();
	ArrayList<String> users = new ArrayList<String>();
	ArrayList<Integer> user_id = new ArrayList<Integer>();
	AutoCompleteTextView userFind;
	AutoCompleteTextView walletFind;
	Integer selectedWallet = 0;
	Context context;
	
	private SelfListener selfListener;
	
	public void walletsUpdated(){
		if(selfListener != null){
			selfListener.walletsUpdated();
		}
	}
	public void walletRelationsUpdated(ArrayList<Integer> newRelations){
		if(selfListener != null){
			selfListener.walletRelationsUpdated(newRelations);
		}
	}
	public void userUpdated(Integer userID){
		if(selfListener != null){
			selfListener.userUpdated(userID);
		}
	}
	
	private void setSelfListener(SelfListener listener){
		selfListener = listener;
	}
	
	private interface SelfListener{
		public void walletsUpdated();
		public void walletRelationsUpdated(ArrayList<Integer> newRelations);
		public void userUpdated(Integer userID);
	}
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_quick_pay, null);
		final DBhttpRequest httpRequest = new DBhttpRequest();
		context = getActivity();
		setupUI(view.findViewById(R.id.fragment_quick_pay_layout), context);
		final ProgressDialog mPDialog = new ProgressDialog(context);
		
		final SharedPreferences profile = context.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		final LogOutCurrent logOut = new LogOutCurrent(profile, null, null, null, null);
		
		final WalletsController walletTable = new WalletsController(context);
		final WalletRelationsController walletRelationTable = new WalletRelationsController(context);
		final UsersController userTable = new UsersController(context);
		final TransactionsController transactionController = new TransactionsController(context, logOut);
		
		
		userFind = (AutoCompleteTextView) view.findViewById(R.id.userAuto);
		walletFind = (AutoCompleteTextView) view.findViewById(R.id.walletAuto);
		final RadioGroup whoPay = (RadioGroup) view.findViewById(R.id.whoPayQuickPay);
		final EditText amount = (EditText) view.findViewById(R.id.amountQuickPay);
		final EditText comments = (EditText) view.findViewById(R.id.commentsQuickPay);
		Button submit = (Button) view.findViewById(R.id.submitQuickPay);
		Button logoutButton = (Button) view.findViewById(R.id.logoutQuickPay);
		
		wallets = walletTable.getWalletNames();
		if(wallets != null){
			ArrayAdapter<String> adapterWallets = new ArrayAdapter<String>(context, R.layout.list_item, wallets);
		   	walletFind.setAdapter(adapterWallets);
		}
	   
	   	final ArrayAdapter<String> adapterUsers = new ArrayAdapter<String>(context, R.layout.list_item, users);
	    userFind.setAdapter(adapterUsers);
		
	    this.setSelfListener(new SelfListener(){

			@Override
			public void walletsUpdated() {
				wallets = walletTable.getWalletNames();
				if(wallets != null){
					getActivity().runOnUiThread(new Runnable(){

						@Override
						public void run() {
							ArrayAdapter<String> adapterWallets = new ArrayAdapter<String>(context, R.layout.list_item, wallets);
						   	walletFind.setAdapter(adapterWallets);
						}
						
					});
					
				}
			}

			@Override
			public void walletRelationsUpdated(ArrayList<Integer> newRelations) {
				for(int i = 0; i < newRelations.size(); i++){
					if(newRelations.get(i) == selectedWallet){
						final Integer userID = walletRelationTable.getUserFromId(newRelations.get(i));
						if(userTable.containsId(userID)){
							getActivity().runOnUiThread(new Runnable(){

								@Override
								public void run() {
									adapterUsers.add(userTable.getUserFromId(userID));
									adapterUsers.notifyDataSetChanged();
								}
								
							});
						}
					}
				}
			}

			@Override
			public void userUpdated(Integer userID) {
				if( walletRelationTable.containsUser(selectedWallet, userID) ){
					if( !users.contains(userID)){
						users.add(userTable.getUserFromId(userID));
						getActivity().runOnUiThread(new Runnable(){
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
		            	   
		            	   getActivity().runOnUiThread(new Runnable(){
		
			   					@Override
			   					public void run() {
			   						
			   		            	userFind.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.list_item, users));
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
						
						recordConfirm.show(getActivity().getFragmentManager(), "Confirm Record");
						
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
					logOut.logOut(getActivity(), httpRequest,
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
		
		transactionController.setTransactionsControllerListener(new transactionsControllerListener(){
	
			@Override
			public void insertComplete() {
				userFind.setText("");
				walletFind.setText("");
				whoPay.clearCheck();
				amount.setText("0.00");
				comments.setText("");
			}

			@Override
			public void getComplete(Integer result) {
				// TODO Auto-generated method stub
				
			}
		
		});
			
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	} 
	
	@Override
	public void onPause(){
		super.onPause();
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
	
	public static void hideSoftKeyboard(Context context2) {
	    InputMethodManager inputMethodManager = (InputMethodManager)  context2.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(((Activity) context2).getCurrentFocus().getWindowToken(), 0);
	}
	
	public void setupUI(View view, final Context context2) {

	    //Set up touch listener for non-text box views to hide keyboard.
	    if(!(view instanceof EditText)) {

	        view.setOnTouchListener(new OnTouchListener() {

	            public boolean onTouch(View v, MotionEvent event) {
	                hideSoftKeyboard(context2);
	                return false;
	            }

	        });
	    }

	    //If a layout container, iterate over children and seed recursion.
	    if (view instanceof ViewGroup) {

	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

	            View innerView = ((ViewGroup) view).getChildAt(i);

	            setupUI(innerView, context2);
	        }
	    }
	}
}
