/**
 * 	Author: Jonathan Harrison
 * 	Date: 3/22/14
 * 
 * 	Use:
 
 
		String email = profile.getString("email", "");
		GetGravitarImage getGravitarImage = new GetGravitarImage(email);
		getGravitarImage.setGravitarImageListener(new GravitarImageListener(){

			@Override
			public void getImageComplete(Drawable result) {
				Log.i("ProfileFrag", "lkejflksjefkljsf");
				profilePic.setImageDrawable(result);
			}
			
		});
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
			getGravitarImage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, width/2);
	   	}
	   	else {
	   		getGravitarImage.execute(width/2);
	   	}
 
 * 
 */

package com.whereone;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class GetGravitarImage extends AsyncTask<Integer, Void, Drawable>{

	private GravitarImageListener listener;
	private String email;
	
	public GetGravitarImage(String email){
		email = email.toLowerCase(Locale.US);
		this.email = email;
	}
	
	public void setGravitarImageListener(GravitarImageListener _listener){
		this.listener = _listener;
	}
	
	@Override
	protected Drawable doInBackground(Integer... arg0) {
		String hash = MD5Util.md5Hex(email);
		InputStream is;
		try {
			is = (InputStream) new URL("http://www.gravatar.com/avatar/"+hash+"?s=" + arg0[0] + "&d=http%3A%2F%2Fwhereone.com%2Fuser.png").getContent();
			Drawable d = Drawable.createFromStream(is, "src name");
			return d;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e){
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Drawable result) {
		listener.getImageComplete(result);
	}

	
	public interface GravitarImageListener{
		public void getImageComplete(Drawable result);
	}
}
