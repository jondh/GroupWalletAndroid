package com.whereone.groupWallet.fragments;

import java.text.NumberFormat;
import java.util.ArrayList;

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
import android.widget.TextView;

import com.whereone.groupWallet.R;
import com.whereone.groupWallet.RecordConfirmDialog;
import com.whereone.groupWallet.RecordConfirmDialog.onSelect;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.FriendsController;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.TransactionsController.TransactionInsertListener;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.models.Friend;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.User;
import com.whereone.groupWallet.models.WalletRelation;

public class QuickPayFragment extends Fragment {

	ArrayList<String> wallets = new ArrayList<String>();
	ArrayList<String> users = new ArrayList<String>();
	ArrayList<Integer> user_id = new ArrayList<Integer>();
	AutoCompleteTextView userFind;
	AutoCompleteTextView walletFind;
	Integer selectedWallet = 0;
	Context context;
	
	private SelfListener selfListener;
	private QuickPayListener listener;
	
	public void walletsUpdated(){
		if(selfListener != null){
			selfListener.walletsUpdated();
		}
	}
	public void walletRelationsUpdated(ArrayList<WalletRelation> newRelations){
		if(selfListener != null){
			selfListener.walletRelationsUpdated(newRelations);
		}
	}
	public void userUpdated(Integer userID){
		if(selfListener != null){
			selfListener.userUpdated(userID);
		}
	}
	public void friendsUpdated(ArrayList<Friend> newFriends){
		if(selfListener != null){
			selfListener.friendsUpdated(newFriends);
		}
	}
	
	private void setSelfListener(SelfListener listener){
		selfListener = listener;
	}
	
	private interface SelfListener{
		public void walletsUpdated();
		public void walletRelationsUpdated(ArrayList<WalletRelation> newRelations);
		public void friendsUpdated(ArrayList<Friend> newFriends);
		public void userUpdated(Integer userID);
	}
	
	public void setQuickPayListener(QuickPayListener listener){
		this.listener = listener;
	}
	
	public interface QuickPayListener{
		public void insertedRecord(Integer result);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_quick_pay, null);
		
		context = getActivity();
		setupUI(view.findViewById(R.id.fragment_quick_pay_layout), context);
		final ProgressDialog mPDialog = new ProgressDialog(context);
		
		final SharedPreferences profile = context.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		final TransactionsController transactionsController = TransactionsController.getInstance();
		final WalletsController walletsController = WalletsController.getInstance();
		final UsersController usersController = UsersController.getInstance();
		final WalletRelationsController walletRelationsController = WalletRelationsController.getInstance();
		final FriendsController friendsController = FriendsController.getInstance();
		
		final DBhttpRequest httpRequest = DBhttpRequest.getInstance();
		
		final Profile profileL = Profile.getInstance();
		
		userFind = (AutoCompleteTextView) view.findViewById(R.id.userAuto);
		walletFind = (AutoCompleteTextView) view.findViewById(R.id.walletAuto);
		final RadioGroup whoPay = (RadioGroup) view.findViewById(R.id.whoPayQuickPay);
		final EditText amount = (EditText) view.findViewById(R.id.amountQuickPay);
		final EditText comments = (EditText) view.findViewById(R.id.commentsQuickPay);
		final TextView errorMessage = (TextView) view.findViewById(R.id.quickPay_error);
		errorMessage.setText("");
		Button submit = (Button) view.findViewById(R.id.submitQuickPay);
		
		wallets = walletsController.getWalletNames();
		if(wallets != null){
			ArrayAdapter<String> adapterWallets = new ArrayAdapter<String>(context, R.layout.list_item, wallets);
		   	walletFind.setAdapter(adapterWallets);
		}
	   
	   	final ArrayAdapter<String> adapterUsers = new ArrayAdapter<String>(context, R.layout.list_item, users);
	    userFind.setAdapter(adapterUsers);
		
