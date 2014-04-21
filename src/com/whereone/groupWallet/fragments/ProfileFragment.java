package com.whereone.groupWallet.fragments;

import java.text.NumberFormat;
import java.util.concurrent.RejectedExecutionException;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.whereone.GetGravitarImage;
import com.whereone.GetGravitarImage.GravitarImageListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.controllers.WalletRelationsController;
import com.whereone.groupWallet.customAdapters.AutoResizeTextView;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.User;

public class ProfileFragment extends Fragment{
	
	private ProfileListener listener;
	private ImageView profilePic;
	private User user;
	Integer dpHeight;
	Integer dpWidth;
	Integer height;
	Integer width;

	public void initScreenInfo(){
		Display display = getActivity().getWindowManager().getDefaultDisplay();
	    DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);

	    float density  = getResources().getDisplayMetrics().density;
	    dpHeight =  (int) (outMetrics.heightPixels / density);
	    dpWidth  = (int) (outMetrics.widthPixels / density);
	    Resources r = getResources();
	    height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) (dpHeight-25), r.getDisplayMetrics());
		width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) (dpWidth-25), r.getDisplayMetrics());
		System.out.println(dpHeight + " " + dpWidth + " " + height + " " + width);
	}
	
	public void setUser(User user){
		this.user = user;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, null);
		
		
		initScreenInfo();
		
		final TransactionsController transactionsController = TransactionsController.getInstance();
		WalletRelationsController walletRelationsController = WalletRelationsController.getInstance();
		
		Profile profile = Profile.getInstance();
		
		if(user == null){
			user = Profile.getInstance();
		}
		
		TextView username = (TextView) view.findViewById(R.id.profile_username);
		TextView name = (TextView) view.findViewById(R.id.profile_name);
		profilePic = (ImageView) view.findViewById(R.id.profile_pic);
		AutoResizeTextView totalMoney = (AutoResizeTextView) view.findViewById(R.id.profile_totalMoney);
		Button walletNum = (Button) view.findViewById(R.id.profile_walletNum);
		
		String email = user.getEmail();
		GetGravitarImage getGravitarImage = new GetGravitarImage(email);
		getGravitarImage.setGravitarImageListener(new GravitarImageListener(){

			@Override
			public void getImageComplete(Drawable result) {
				profilePic.setImageDrawable(result);
			}
			
		});
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
			try{
				getGravitarImage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, width/2);
			}
			catch(RejectedExecutionException e){
				getGravitarImage.execute(width/2);
			}
	   	}
	   	else {
	   		getGravitarImage.execute(width/2);
	   	}
		
		username.setText("@"+user.getUserName());
		name.setText(user.getFirstName() + " " + user.getLastName());
		
		if(profile.getUserID() == user.getUserID()){
			
			Double totalOwe = transactionsController.getTotalOwe(user.getUserID());
			Double totalOwed = transactionsController.getTotalOwed(user.getUserID());
			String formatted = NumberFormat.getCurrencyInstance().format((totalOwed-totalOwe));
			if((totalOwed < totalOwe)){
				totalMoney.setTextColor(getActivity().getResources().getColor(R.color.red));
			}
			totalMoney.setText(formatted);
			
			Integer numWallets = walletRelationsController.getWalletsForUser(user.getUserID(), 1).size();
			walletNum.setText("You are in "+numWallets+" wallets");
			
		}
		else{
			Double totalOwe = transactionsController.getOweUser(profile.getUserID(), user.getUserID());
			Double totalOwed = transactionsController.getOweUser(user.getUserID(), profile.getUserID());
			String formatted = NumberFormat.getCurrencyInstance().format((totalOwed-totalOwe));
			if((totalOwed < totalOwe)){
				totalMoney.setTextColor(getActivity().getResources().getColor(R.color.red));
			}
			totalMoney.setText(formatted);
			
			Integer sharedWallets = walletRelationsController.getWalletsForUser(user.getUserID(), 1).size();
			if(sharedWallets == 1){
				walletNum.setText("You share " + sharedWallets + " wallet");
			}
			else{
				walletNum.setText("You share " + sharedWallets + " wallets");
			}
		}
		
		walletNum.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.walletsClicked();
			}
			
		});
		
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void setProfileListener(ProfileListener listener){
		this.listener = listener;
	}
	
	public interface ProfileListener{
		public void walletsClicked();
	}
	
}
