package com.whereone.groupWallet.activities;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.LruCache;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.whereone.groupWallet.GetData;
import com.whereone.groupWallet.GetData.getDataListener;
import com.whereone.groupWallet.LogOutCurrent;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.FriendsController;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.fragments.AddFragment;
import com.whereone.groupWallet.fragments.AddFragment.AddListener;
import com.whereone.groupWallet.fragments.FragmentStack;
import com.whereone.groupWallet.fragments.ProfileFragment;
import com.whereone.groupWallet.fragments.ProfileFragment.ProfileListener;
import com.whereone.groupWallet.fragments.QuickPayFragment;
import com.whereone.groupWallet.fragments.QuickPayFragment.QuickPayListener;
import com.whereone.groupWallet.fragments.RecordDetailedFragment;
import com.whereone.groupWallet.fragments.RecordsFragment;
import com.whereone.groupWallet.fragments.RecordsFragment.RecordFragmentListener;
import com.whereone.groupWallet.fragments.RelationshipsFragment;
import com.whereone.groupWallet.fragments.RelationshipsFragment.RelationshipListener;
import com.whereone.groupWallet.fragments.WalletInviteFragment;
import com.whereone.groupWallet.fragments.WalletInviteFragment.WalletInviteFragmentListener;
import com.whereone.groupWallet.fragments.WalletsFragment;
import com.whereone.groupWallet.fragments.WalletsFragment.WalletFragmentListener;
import com.whereone.groupWallet.models.Friend;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.Record;
import com.whereone.groupWallet.models.User;
import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupWallet.models.WalletRelation;

public class MainActivity extends Activity {
	 
	 private DrawerLayout mDrawerLayout;
	 private ListView mDrawerList;
	 private ActionBarDrawerToggle mDrawerToggle;

	 private CharSequence mDrawerTitle;
	 private CharSequence mTitle;
	 private String[] mPlanetTitles;
	 
	 private long time = 0;
	 
	 private FragmentStack fragmentStack;
	 
	 private GWApplication application;
	 
	 private final Fragment quickPay = new QuickPayFragment();
	 private final Fragment records = new RecordsFragment();
	 private final Fragment profileFrag = new ProfileFragment();
	 private final Fragment walletFrag = new WalletsFragment();
	 private final Fragment relationshipFrag = new RelationshipsFragment();
	 private final Fragment recordDetailedFrag = new RecordDetailedFragment();
	 private final Fragment addFrag = new AddFragment();
	 private final Fragment walletInviteFrag = new WalletInviteFragment();

	 private Fragment current;
	 
	 private GetData getData;
	 private LogOutCurrent logOut;
	 private Profile profileL;
	 
	 private LruCache<Integer, Drawable> picCache;
	 
	 private Boolean logOutFlag;
	 private Boolean logOutGetData;
	 private Integer numWalletInvites;
	 
