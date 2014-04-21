package com.whereone.groupWallet.fragments;

import java.text.DecimalFormat;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.controllers.WalletsController;
import com.whereone.groupWallet.models.Record;
import com.whereone.groupWallet.models.User;

public class RecordDetailedFragment extends Fragment{
	
	private SelfListener selfListener;
	private LruCache<Integer, Drawable> cache;
	private Record record;
	
	public void transactionsUpdated(){
		if(selfListener != null){
			selfListener.amountsUpdated();
		}
	}
	
	private void setSelfListener(SelfListener listener){
		selfListener = listener;
	}
	
	private interface SelfListener{
		public void amountsUpdated();
	}
	
	public void setCache(LruCache<Integer, Drawable> _cache){
		cache = _cache;
	}
	
	public void setRecord(Record _record){
		record = _record;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_record_detailed, null);
		
		TextView time = (TextView) view.findViewById(R.id.record_detailed_time);
		
		ImageView owePic = (ImageView) view.findViewById(R.id.record_detailed_owe_pic);
		TextView oweUserName = (TextView) view.findViewById(R.id.record_detailed_owe_username);
		TextView oweName = (TextView) view.findViewById(R.id.record_detailed_owe_name);
		
		TextView amount = (TextView) view.findViewById(R.id.record_detailed_amount);
		
		ImageView owedPic = (ImageView) view.findViewById(R.id.record_detailed_owed_pic);
		TextView owedUserName = (TextView) view.findViewById(R.id.record_detailed_owed_username);
		TextView owedName = (TextView) view.findViewById(R.id.record_detailed_owed_name);
		
		TextView walletName = (TextView) view.findViewById(R.id.record_detailed_walletName);
		
		TextView comments = (TextView) view.findViewById(R.id.record_detailed_comments);
		
		if( record != null){
			
			time.setText( record.getFormattedLongDate() );
			Double amountD = record.getAmount();
			DecimalFormat df = new DecimalFormat("#0.00");
			amount.setText( "$" + df.format(amountD) );
			
			comments.setText( record.getComments() );
			
			if( cache != null ){
				Drawable oweTemp = cache.get( record.getOweId() );
				if(oweTemp != null) owePic.setImageDrawable( oweTemp );
				
				Drawable owedTemp = cache.get( record.getOwedId() );
				if(owedTemp != null) owedPic.setImageDrawable( owedTemp );
			}
		
			User oweUser = UsersController.getInstance().getUserFromId( record.getOweId() );
			oweUserName.setText( "@"+oweUser.getUserName() );
			oweName.setText( oweUser.getFirstName() + " " + oweUser.getLastName() );
			
			User owedUser = UsersController.getInstance().getUserFromId( record.getOwedId() );
			owedUserName.setText( "@"+owedUser.getUserName() );
			owedName.setText( owedUser.getFirstName() + " " + owedUser.getLastName() );
		
		
			walletName.setText( WalletsController.getInstance().getWalletNameFromId( record.getWalletId() ) );
		
		}
		
        return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		this.setSelfListener(new SelfListener(){

			@Override
			public void amountsUpdated() {
				// TODO Auto-generated method stub
				
			}
			
		});
		
	}
}
