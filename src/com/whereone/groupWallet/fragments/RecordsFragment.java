package com.whereone.groupWallet.fragments;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

import android.app.ListFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.customAdapters.AutoResizeTextView;
import com.whereone.groupWallet.customAdapters.RecordListAdapter;
import com.whereone.groupWallet.customAdapters.RecordListAdapter.RecordListAdapterListener;
import com.whereone.groupWallet.models.Profile;
import com.whereone.groupWallet.models.Record;
import com.whereone.groupWallet.models.User;
import com.whereone.groupWallet.models.Wallet;

public class RecordsFragment extends ListFragment{
	
	private SelfListener selfListener;
	private RecordFragmentListener listener;
	private LruCache<Integer, Drawable> cache;
	private Profile profile;
	private User user;
	private Wallet wallet;
	private Context context;
	private TransactionsController transactionsController;
	
	public void transactionsUpdated(){
		if(selfListener != null){
			selfListener.transactionsUpdated();
		}
	}
	
	private void setSelfListener(SelfListener listener){
		selfListener = listener;
	}
	
	private interface SelfListener{
		public void transactionsUpdated();
	}
	
	public void setRecordFragmentListener(RecordFragmentListener listener){
		this.listener = listener;
	}
	
	public interface RecordFragmentListener{
		public void oweUserClicked(User user);
		public void owedUserClicked(User user);
		public void recordClicked(Record record);
	}
	
	public void setCache(LruCache<Integer, Drawable> _cache){
		cache = _cache;
	}
	
	public void setUser(User user){
		this.user = user;
	}
	
	public void setWallet(Wallet wallet){
		this.wallet = wallet;
	}
	
	public Boolean update(User user, Wallet wallet){
		if(this.user == user && this.wallet == wallet){
			return false;
		}
		else{
			this.user = user;
			this.wallet = wallet;
			setView();
			setAdapter();
			return true;
		}
	}
	
	private LinearLayout root;
	private View walletView;
	private View relation;
	
	private void setView(){
		
		if( root != null && walletView != null && relation != null ){
			// Set view for wallet filter
			if(wallet != null){
			
				AutoResizeTextView walletName = (AutoResizeTextView) walletView.findViewById(R.id.wallett_name);
				AutoResizeTextView walletTotal = (AutoResizeTextView) walletView.findViewById(R.id.wallett_total);
				
				if(walletName != null){
					walletName.setText( wallet.getName() );
				}
				if(walletTotal != null){
					Double Owe = transactionsController.getOweWallet(profile.getUserID(), wallet.getID());
					Double Owed = transactionsController.getOwedWallet(profile.getUserID(), wallet.getID());
					String formatted = NumberFormat.getCurrencyInstance().format((Owed-Owe));
					walletTotal.setText(formatted);
					if((Owed-Owe) < 0){
						walletTotal.setTextColor(context.getResources().getColor(R.color.red));
					}
				}
				
				root.addView(walletView, 0);
			}
			else{
				root.removeView(walletView);
			}
			// set view for user filter
			if(user != null){
				AutoResizeTextView name = (AutoResizeTextView) relation.findViewById(R.id.relation_name);
				AutoResizeTextView fullName = (AutoResizeTextView) relation.findViewById(R.id.relation_fullName);
				AutoResizeTextView amount = (AutoResizeTextView) relation.findViewById(R.id.relation_total);
				
				if(name != null){
					name.setText("@" + user.getUserName());
				}
				if(fullName != null){
					fullName.setText(user.getFirstName() + " " + user.getLastName());
				}
				if(amount != null){
					
					Double Owe = transactionsController.getOweUser(profile.getUserID(), user.getUserID());
					Double Owed = transactionsController.getOweUser(user.getUserID(), profile.getUserID());
					String formatted = NumberFormat.getCurrencyInstance().format((Owed-Owe));
					amount.setText(formatted);
					if((Owed-Owe) < 0){
						amount.setTextColor(context.getResources().getColor(R.color.red));
					}
					
				}
				
				if(wallet == null){
					root.addView(relation, 0);
				}
				else{
					root.addView(relation, 1);
				}
			}
			else{
				root.removeView( relation );
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_records, null);
		
		root = (LinearLayout) view.findViewById(R.id.frag_records_root);
		walletView = inflater.inflate(R.layout.wallet, null);
		relation = inflater.inflate(R.layout.relationship, null);
		
		setView();
		
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context = getActivity();
		transactionsController = TransactionsController.getInstance();
		profile = Profile.getInstance();
		
		setAdapter();
		
		this.setSelfListener(new SelfListener(){

			@Override
			public void transactionsUpdated() {
				setAdapter();
			}
			
		});
		
	}
	
	private void setAdapter(){
		ArrayList<Record> records;
		if(user == null){
			records = transactionsController.getRecords();
		}
		else{
			records = transactionsController.getRecordsForUser(user.getUserID());
		}
		if(records != null){
			for(int i = 0; i < records.size(); i++){
				records.get(i).findDate();
			}
			Collections.sort(records, Record.Comparitors.DATE);
			if(records != null){
				RecordListAdapter radapter = new RecordListAdapter(context, R.layout.record_row, records, cache);
				radapter.notifyDataSetChanged();
				setListAdapter(radapter);
				
				radapter.setRecordListAdapterListener(new RecordListAdapterListener(){
	
					@Override
					public void oweUserClicked(User user) {
						listener.oweUserClicked(user);
					}
	
					@Override
					public void owedUserClicked(User user) {
						listener.owedUserClicked(user);
					}
	
					@Override
					public void rowClicked(Record record) {
						listener.recordClicked(record);
					}
					
				});
				
			}
		}
	}
}
