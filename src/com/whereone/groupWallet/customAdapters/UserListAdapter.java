/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is a list view adapter for a list of Users
 */

package com.whereone.groupWallet.customAdapters;

import java.util.ArrayList;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.whereone.GetGravitarImage;
import com.whereone.GetGravitarImage.GravitarImageListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.models.User;

public class UserListAdapter extends ArrayAdapter<User> {
	
	private ArrayList<User> users;
	private LruCache<Integer, Drawable> drawableCache;
	private String buttonText;
	
	private Drawable defaultPic;
	
	private UserListAdapterListener listener;
	
	public UserListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    
	    defaultPic = context.getResources().getDrawable(R.drawable.ic_user);
	}
	
	public void setUserListAdapterListener(UserListAdapterListener listener){
		this.listener = listener;
	}
	
	public interface UserListAdapterListener{
		public void buttonClicked(User user);
		public void rowClicked(User User);
	}
	
	public UserListAdapter(Context context, int resource, ArrayList<User> _users, String buttonText, LruCache<Integer, Drawable> cache) {

	    super(context, resource, _users);

	    this.users = _users;
	    if(buttonText != null){
	    	this.buttonText = buttonText;
	    }
	    else{
	    	this.buttonText = "Select";
	    }
	  	
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
	        v = vi.inflate(R.layout.user_row, null);

	    }
	    
	    final User user = users.get(position);

	    v.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(listener != null){
					listener.rowClicked(user);
				}
			}
	    	
	    });

        TextView username = (TextView) v.findViewById(R.id.userRow_username); 
        TextView firstName = (TextView) v.findViewById(R.id.userRow_firstName);
        TextView lastName = (TextView) v.findViewById(R.id.userRow_lastName);
        TextView email = (TextView) v.findViewById(R.id.userRow_email); 
        
        ImageView image = (ImageView) v.findViewById(R.id.userRow_Image);
        
        Button selectButton = (Button) v.findViewById(R.id.userRow_button);

        
        if(username != null){
    	   String usernameStr = user.getUserName();
    	   username.setText("@" + usernameStr);
        }
    	
    	if(firstName != null){
    		String nameFirst = user.getFirstName();
    		
    		firstName.setText(nameFirst);
    	}
    	
    	if(lastName != null){
    		String nameLast = user.getLastName();
    		
    		lastName.setText(nameLast);
    	}
    	
    	if(email != null){
    		String emailStr = user.getEmail();
    		email.setText(emailStr);
    	}
        	
        
        if(image != null){
        	setImage(image, user.getEmail(), user.getUserID());
        }
        
        if(selectButton != null){
        	selectButton.setText(buttonText);
        	selectButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					listener.buttonClicked(user);
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