	    this.setSelfListener(new SelfListener(){

			@Override
			public void walletsUpdated() {
				wallets = walletsController.getWalletNames();
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
			public void walletRelationsUpdated(ArrayList<WalletRelation> newRelations) {
				for(int i = 0; i < newRelations.size(); i++){
					if(newRelations.get(i).getWalletID() == selectedWallet){
						final Integer userID = walletRelationsController.getUserFromId(newRelations.get(i).getID());
						if(usersController.containsId(userID)){
							getActivity().runOnUiThread(new Runnable(){

								@Override
								public void run() {
									adapterUsers.add(usersController.getUserNameFromId(userID));
									adapterUsers.notifyDataSetChanged();
								}
								
							});
						}
					}
				}
			}

			@Override
			public void userUpdated(Integer userID) {
				if( walletRelationsController.containsUser(selectedWallet, userID) || friendsController.containsUser(userID, 1) ){
					if( !users.contains(userID)){
						users.add(usersController.getUserNameFromId(userID));
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

			@Override
			public void friendsUpdated(ArrayList<Friend> newFriends) {
				if(selectedWallet == 0){
					for(int i = 0; i < newFriends.size(); i++){
						Integer tuserID;
						if(newFriends.get(i).getUser1() == profileL.getUserID()){
							tuserID = newFriends.get(i).getUser2();
						}
						else{
							tuserID = newFriends.get(i).getUser1();
						}
						final Integer userID = tuserID;
						if(usersController.containsId(userID)){
							getActivity().runOnUiThread(new Runnable(){

								@Override
								public void run() {
									adapterUsers.add(usersController.getUserNameFromId(userID));
									adapterUsers.notifyDataSetChanged();
								}
								
							});
						}
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
	            	   selectedWallet = walletsController.getWalletIdFromName( walletFind.getText().toString() );
	            	   ArrayList<Integer> tempUserIds;
	            	   if(selectedWallet == 0){
	            		   tempUserIds = friendsController.getUserIds(profileL.getUserID(), 1);
	            	   }
	            	   else{
	            		   tempUserIds = walletRelationsController.getUsersForWallet(selectedWallet, profileL.getUserID(), 1);
	            	   }
	            	   if( tempUserIds != null ){
		            	   final ArrayList<String> temp = usersController.getUsersFromIds( tempUserIds );
		            	   users.clear(); users.addAll(temp);
		            	   
		            	   getActivity().runOnUiThread(new Runnable(){
		
			   					@Override
			   					public void run() {
			   						
			   		            	userFind.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.list_item, users));
			   		            	if(userFind.hasFocus()){
			   		            		userFind.showDropDown();
			   		            	}
			   		            	if(selectedWallet == 0){
			   		            		if(!friendsController.containsUser(usersController.getIdFromUserName( userFind.getText().toString() ), 1)){
			   		            			userFind.setText("");
			   		            		}
			   		            	}
			   		            	else if(!walletRelationsController.containsUser(selectedWallet, usersController.getIdFromUserName( userFind.getText().toString() ))){
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
				       if(cleanString != ""){
					       double parsed = Double.parseDouble(cleanString);
					       String formated = NumberFormat.getCurrencyInstance().format((parsed/100));
	
					       current = formated;
					       amount.setText(formated);
					       amount.setSelection(formated.length());
	
					       amount.addTextChangedListener(this);
				       }
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
					final User user = usersController.getUserFromUserName( userFind.getText().toString() );
					if(user != null){
						RecordConfirmDialog recordConfirm = RecordConfirmDialog.newInstance(
								user.getFirstName(), user.getLastName(), user.getUserName(), amountCash, owe);
						
						recordConfirm.show(getActivity().getFragmentManager(), "Confirm Record");
						
						recordConfirm.setSelectListener(new onSelect(){

							@Override
							public void comfirmPressed() {
								
								transactionsController.setTransactionInsertListener(new TransactionInsertListener(){
									
									@Override
									public void insertComplete(Integer result) {
										if(result == -2){
											errorMessage.setText("Timeout: record not inserted");
										}
										else if(result == -3){
											errorMessage.setText("Failure: record not inserted :(");
										}
										else{
											errorMessage.setText("");
											userFind.setText("");
											walletFind.setText("");
											whoPay.clearCheck();
											amount.setText("0.00");
											comments.setText("");
											listener.insertedRecord(result);
										}
									}
								
								});
								
								transactionsController.insert(httpRequest, profileL, (Integer)profile.getInt("id", 0), user.getUserID(), amountD, selectedWallet, commentsText, owe, mPDialog);
								
							}

							@Override
							public void cancelPressed() {
								System.out.println("Cancel");
							}
							
						});
					}
					else{
						System.out.println("NULL USR");
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
