package com.whereone.groupwalletcake;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.whereone.LogIn;
import com.whereone.LogIn.LogInListener;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.models.Profile;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		final DBhttpRequest httpRequest = new DBhttpRequest();
		final Intent intent = new Intent(this, QuickPayActivity.class);
		final Context context = this.getApplicationContext();
		final ProgressDialog mPDialog = new ProgressDialog(this);
		
		SharedPreferences profile = context.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		final SharedPreferences.Editor profileEdit = profile.edit();
		
		final TextView loginError = (TextView) findViewById(R.id.loginError);
		final EditText userLogin = (EditText) findViewById(R.id.userLogin);
		final EditText passwordLogin = (EditText) findViewById(R.id.password);
		Button login = (Button) findViewById(R.id.loginButton);
		
		login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        	    mPDialog.setMessage("Loading...");
        	    mPDialog.show();  
        	    loginError.setVisibility(View.INVISIBLE);
            	LogIn logIn = new LogIn(httpRequest, userLogin.getText().toString(), passwordLogin.getText().toString());
        	   	logIn.setLogInListener(new LogInListener(){
        	   		@Override
        	   		public void logInComplete(Profile _user){
        	   			if(_user != null){
	        	   			profileEdit.putInt("id", _user.getUserID());
	        	   			profileEdit.putString("username", _user.getUserName());
	        	   			profileEdit.putString("password", _user.getPassword());
	        	   			profileEdit.putString("firstName", _user.getFirstName());
	        	   			profileEdit.putString("lastName", _user.getLastName());
	        	   			profileEdit.putString("email", _user.getEmail());
	        	   			profileEdit.putInt("fbID", _user.getFbID());
	        	   			profileEdit.putString("privateToken", _user.getPrivateToken());
	        	   			profileEdit.putString("publicToken", _user.getPublicToken());
	        	   			profileEdit.commit();
	        	   			
	        	   			mPDialog.hide();  
	        	   			startActivity(intent);
        	   			}
        	   			else{
        	   				mPDialog.hide();  
        	   				loginError.setVisibility(View.VISIBLE);
        	   			}
        	   		}
        	   		@Override
        	   		public void logInCancelled(){
        	   			mPDialog.hide();  
        	   		}
        	   	});
        	   	logIn.execute(getString(R.string.logInURL));
            }
        });
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
