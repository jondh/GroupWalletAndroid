package com.whereone.groupWallet.activities;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ListView;

import com.whereone.groupWallet.GetData;
import com.whereone.groupWallet.GetData.getDataListener;
import com.whereone.groupWallet.LogOutCurrent;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.fragments.AddFragment;
import com.whereone.groupWallet.fragments.AddFragment.AddListener;
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
		
		application = (GWApplication) this.getApplication();
		
		final TransactionsController transactionsController = TransactionsController.getInstance();
		final WalletsController walletsController = WalletsController.getInstance();
		final UsersController usersController = UsersController.getInstance();
		final WalletRelationsController walletRelationsController = WalletRelationsController.getInstance();
		
		final DBhttpRequest httpRequest = DBhttpRequest.getInstance();
		
		profileL = Profile.getInstance();
		
		logOut = new LogOutCurrent(httpRequest, profileL, application);
		
		picCache = application.drawableCache;
		
		logOutFlag = false;
		logOutGetData = false;
		
		((RecordsFragment) records).setCache(picCache);
		((RecordDetailedFragment) recordDetailedFrag).setCache(picCache);
		
		getData = new GetData(httpRequest, profileL, transactionsController, walletRelationsController, walletsController, usersController);
		
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
			public void inviteWalletComplete(Integer result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getRelationsComplete(ArrayList<Integer> result,	ArrayList<WalletRelation> newRelations) {
				if(result.contains(-1)){
					//logOutNow();
					getData.cancel();
					logOutGetData = true;
				}
				if(newRelations != null){
					((QuickPayFragment) quickPay).walletRelationsUpdated(newRelations);
				}
			}

			@Override
			public void getUsersComplete(Integer result, ArrayList<Integer> results) {
				((RelationshipsFragment) relationshipFrag).walletRelationshipsUpdated();
			}

			@Override
			public void getDataComplete(Boolean walletFailFlag,
					Boolean relationFailFlag, Boolean transactionFailFlag,
					Boolean walletInviteFailFlag, Boolean usersFailFlag) {
				if(logOutGetData){
					logOutGetData = false;
					logOutNow();
				}
			}
			
		});
		
		((WalletsFragment) walletFrag).setWalletFragmentListener( new WalletFragmentListener(){

			@Override
			public void walletClicked(Wallet wallet) {
				showRelations(wallet, wallet.getID());
			}

			@Override
			public void addWallet() {
				showAdd(AddFragment.Type.WALLET, null);
			}

			@Override
			public void load() {
				getData.getWallets(profileL.getUserID());
			}
			
		});
		
		((RelationshipsFragment) relationshipFrag).setRelationshipListener( new RelationshipListener(){

			@Override
			public void walletNameClicked() {
				showWallets();
			}

			@Override
			public void relationClicked(User user, Wallet wallet) {
		        showTransactions(user, wallet);
			}

			@Override
			public void addUserClicked(Wallet wallet) {
				showAdd(AddFragment.Type.USER, wallet.getID());
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
					showTransactions(null, null);
				}
			}
			
		});
		
		((RecordsFragment) records).setRecordFragmentListener(new RecordFragmentListener(){

			@Override
			public void oweUserClicked(User user) {
				showProfile(user);
			}

			@Override
			public void owedUserClicked(User user) {
				showProfile(user);
			}

			@Override
			public void recordClicked(Record record) {
				showDetailedTransaction(record);
			}
			
		});
		
		((ProfileFragment) profileFrag).setProfileListener(new ProfileListener(){

			@Override
			public void walletsClicked() {
				showWallets();
			}
			
		});
		
		((AddFragment) addFrag).setAddListener(new AddListener(){

			@Override
			public void newWalletInserted() {
				showWallets();
			}

			@Override
			public void newUserInserted(final Integer walletID) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						showRelations(null, walletID);
					}
					
				});
				
			}
			
		});
		
		((WalletInviteFragment) walletInviteFrag).setWalletInviteFragmentListener(new WalletInviteFragmentListener(){

			@Override
			public void walletClicked(Wallet wallet) {
				  
			}

			@Override
			public void accpeted(Wallet wallet) {
				showRelations(wallet, wallet.getID());
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
        inflater.inflate(R.menu.quick_pay, menu);
        return super.onCreateOptionsMenu(menu);
	}

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.action_settings:
            // create intent to perform web search for this planet
        	/*
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
            // catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            */
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
            	getData.checkForNewData();
            }
        }
    }

    private void selectItem(int position) {
    	switch (position){
    		case 0:
    			showProfile(profileL);
    			break;
    		case 1:
    			showQuickPay();
    			break;
    		case 2:
    			showTransactions(null, null);
    			break;
    		case 3:
    			showWallets();
    			break;
    		case 4:
    			showRelations(null, null);
    			break;
    		case 5:
    			mDrawerList.setItemChecked(position, true);
		        setTitle(mPlanetTitles[position]);
    			logOutNow();
	    		break;
    		case 6:
    			showInvites();
    			break;
    		default:
    			break;
    	}
    	mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    public void showProfile(User user){
    	if(current != profileFrag){
			((ProfileFragment)profileFrag).setUser(user);
			current = profileFrag;
			getFragmentManager().beginTransaction().replace(R.id.container, profileFrag).commit();
			mDrawerList.setItemChecked(0, true);
	        setTitle(mPlanetTitles[0]);
		}
    }
    
    public void showQuickPay(){
    	if(current != quickPay){
			current = quickPay;
			getFragmentManager().beginTransaction().replace(R.id.container, quickPay).commit();
			mDrawerList.setItemChecked(1, true);
	        setTitle(mPlanetTitles[1]);
    	}
    }
    
    public void showTransactions(User user, Wallet wallet){
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
    }
    
    public void showWallets(){
    	if(current != walletFrag){
			current = walletFrag;
			getFragmentManager().beginTransaction().replace(R.id.container, walletFrag).commit();
			mDrawerList.setItemChecked(3, true);
	        setTitle(mPlanetTitles[3]);
		}
    }
    
    public void showRelations(Wallet wallet, Integer walletID){
    	if(current != relationshipFrag){
			current = relationshipFrag;
			((RelationshipsFragment) relationshipFrag).setWallet(wallet, walletID);
			getFragmentManager().beginTransaction().replace(R.id.container, relationshipFrag).commit();
			mDrawerList.setItemChecked(4, true);
	        setTitle(mPlanetTitles[4]);
		}
    }
    
    public void showInvites(){
    	if(current != walletInviteFrag){
			current = walletInviteFrag;
			getFragmentManager().beginTransaction().replace(R.id.container, walletInviteFrag).commit();
			mDrawerList.setItemChecked(6, true);
	        setTitle(mPlanetTitles[6]);
		}
    }
    
    public void showDetailedTransaction(Record record){
    	if(current != recordDetailedFrag){
	    	current = recordDetailedFrag;
	    	((RecordDetailedFragment)recordDetailedFrag).setRecord(record);
			getFragmentManager().beginTransaction().replace(R.id.container, recordDetailedFrag).commit();
    	}
    }
    
    public void showAdd(AddFragment.Type type, Integer walletID){
    	if(current != addFrag){
    		current = addFrag;
	    	((AddFragment) addFrag).setType(type.ordinal(), walletID);
			getFragmentManager().beginTransaction().replace(R.id.container, addFrag).commit();
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
            
            return true;
        }
        return false;
    }
}
