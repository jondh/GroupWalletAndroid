/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is a list view adapter for a list of records
 */

package com.whereone.groupWallet.customAdapters;

import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.whereone.GetGravitarImage;
import com.whereone.GetGravitarImage.GravitarImageListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.models.Record;
import com.whereone.groupWallet.models.User;

public class RecordListAdapter extends ArrayAdapter<Record> {
	
	private List<Record> records;
	private LruCache<Integer, Drawable> drawableCache;
	
	private Drawable defaultPic;
	
	private RecordListAdapterListener listener;
	
	public RecordListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    
	    defaultPic = context.getResources().getDrawable(R.drawable.ic_user);
	}
	
	public void setRecordListAdapterListener(RecordListAdapterListener listener){
		this.listener = listener;
	}
	
	public interface RecordListAdapterListener{
		public void oweUserClicked(User user);
		public void owedUserClicked(User user);
		public void rowClicked(Record record);
		
	}
	
	public RecordListAdapter(Context context, int resource, List<Record> _records, LruCache<Integer, Drawable> cache) {

	    super(context, resource, _records);

	    this.records = _records;
	  	
	    if(cache == null){
	    	drawableCache = new LruCache<Integer, Drawable>(20);
	    }
	    else{
	    	drawableCache = cache;
	    }
	    
	    defaultPic = context.getResources().getDrawable(R.drawable.ic_user);
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		
	    View v = convertView;

	    if (v == null) {

	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.record_row, null);

	    }
	    
	    final Record record = records.get(position);

	    v.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				listener.rowClicked(record);
			}
	    	
	    });

        TextView oweName = (TextView) v.findViewById(R.id.record_oweName); 
        TextView oweFirstName = (TextView) v.findViewById(R.id.record_oweFirstName);
        TextView oweLastName = (TextView) v.findViewById(R.id.record_oweLastName);
        TextView owedName = (TextView) v.findViewById(R.id.record_owedName); 
        TextView owedFirstName = (TextView) v.findViewById(R.id.record_owedFirstName);
        TextView owedLastName = (TextView) v.findViewById(R.id.record_owedLastName);
        TextView tt1 = (TextView) v.findViewById(R.id.record_amount);
        TextView tt3 = (TextView) v.findViewById(R.id.record_comments);
        TextView dateView = (TextView) v.findViewById(R.id.record_date);
        
        ImageView oweImage = (ImageView) v.findViewById(R.id.record_oweImage);
        ImageView owedImage = (ImageView) v.findViewById(R.id.record_owedImage);

        final User oweUser = UsersController.getInstance().getUserFromId(record.getOweId());
        final User owedUser = UsersController.getInstance().getUserFromId(record.getOwedId());
        
        if (oweName != null && oweUser != null && owedUser != null) {
        	String owe = oweUser.getUserName();
        	
        	oweName.setText("@" + owe);
        	
        	if(oweFirstName != null){
        		String oweFirst = oweUser.getFirstName();
        		
        		oweFirstName.setText(oweFirst);
        	}
        	
        	if(oweLastName != null){
        		String oweLast = oweUser.getLastName();
        		
        		oweLastName.setText(oweLast);
        	}
        	
        }
        if(owedName != null && oweUser != null && owedUser != null){
        	String owed = owedUser.getUserName();
        	owedName.setText("@" + owed);
        	
        	if(owedFirstName != null){
        		String owedFirst = owedUser.getFirstName();
        		
        		owedFirstName.setText(owedFirst);
        	}
        	
        	if(owedLastName != null){
        		String owedLast = owedUser.getLastName();
        		
        		owedLastName.setText(owedLast);
        	}
        }
        if (tt1 != null) {
        	String formatted = NumberFormat.getCurrencyInstance().format( record.getAmount() );
            tt1.setText( formatted );
        }
        if (tt3 != null) {
            tt3.setText(record.getComments());
        }
        if (dateView != null) {
        	String formatDate = record.getFormattedDate().trim();
        	dateView.setText( formatDate );
        }
        if(oweImage != null && oweUser != null && owedUser != null){
        	setImage(oweImage, oweUser.getEmail(), oweUser.getUserID());
        	
        	oweImage.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					listener.oweUserClicked( oweUser );
				}
        		
        	});
        	
        }
        if(owedImage != null && oweUser != null && owedUser != null){
        	setImage(owedImage, owedUser.getEmail(), owedUser.getUserID());
        	
        	owedImage.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					listener.owedUserClicked( owedUser );
				}
        		
        	});
        }

    return v;

	}
	
	private void setImage(final ImageView iview, String email, final Integer userID){
		
		Drawable image = (Drawable) drawableCache.get(userID);
		if(image == null){
		
			GetGravitarImage getGravitarImage = new GetGravitarImage(email);
			getGravitarImage.setGravitarImageListener(new GravitarImageListener(){
	
				@Override
				public void getImageComplete(Drawable result) {
					if(result != null){
						iview.setImageDrawable(result);
						drawableCache.put(userID, result);
					}
					else{
						iview.setImageDrawable(defaultPic);
						drawableCache.put(userID, defaultPic);
					}
				}
				
			});
			if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
				try{
					getGravitarImage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 100);
				}
				catch(RejectedExecutionException e){
					System.out.println("executorException");
				}
		   	}
		   	else {
		   		getGravitarImage.execute(100);
		   	}
		}
		else{
			iview.setImageDrawable(image);
		}
	}
}
