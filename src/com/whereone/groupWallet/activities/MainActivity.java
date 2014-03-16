package com.whereone.groupWallet.activities;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.fragments.ProfileFragment;
import com.whereone.groupWallet.fragments.QuickPayFragment;
import com.whereone.groupWallet.fragments.RecordsFragment;
import com.whereone.groupWallet.fragments.RelationshipsFragment;
import com.whereone.groupWallet.fragments.RelationshipsFragment.RelationshipListener;
import com.whereone.groupWallet.fragments.WalletsFragment;
import com.whereone.groupWallet.fragments.WalletsFragment.WalletFragmentListener;
import com.whereone.groupwalletcake.GetData;
import com.whereone.groupwalletcake.GetData.getDataListener;
import com.whereone.groupwalletcake.LogOutCurrent;
import com.whereone.groupwalletcake.R;
/*
 * Notes TODO:
 * 	insert record -> clean up esp. with logout
 * 				  -> add record to local database and update
 */
public class MainActivity extends Activity {
	 
	 private DrawerLayout mDrawerLayout;
	 private ListView mDrawerList;
	 private ActionBarDrawerToggle mDrawerToggle;

	 private CharSequence mDrawerTitle;
	 private CharSequence mTitle;
	 private String[] mPlanetTitles;
	 
	 private DBhttpRequest httpRequest;
	 
	 private final Fragment quickPay = new QuickPayFragment();
	 private final Fragment records = new RecordsFragment();
	 private final Fragment profileFrag = new ProfileFragment();
	 private final Fragment walletFrag = new WalletsFragment();
	 private final Fragment relationshipFrag = new RelationshipsFragment();
	 private SharedPreferences profile;
	 
	 private LogOutCurrent logout;
	 private GetData getData;
	 private Fragment current;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		httpRequest = new DBhttpRequest();
		
		setupUI(findViewById(R.id.drawer_layout), this);
		
		final WalletsController walletTable = new WalletsController(this);
		final WalletRelationsController walletRelationTable = new WalletRelationsController(this);
		final UsersController userTable = new UsersController(this);
		final TransactionsController transactionTable = new TransactionsController(this, null);
		profile = this.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		logout = new LogOutCurrent(profile, transactionTable, userTable, walletRelationTable, walletTable);
		
		getData = new GetData(httpRequest, profile, transactionTable, userTable, walletRelationTable, walletTable);
		getData.checkForNewData();
		
		getData.setGetDataListener(new getDataListener(){

			@Override
			public void getWalletComplete(Integer result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertWalletComplete() {
				// TODO Auto-generated method stub
				((QuickPayFragment) quickPay).walletsUpdated();
				((WalletsFragment) walletFrag).walletsUpdated();
			}

			@Override
			public void getRecordsComplete(Integer result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertRecordsComplete() {
				((RecordsFragment) records).transactionsUpdated();
				((RelationshipsFragment) relationshipFrag).walletRelationshipsUpdated();
			}

			@Override
			public void getWalletRelationsComplete(Integer result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertWalletRelationsComplete(ArrayList<Integer> newRelations) {
				((QuickPayFragment) quickPay).walletRelationsUpdated(newRelations);
			}

			@Override
			public void getUserComplete(Integer result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertUserComplete(Integer userID) {
				((QuickPayFragment) quickPay).userUpdated(userID);
			}
			
		});
		
		((WalletsFragment) walletFrag).setWalletFragmentListener( new WalletFragmentListener(){

			@Override
			public void walletClicked(Integer walletID) {
				current = relationshipFrag;
				((RelationshipsFragment) relationshipFrag).newWalletID(walletID);
				getFragmentManager().beginTransaction().replace(R.id.container, relationshipFrag).commit();
				mDrawerList.setItemChecked(4, true);
		        setTitle(mPlanetTitles[4]);
			}
			
		});
		
		((RelationshipsFragment) relationshipFrag).setRelationshipListener( new RelationshipListener(){

			@Override
			public void walletNameClicked() {
				current = walletFrag;
				getFragmentManager().beginTransaction().replace(R.id.container, walletFrag).commit();
				mDrawerList.setItemChecked(3, true);
		        setTitle(mPlanetTitles[3]);
			}
			
		});
		
		mTitle = mDrawerTitle = getTitle();
		ArrayList<String> test = new ArrayList<String>();
		test.add("lskdjfsdf"); test.add("eteme"); test.add("qwje");
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
            getData.checkForNewData();
        }
    }

    private void selectItem(int position) {
    	switch (position){
    		case 0:
    			if(current != profileFrag){
	    			current = profileFrag;
	    			getFragmentManager().beginTransaction().replace(R.id.container, profileFrag).commit();
    			}
    			break;
    		case 1:
    			if(current != quickPay){
	    			current = quickPay;
	    			getFragmentManager().beginTransaction().replace(R.id.container, quickPay).commit();
    			}
    			break;
    		case 2:
    			if(current != records){
	    			current = records;
	    			getFragmentManager().beginTransaction().replace(R.id.container, records).commit();
    			}
    			break;
    		case 3:
    			if(current != walletFrag){
	    			current = walletFrag;
	    			getFragmentManager().beginTransaction().replace(R.id.container, walletFrag).commit();
    			}
    			break;
    		case 4:
    			if(current != relationshipFrag){
	    			current = relationshipFrag;
	    			getFragmentManager().beginTransaction().replace(R.id.container, relationshipFrag).commit();
    			}
    			break;
    		case 5:
    			SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
				String currentDate = s.format(new Date());
				try {
					mDrawerList.setItemChecked(position, true);
			        setTitle(mPlanetTitles[position]);
					logout.logOut(getApplicationContext(), httpRequest,
	    					profile.getString("publicToken", ""), computeHash((profile.getString("privateToken", "") + currentDate)), currentDate);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    			break;
    		default:
    			break;
    	}
    	
    	mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
    	mDrawerLayout.closeDrawer(mDrawerList);
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
    
    private String computeHash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException{
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