	 private Intent demoIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.addContentView(new View(this), (new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)));
        dialog.show();
		*/
		setContentView(R.layout.activity_main);
		
		setupUI(findViewById(R.id.drawer_layout), this);
		
		fragmentStack = new FragmentStack();
		
		application = (GWApplication) this.getApplication();
		demoIntent = new Intent(this, TutorialActivity.class);
		
		final TransactionsController transactionsController = TransactionsController.getInstance();
		final WalletsController walletsController = WalletsController.getInstance();
		final UsersController usersController = UsersController.getInstance();
		final WalletRelationsController walletRelationsController = WalletRelationsController.getInstance();
		final FriendsController friendsController = FriendsController.getInstance();
		
		final DBhttpRequest httpRequest = DBhttpRequest.getInstance();
		
		profileL = Profile.getInstance();
		
		logOut = new LogOutCurrent(httpRequest, profileL, application);
		
		picCache = application.drawableCache;
		
		logOutFlag = false;
		logOutGetData = false;
		
		((RecordsFragment) records).setCache(picCache);
		((RecordDetailedFragment) recordDetailedFrag).setCache(picCache);
		
		getData = new GetData(httpRequest, profileL, transactionsController, walletRelationsController, walletsController, usersController, friendsController);
		
		getData.setGetDataListener(new getDataListener(){

			@Override
			public void getWalletComplete(Integer result) {
				// TODO Auto-generated method stub
				if(result == -1){
					//logOutNow();
					getData.cancel();
					logOutGetData = true;
				}
				else if(result >= 0){
					((WalletsFragment) walletFrag).walletsUpdated();
				}
			}

			@Override
			public void getRecordsComplete(Integer result) {
				if(result == -1){
					//logOutNow();
					getData.cancel();
					logOutGetData = true;
				}
				else if(result == 1){
					((QuickPayFragment) quickPay).walletsUpdated();
					((WalletsFragment) walletFrag).walletsUpdated();
				}
				((RecordsFragment) records).transactionsUpdated();
				((RelationshipsFragment) relationshipFrag).walletRelationshipsUpdated();
			}

			@Override
			public void inviteWalletComplete(Integer result, ArrayList<Wallet> wallets) {
				if(wallets != null){
					numWalletInvites = getData.getNumInvites();
					invalidateOptionsMenu();
				}
			}

			@Override
			public void getRelationsComplete(ArrayList<Integer> result,	ArrayList<WalletRelation> newRelations) {
				if(result.contains(-1)){
					//logOutNow();
					getData.cancel();
					logOutGetData = true;
				}
				if(newRelations != null){
					if(current == quickPay){
						((QuickPayFragment) quickPay).walletRelationsUpdated(newRelations);
					}
				}	
			}

			@Override
			public void getUsersComplete(Integer result, ArrayList<Integer> results) {
				((RelationshipsFragment) relationshipFrag).walletRelationshipsUpdated();
			}

			@Override
			public void getFriendsComplete(Integer result, ArrayList<Friend> friends) {
				if(friends != null && current == quickPay){
					((QuickPayFragment) quickPay).friendsUpdated(friends);
				}
				numWalletInvites = getData.getNumInvites();
				invalidateOptionsMenu();
			}

			@Override
			public void getDataComplete(Boolean walletFailFlag,
					Boolean relationFailFlag, Boolean transactionFailFlag,
					Boolean walletInviteFailFlag, Boolean usersFailFlag,
					Boolean friendsFailFlag) {
				if(logOutGetData){
					logOutGetData = false;
					logOutNow();
				}
			}
			
		});
		
		((WalletsFragment) walletFrag).setWalletFragmentListener( new WalletFragmentListener(){

			@Override
			public void walletClicked(Wallet wallet) {
				showRelations(wallet, wallet.getID(), true);
			}

			@Override
			public void addWallet() {
				showAdd(AddFragment.Type.WALLET, null, true);
			}

			@Override
			public void load() {
				getData.getWallets(profileL.getUserID());
			}
			
		});
		
		((RelationshipsFragment) relationshipFrag).setRelationshipListener( new RelationshipListener(){

			@Override
			public void walletNameClicked() {
				showWallets(true);
			}

			@Override
			public void relationClicked(User user, Wallet wallet) {
		        showTransactions(user, wallet, true);
			}

			@Override
			public void addUserClicked(Wallet wallet) {
				showAdd(AddFragment.Type.USER, wallet.getID(), true);
			}

			@Override
			public void load(Wallet wallet) {
		    	if( wallet != null ){
		    		ArrayList<Integer> tempWallet = new ArrayList<Integer>();
			    	tempWallet.add(wallet.getID());
			    	getData.getRelationsAndUsers(tempWallet);
		    	}
			}
			
		});
		
		((QuickPayFragment) quickPay).setQuickPayListener( new QuickPayListener(){

			@Override
			public void insertedRecord(Integer result) {
				if(result == -1){
					logOutNow();
				}
				else{
					showTransactions(null, null, true);
				}
			}
			
		});
		
		((RecordsFragment) records).setRecordFragmentListener(new RecordFragmentListener(){

			@Override
			public void oweUserClicked(User user) {
				showProfile(user, true);
			}

			@Override
			public void owedUserClicked(User user) {
				showProfile(user, true);
			}

			@Override
			public void recordClicked(Record record) {
				showDetailedTransaction(record, true);
			}

			@Override
			public void update() {
				getData.getTransactions();
			}
			
		});
		
		((ProfileFragment) profileFrag).setProfileListener(new ProfileListener(){

			@Override
			public void walletsClicked() {
				showWallets(true);
			}
			
		});
		
		((AddFragment) addFrag).setAddListener(new AddListener(){

			@Override
			public void newWalletInserted() {
				showWallets(true);
			}

			@Override
			public void newUserInserted(final Integer walletID) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						showRelations(null, walletID, true);
					}
					
				});
				
			}
			
		});
		
		((WalletInviteFragment) walletInviteFrag).setWalletInviteFragmentListener(new WalletInviteFragmentListener(){

			@Override
			public void walletClicked(Wallet wallet, Friend friend) {
				  
			}

			@Override
			public void accpeted(Wallet wallet, Friend friend) {
				if(wallet == null){
					showRelations(null, 0, true);
				}
				else{
					showRelations(wallet, wallet.getID(), true);
				}
				numWalletInvites -= 1;
				invalidateOptionsMenu();
			}

			@Override
			public void failure(Integer result) {
				if(result == -1){
					logOutNow();
				}
			}
			
		});
		
		mTitle = mDrawerTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(R.array.options_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
      //  mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
       // getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
               this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
		
	} 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.quick_pay, menu);
		//return true;
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        
        if( numWalletInvites == null){
        	numWalletInvites = getData.getNumInvites();
        }
        
        View count = menu.findItem(R.id.badge).getActionView();
        Button notifWalletCount;
        notifWalletCount = (Button) count.findViewById(R.id.notif_count);
        notifWalletCount.setText( numWalletInvites.toString() );
        notifWalletCount.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showInvites();
			}
		});
        
        if(numWalletInvites == 0){
        	count.setVisibility(View.INVISIBLE);
        }
        
        return super.onCreateOptionsMenu(menu);
	}

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
       // menu.findItem(R.id.badge).setVisible(!drawerOpen);
    	if (numWalletInvites == 0){
    		menu.findItem(R.id.badge).setVisible(false);
    	}
    	else{
    		menu.findItem(R.id.badge).setVisible(true);
    	}
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        getData.getInvites(profileL.getUserID());
        
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.sync:
        	syncData();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
        
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
            Calendar timeTemp = Calendar.getInstance();
            if((timeTemp.getTime().getTime() - time) > 60000){
            	time = timeTemp.getTime().getTime();
            	//getData.checkForNewData();
            }
        }
    }

    private void selectItem(int position) {
    	System.out.println(position);
    	switch (position){
    		case 0:
    			showProfile(profileL, true);
    			break;
    		case 1:
    			showQuickPay(true);
    			break;
    		case 2:
    			showTransactions(null, null, true);
    			break;
    		case 3:
    			showWallets(true);
    			break;
    		case 4:
    			showRelations(null, null, true);
    			break;
    		case 5:
    			mDrawerList.setItemChecked(position, true);
		        setTitle(mPlanetTitles[position]);
    			startActivity(demoIntent);
    			break;
    		case 6:
    			mDrawerList.setItemChecked(position, true);
		        setTitle(mPlanetTitles[position]);
    			logOutNow();
	    		break;
    		default:
    			break;
    	}
    	mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    public void showProfile(User user, Boolean addToStack){
    	if(current != profileFrag){
			((ProfileFragment)profileFrag).setUser(user);
			current = profileFrag;
			getFragmentManager().beginTransaction().replace(R.id.container, profileFrag).commit();
			mDrawerList.setItemChecked(0, true);
	        setTitle(mPlanetTitles[0]);
	        
	        if(addToStack){
	        	fragmentStack.add(FragmentStack.FRAGMENT.Profile, null, null, null, null, null);
	        }
		}
    }
    
    public void showQuickPay(Boolean addToStack){
    	if(current != quickPay){
			current = quickPay;
			getFragmentManager().beginTransaction().replace(R.id.container, quickPay).commit();
			mDrawerList.setItemChecked(1, true);
	        setTitle(mPlanetTitles[1]);
	        
	        if(addToStack){
	        	fragmentStack.add(FragmentStack.FRAGMENT.QuickPay, null, null, null, null, null);
	        }
    	}
    }
    
    public void showTransactions(User user, Wallet wallet, Boolean addToStack){
    	if(current != records){
			current = records;
			((RecordsFragment)records).setUser(user);
			((RecordsFragment)records).setWallet(wallet);
			getFragmentManager().beginTransaction().replace(R.id.container, records).commit();
			mDrawerList.setItemChecked(2, true);
	        setTitle(mPlanetTitles[2]);
		}
    	else{
			((RecordsFragment)records).update(user, wallet);
    	}
    	if(addToStack){
    		fragmentStack.add(FragmentStack.FRAGMENT.Transaction, user, wallet, null, null, null);
    	}
    }
    
    public void showWallets(Boolean addToStack){
    	if(current != walletFrag){
			current = walletFrag;
			getFragmentManager().beginTransaction().replace(R.id.container, walletFrag).commit();
			mDrawerList.setItemChecked(3, true);
	        setTitle(mPlanetTitles[3]);
	        
	        if(addToStack){
	        	fragmentStack.add(FragmentStack.FRAGMENT.Wallet, null, null, null, null, null);
	        }
		}
    }
    
    public void showRelations(Wallet wallet, Integer walletID, Boolean addToStack){
    	if(current != relationshipFrag){
			current = relationshipFrag;
			((RelationshipsFragment) relationshipFrag).setWallet(wallet, walletID);
			getFragmentManager().beginTransaction().replace(R.id.container, relationshipFrag).commit();
			mDrawerList.setItemChecked(4, true);
	        setTitle(mPlanetTitles[4]);
	        
	        if(addToStack){
	        	fragmentStack.add(FragmentStack.FRAGMENT.Relation, null, wallet, walletID, null, null);
	        }
		}
    }
    
    public void showInvites(){
    	if(current != walletInviteFrag){
			current = walletInviteFrag;
			getFragmentManager().beginTransaction().replace(R.id.container, walletInviteFrag).commit();
		}
    }
    
    public void showDetailedTransaction(Record record, Boolean addToStack){
    	if(current != recordDetailedFrag){
	    	current = recordDetailedFrag;
	    	((RecordDetailedFragment)recordDetailedFrag).setRecord(record);
			getFragmentManager().beginTransaction().replace(R.id.container, recordDetailedFrag).commit();
			
			if(addToStack){
				fragmentStack.add(FragmentStack.FRAGMENT.Relation, null, null, null, record, null);
			}
    	}
    }
    
    public void showAdd(AddFragment.Type type, Integer walletID, Boolean addToStack){
    	if(current != addFrag){
    		current = addFrag;
	    	((AddFragment) addFrag).setType(type.ordinal(), walletID);
			getFragmentManager().beginTransaction().replace(R.id.container, addFrag).commit();
			if(addToStack){
				fragmentStack.add(FragmentStack.FRAGMENT.Relation, null, null, walletID, null, type);
			}
    	}
    }

    private void logOutNow() {
    	if(!logOutFlag){
	    	logOutFlag = true;
	    	logOut.logOut();
    	}
	}

	@Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
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
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            FragmentStack.FragmentType popFrag = fragmentStack.remove();
            
            if(popFrag != null){
            
	            if(popFrag.type == FragmentStack.FRAGMENT.Profile){
	            	showProfile(popFrag.user, false);
	            }
	            else if(popFrag.type == FragmentStack.FRAGMENT.QuickPay){
	            	showQuickPay(false);
	            }
	            else if(popFrag.type == FragmentStack.FRAGMENT.Transaction){
	            	showTransactions(popFrag.user, popFrag.wallet, false);
	            }
	            else if(popFrag.type == FragmentStack.FRAGMENT.DetailedTransaction){
	            	showDetailedTransaction(popFrag.record, false);
	            }
	            else if(popFrag.type == FragmentStack.FRAGMENT.Wallet){
	            	showWallets(false);
	            }
	            else if(popFrag.type == FragmentStack.FRAGMENT.Relation){
	            	showRelations(popFrag.wallet, popFrag.walletID, false);
	            }
	            else if(popFrag.type == FragmentStack.FRAGMENT.Add){
	            	showAdd(popFrag.addType, popFrag.walletID, false);
	            }
	            
            }
        	
            return true;
        }
        return false;
    }
	
	private void syncData(){
		if(current == walletFrag){
			((WalletsFragment) walletFrag).update();
		}
		else if(current == relationshipFrag){
			((RelationshipsFragment) relationshipFrag).update();
		}
		else if(current == records){
			((RecordsFragment) records).update();
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
    
}
