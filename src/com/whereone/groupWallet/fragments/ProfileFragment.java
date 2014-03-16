package com.whereone.groupWallet.fragments;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.whereone.CustomHorizontalScrollView;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupwalletcake.R;

public class ProfileFragment extends Fragment{
	
	ArrayList<String> wallets = new ArrayList<String>();
	private LinearLayout linearLayout;
	private CustomHorizontalScrollView horizontalScrollView;
	private ListView friendView;
	private ListView walletView;
	Integer height;
	Integer width;

	public void initScreenInfo(){
		Display display = getActivity().getWindowManager().getDefaultDisplay();
	    DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);

	    float density  = getResources().getDisplayMetrics().density;
	    Integer dpHeight = (int) (outMetrics.heightPixels / density);
	    Integer dpWidth  = (int) (outMetrics.widthPixels / density);
	    Resources r = getResources();
	    height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpHeight-25, r.getDisplayMetrics());
		width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpWidth-25, r.getDisplayMetrics());
		  
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, null);
		
		//initScreenInfo();
		//setHorizontalScroll(view);
		
		final SharedPreferences profile = getActivity().getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		
		WalletsController walletTable = new WalletsController(getActivity());
		
		TextView username = (TextView) view.findViewById(R.id.profile_username);
		TextView name = (TextView) view.findViewById(R.id.profile_name);
		walletView = (ListView) view.findViewById(R.id.profile_wallets);
		friendView = (ListView) view.findViewById(R.id.profile_friends);
		
		username.setText("@"+profile.getString("username", ""));
		name.setText(profile.getString("firstName", "") + " " + profile.getString("lastName", ""));
		
		ArrayList<String> friend = new ArrayList<String>();
		friend.add("klsdfjklsdjf");friend.add("klsdfjklsdjf");friend.add("klsdfjklsdjf");friend.add("klsdfjklsdjf");friend.add("klsdfjklsdjf");friend.add("klsdfjklsdjf");friend.add("klsdfjklsdjf");friend.add("klsdfjklsdjf");friend.add("klsdfjklsdjf");
		ArrayAdapter<String> adapterFriends = new ArrayAdapter<String>(getActivity(), R.layout.list_item, friend);
	   	friendView.setAdapter(adapterFriends);
		
		wallets = walletTable.getWalletNames();
		if(wallets != null){
			wallets.addAll(walletTable.getWalletNames());
			ArrayAdapter<String> adapterWallets = new ArrayAdapter<String>(getActivity(), R.layout.list_item, wallets);
		   	walletView.setAdapter(adapterWallets);
		}
		
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void setHorizontalScroll(View view){
		horizontalScrollView = new CustomHorizontalScrollView(getActivity());
		// get parent container linearLayout
		linearLayout = (LinearLayout) view.findViewById(R.id.layer);
		linearLayout.addView(horizontalScrollView);
		// create a horizontal linearLayout to contain each page
		LinearLayout scrollContainer = new LinearLayout(getActivity());
		scrollContainer.setLayoutParams(new LayoutParams(width, -1));
		// create and add first page
		walletView = new ListView(getActivity());
		walletView.setLayoutParams(new LayoutParams(width, height));
		//scrollContainer.addView(walletView);
		/*
		walletView.setOnTouchListener(new ListView.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	        	System.out.println("listTouched");
	        	
	            int action = event.getAction();
	            switch (action) {
	            case MotionEvent.ACTION_DOWN:
	                // Disallow ScrollView to intercept touch events.
	                v.getParent().requestDisallowInterceptTouchEvent(true);
	                break;

	            case MotionEvent.ACTION_UP:
	                // Allow ScrollView to intercept touch events.
	                v.getParent().requestDisallowInterceptTouchEvent(true);
	                break;
	            case MotionEvent.ACTION_SCROLL:
	            	v.getParent().requestDisallowInterceptTouchEvent(true);
	            	break;
	            }
	           

	            // Handle ListView touch events.
	            v.onGenericMotionEvent(event);
	            v.onTouchEvent(event);
	            return true;
	        }
	    });
	    */
		// create and add second page
		friendView = new ListView(getActivity());
		friendView.setLayoutParams(new LayoutParams(width, height));
		//scrollContainer.addView(friendView);
		
		horizontalScrollView.addView(walletView);
	}
}
