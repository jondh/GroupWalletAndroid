package com.whereone.groupWallet.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.whereone.groupWallet.FindShowUsers;
import com.whereone.groupWallet.FindShowUsers.FindShowUsersListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletRelationsController.WalletRelationInsertListener;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.controllers.WalletsController.walletsControllerListener;
import com.whereone.groupWallet.customAdapters.UserListAdapter;
import com.whereone.groupWallet.customAdapters.UserListAdapter.UserListAdapterListener;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.User;
import com.whereone.groupWallet.models.WalletRelation;

public class AddFragment extends Fragment{
	
	private String LOG = "AddFragment";
	private AddListener listener;
	Integer dpHeight;
	Integer dpWidth;
	Integer height;
	Integer width;
	
	private ProgressDialog mpDialog;
	
	private WalletsController walletsController;
	private WalletRelationsController walletRelationsController;
	private UsersController usersController;
	private DBhttpRequest httpRequest;
	private Profile profile;
	private Integer type;
	private Integer walletID;
	
	private Integer length;
	private ArrayList<User> users; 
	private Boolean submitClicked;

	public void initScreenInfo(){
		Display display = getActivity().getWindowManager().getDefaultDisplay();
	    DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);

	    float density  = getResources().getDisplayMetrics().density;
	    dpHeight =  (int) (outMetrics.heightPixels / density);
	    dpWidth  = (int) (outMetrics.widthPixels / density);
	    Resources r = getResources();
	    height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) (dpHeight), r.getDisplayMetrics());
		width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) (dpWidth), r.getDisplayMetrics());
		System.out.println(dpHeight + " " + dpWidth + " " + height + " " + width);
	}
	
	public void setType(Integer type, Integer walletID){
		if(type == 0 || type == 1){
			this.type = type;
		}
		this.walletID = walletID;
	}
	
	public enum Type{
		WALLET ,
		USER 
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add, null);
		
		initScreenInfo();
		setupUI(view, getActivity());
		
		if(type == null){
			setType(Type.WALLET.ordinal(), null);
		}
		
		length = 10;
		users = new ArrayList<User>(); 
		
		mpDialog = new ProgressDialog(getActivity());
		
		final ScrollView walletView = (ScrollView) view.findViewById(R.id.add_walletViewScroll);
		final LinearLayout userView = (LinearLayout) view.findViewById(R.id.add_userView);
		final ListView userList = (ListView) view.findViewById(R.id.add_userList);
		final EditText userSearch = (EditText) view.findViewById(R.id.add_userFilter);
		
		if(type == Type.WALLET.ordinal()){
			walletView.setVisibility(View.VISIBLE);
			userView.setVisibility(View.INVISIBLE);
			
			submitClicked = false;
			
			final EditText insertWallet = (EditText) view.findViewById(R.id.add_walletSearch);
			final Button inviteUser = (Button) view.findViewById(R.id.add_walletInvite);
			final Button submit = (Button) view.findViewById(R.id.add_submit);
			final ListView inviteUserList = (ListView) view.findViewById(R.id.add_walletInviteList);
			
			insertWallet.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
					insertWallet.setError(null);
				}
				
			});
			
			insertWallet.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if( walletsController.containsWalletName(insertWallet.getText().toString()) ){
						insertWallet.setError("You already have a wallet named this");
					}
				}
				
			});
			
			inviteUser.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					walletView.setVisibility(View.INVISIBLE);
					userView.setVisibility(View.VISIBLE);
					
					FindShowUsers findShowUsers = new FindShowUsers(getActivity(), httpRequest, profile, userSearch, userList, length, "select", walletID, walletRelationsController);
					findShowUsers.setThisListener(new FindShowUsersListener(){

						@Override
						public void userClicked(User user) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void buttonClicked(User user) {
							
							walletView.setVisibility(View.VISIBLE);
							userView.setVisibility(View.INVISIBLE);
							
							users.add(user);
							
							final UserListAdapter uAdapter = new UserListAdapter(getActivity(), R.layout.user_row, users, "remove", null);
							uAdapter.setUserListAdapterListener(new UserListAdapterListener(){

								@Override
								public void buttonClicked(User user) {
									users.remove(user);
									uAdapter.notifyDataSetChanged();
								}

								@Override
								public void rowClicked(User user) {
									
								}
								
							});
							inviteUserList.setAdapter(uAdapter);
						}
						
					});
				
				}
			});
			
			submit.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					View curFocus = getActivity().getCurrentFocus();
					if(curFocus != null){
						curFocus.clearFocus();
						curFocus.requestFocus();
					}
					
					if(insertWallet.getError() == null && !submitClicked){
						mpDialog.setMessage("loading");
						mpDialog.show();
						System.out.println("Submit Clicked");
						submitClicked = true;
						walletsController.insertPutWallet(httpRequest, profile, insertWallet.getText().toString(), walletRelationsController);
						walletsController.setWalletsControllerListener(new walletsControllerListener(){
	
							@Override
							public void getWalletComplete(final Integer result) { // 1->success 0->empty -1->failure -2->timeout -3->unknownHost
								
								submitClicked = false;
								getActivity().runOnUiThread(new Runnable(){

									@Override
									public void run() {
										mpDialog.hide();
										if(result == -2){
											// TODO: Timeout Response
										}
										else if(result == -3){
											// TODO: Unknown Host Response
										}
									}
									
								});
								if(result == -1){
									// TODO: general failure
									Log.i(LOG, "failure in inserting/putting wallet");
								}
							}
							
						});
					}
				}
			});
			
		}
		else if(type == Type.USER.ordinal()){
			walletView.setVisibility(View.INVISIBLE);
			userView.setVisibility(View.VISIBLE);
			
			FindShowUsers findShowUsers = new FindShowUsers(getActivity(), httpRequest, profile, userSearch, userList, length, "select", walletID, walletRelationsController);
			findShowUsers.setThisListener(new FindShowUsersListener(){

				@Override
				public void userClicked(User user) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void buttonClicked(final User user) {
					
					new AlertDialog.Builder(getActivity())
				    .setTitle("Invite user")
				    .setMessage("Invite "+ user.getFirstName() + " " + user.getLastName() + 
				    		" (@" + user.getUserName() + ") to wallet " + walletsController.getWalletNameFromId(walletID) +"?")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	
				        	mpDialog.setMessage("loading");
							mpDialog.show();
				            walletRelationsController.insertPutWalletRelation(httpRequest, profile, new WalletRelation(0,walletID,user.getUserID(),false), usersController);
				            walletRelationsController.setWalletRelationInsertListener(new WalletRelationInsertListener(){

								@Override
								public void insertPutComplete(Integer result) {
									mpDialog.hide();
									listener.newUserInserted(walletID);
								}
				            	
				            });
				            
				        }
				     })
				    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            
				        }
				     })
				     .show();
				}
				
			});
		}
		
		
		
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		walletsController = WalletsController.getInstance();
		walletRelationsController = WalletRelationsController.getInstance();
		usersController = UsersController.getInstance();
		httpRequest = DBhttpRequest.getInstance();
		profile = Profile.getInstance();
		
		
	}
	
	public void setAddListener(AddListener listener){
		this.listener = listener;
	}
	
	public interface AddListener{
		public void newWalletInserted();
		public void newUserInserted(Integer walletID);
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
