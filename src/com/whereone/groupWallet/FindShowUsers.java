package com.whereone.groupWallet;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ListView;

import com.whereone.groupWallet.GetPageUsers.getUsersListener;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.FriendsController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.customAdapters.UserListAdapter;
import com.whereone.groupWallet.customAdapters.UserListAdapter.UserListAdapterListener;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.User;

public class FindShowUsers {
	private Context context;
	private DBhttpRequest httpRequest;
	private Profile profile;
	private EditText userSearch;
	private ListView userList;
	private String filter;
	private GetPageUsers getUsers;
	private ArrayList<User> users;
	private Boolean loadingFlag;
	private Integer start;
	private Integer length;
	private UserListAdapter uAdapter;
	private FindShowUsersListener listener;
	private String buttonText;
	private Integer walletID;
	private ArrayList<Integer> usersExclude;
	private WalletRelationsController walletRelationsController;
	private FriendsController friendsController;
	
	public interface FindShowUsersListener{
		public void userClicked(User user);
		public void buttonClicked(User user);
	}
	
	public void setThisListener(FindShowUsersListener listener){
		this.listener = listener;
	}

	public FindShowUsers(Context context, DBhttpRequest httpRequest, Profile profile, EditText userSearch, 
			ListView userList, Integer length, String buttonText, Integer walletID, 
			WalletRelationsController walletRelationsController, FriendsController friendsController){
		this.context = context;
		this.httpRequest = httpRequest;
		this.profile = profile;
		this.userList = userList;
		this.userSearch = userSearch;
		this.length = length;
		this.start = 0;
		this.buttonText = buttonText;
		this.walletID = walletID;
		this.walletRelationsController = walletRelationsController;
		this.friendsController = friendsController;
		
		setUpUserArrays();
		userSearchTextListener();
		userListScrollListener();
		
	}
	
	private void setUpUserArrays(){
		users = new ArrayList<User>();
		usersExclude = new ArrayList<Integer>();
		usersExclude.add( profile.getUserID() );
		if(walletID != null){
			if(walletID == 0){
				usersExclude.addAll( friendsController.getUserIds(profile.getUserID(), null) );
			}
			else{
				usersExclude.addAll( walletRelationsController.getUsersForWallet(walletID, null, null) );
			}
		}
	}
	
	private void userSearchTextListener(){
		userSearch.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() >= 1){
					Boolean findNew = true;
					if(getUsers != null){ 
						System.out.println("filter: " + filter + " s: " + s.toString()); 
						if(filter.length() < s.length() && s.toString().contains(filter) && users.size() < start+length){
							filter = s.toString();
							findNew = false;
							filter();
							uAdapter = new UserListAdapter(context, R.layout.user_row, users, buttonText, null);
							uAdapter.setUserListAdapterListener(new UserListAdapterListener(){

								@Override
								public void buttonClicked(User user) {
									userButtonClicked(user);
									System.out.println(user.getFirstName());
								}

								@Override
								public void rowClicked(User user) {
									userListClicked(user);
									System.out.println("still " + user.getLastName());
								}
								
							});
							userList.setAdapter(uAdapter);
						}
						else{
							users.clear();
							start = 0;
						}
					}
					if(findNew){
						if(getUsers != null){ getUsers.cancel(true); }
						filter = s.toString();
						loadUsers(userList, start, length, filter);
					}
				}
				else{
					if(getUsers != null){ getUsers.cancel(true); }
					users.clear();
					uAdapter = new UserListAdapter(context, R.layout.user_row, users, buttonText, null);
					uAdapter.setUserListAdapterListener(new UserListAdapterListener(){

						@Override
						public void buttonClicked(User user) {
							userButtonClicked(user);
							System.out.println(user.getFirstName());
						}

						@Override
						public void rowClicked(User user) {
							userListClicked(user);
							System.out.println("still " + user.getLastName());
						}
						
					});
					userList.setAdapter(uAdapter);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	private void userListScrollListener(){
		userList.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(firstVisibleItem+visibleItemCount == totalItemCount &&
						totalItemCount != 0 &&
						users.size() == totalItemCount)
	            {
	                if(loadingFlag == false)
	                {
	                	loadingFlag = true;
	                    System.out.println("load now!!");
	                    start += length;
	                    loadUsers(userList, start, length, filter);
	                }
	            }
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	private void loadUsers(final ListView userList, Integer start, Integer length, String match){
		getUsers = new GetPageUsers(httpRequest, profile, start, length, match, usersExclude);
		getUsers.setUsersListener(new getUsersListener(){

			@Override
			public void getUsersPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getUsersCompleted(ArrayList<User> usersRes, String resultString) {
				users.addAll(usersRes);
				if(users != null && resultString.contains("success")){
					
					int index = userList.getFirstVisiblePosition();
					View v = userList.getChildAt(0);
					int top = (v == null) ? 0 : v.getTop();
					
					uAdapter = new UserListAdapter(context, R.layout.user_row, users, buttonText, null);
					uAdapter.setUserListAdapterListener(new UserListAdapterListener(){

						@Override
						public void buttonClicked(User user) {
							userButtonClicked(user);
							System.out.println(user.getFirstName());
						}

						@Override
						public void rowClicked(User user) {
							userListClicked(user);
							System.out.println("still " + user.getLastName());
						}
						
					});
					userList.setAdapter(uAdapter);
					userList.setSelectionFromTop(index, top);
					loadingFlag = false;
				}
			}

			@Override
			public void getUsersCancelled() {
				// TODO Auto-generated method stub
				
			}
			
		});
		getUsers.execute(context.getString(R.string.getUsersURL));
	}
	
	private void filter(){
		ArrayList<User> temp = new ArrayList<User>();
		for(int i = 0; i < users.size(); i++){
			if( users.get(i).getEmail().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)) 
					|| users.get(i).getFirstName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)) 
					|| users.get(i).getLastName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
					|| users.get(i).getUserName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)) ){
				temp.add( users.get(i) );
			}
		}
		users.clear();
		users.addAll(temp); 
	}
	
	private void userListClicked(User user){
		if(listener != null){
			listener.userClicked(user);
		}
	}
	private void userButtonClicked(User user){
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(userSearch.getWindowToken(), 0);
		if(listener != null){
			listener.buttonClicked(user);
		}
	}
}
