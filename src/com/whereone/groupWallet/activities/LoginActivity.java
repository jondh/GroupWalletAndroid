package com.whereone.groupWallet.activities;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.whereone.LogIn;
import com.whereone.LogIn.LogInListener;
import com.whereone.LogInFacebook;
import com.whereone.LogInFacebook.LoginFBListener;
import com.whereone.NewUser;
import com.whereone.NewUser.NewUserListener;
import com.whereone.groupWallet.R;
import com.whereone.groupWallet.TextValidate;
import com.whereone.groupWallet.controllers.DBhttpRequest;
import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.models.Profile;


public class LoginActivity extends Activity {
	
	private UiLifecycleHelper uiHelper;
	private String LOG = "LoginActivity";
	private GraphUser graphUser;
	private Intent intent;
	private SharedPreferences.Editor profileEdit;
	private ProgressDialog mPDialog;
	private Boolean sessionCallback;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.i("HelloFacebook", String.format("Error: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.i("HelloFacebook", "Success!");
        }
    };
    
    private RelativeLayout loginLayout;
    private ScrollView newUserScroll;
    private LinearLayout newUserLayout;
    private RelativeLayout mainLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		final Context context = this;
		sessionCallback = false;
		
		intent = new Intent(this, StartupActivity.class);
		final GWApplication application = (GWApplication) this.getApplication();
		mPDialog = new ProgressDialog(this);
		
		SharedPreferences profile = application.getSharedPreferences("com.whereone.groupWallet.profile", Context.MODE_PRIVATE);
		profileEdit = profile.edit();
		
		final TextValidate validator = new TextValidate();
		
		mainLayout = (RelativeLayout) findViewById(R.id.login_mainLayout);
		Button login_login = (Button) findViewById(R.id.login_login);
		Button login_newUser = (Button) findViewById(R.id.login_newUser);
		final LoginButton facebookLogin = (LoginButton) findViewById(R.id.login_facebook);
		
		newUserScroll = (ScrollView) findViewById(R.id.login_new_scroll);
		newUserLayout = (LinearLayout) findViewById(R.id.login_new);
		final EditText new_username = (EditText) findViewById(R.id.login_new_username);
		final EditText new_firstname = (EditText) findViewById(R.id.login_new_firstName);
		final EditText new_lastname = (EditText) findViewById(R.id.login_new_lastName);
		final EditText new_email = (EditText) findViewById(R.id.login_new_email);
		final EditText new_password = (EditText) findViewById(R.id.login_new_password);
		final EditText new_passwordConfirm = (EditText) findViewById(R.id.login_new_passwordConfirm);
		final TextView new_userError = (TextView) findViewById(R.id.login_newUserError);
		Button new_submit = (Button) findViewById(R.id.login_new_submit);
		Button new_back = (Button) findViewById(R.id.login_new_back);
		
		loginLayout = (RelativeLayout) findViewById(R.id.login_loginLayout);
		final TextView loginError = (TextView) findViewById(R.id.loginError);
		final EditText userLogin = (EditText) findViewById(R.id.userLogin);
		final EditText passwordLogin = (EditText) findViewById(R.id.password);
		Button login = (Button) findViewById(R.id.loginButton);
		Button login_back = (Button) findViewById(R.id.login_back);
		
		final MyInt loginSize = new MyInt();
		final MyInt newUserSize = new MyInt();
		
		validator.validate(new_username, validator.getAlphaNumericUnderscore(), 15, true);
		validator.validate(new_firstname, validator.getAlphaNumericUnderSpace(), 15, true);
		validator.validate(new_lastname, validator.getAlphaNumericUnderSpace(),  15, true);
		validator.validate(new_email, null, 40, false);
		validator.validate(new_password, null, 20, true);
		validator.validate(new_passwordConfirm, null, 20, true);
		
		new_username.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(new_username.length() < 5 && new_username.length() != 0){
					new_username.setError("Your username should be at least 5 characters");
				}
			}
			
		});
		
		new_email.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					if( !new_email.getText().toString().matches(validator.getEmailCheck()) && new_email.getText().toString().length() > 0){
						new_email.setError("Not valid email");
					}
					else{
						new_email.setError(null);
					}
				}
			}
			
		});
		
		new_password.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(new_password.length() < 6 && new_password.length() != 0){
					new_password.setError("Your password should be at least 6 characters");
				}
				if( !new_passwordConfirm.getText().toString().contentEquals(new_password.getText().toString() )
						&& new_passwordConfirm.getText().toString().length() > 0){
					new_passwordConfirm.setError("Passwords must match..");
				}
				else{
					new_passwordConfirm.setError(null);
				}
			}
			
		});
		
		new_passwordConfirm.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					if( !new_passwordConfirm.getText().toString().contentEquals(new_password.getText().toString() ) 
							&&  new_passwordConfirm.getText().toString().length() > 0){
						new_passwordConfirm.setError("Passwords must match..");
					}
					else{
						new_passwordConfirm.setError(null);
					}
				}
			}
			
		});
		
		login_login.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				mainLayout.setVisibility(View.INVISIBLE);
				loginLayout.setVisibility(View.VISIBLE);
				Rect r = new Rect();
				loginLayout.getWindowVisibleDisplayFrame(r);
		        loginSize.myInt = (r.bottom-r.top);
			}
			
		});
		
		login_newUser.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mainLayout.setVisibility(View.INVISIBLE);
				newUserLayout.setVisibility(View.VISIBLE);
				newUserScroll.setVisibility(View.VISIBLE);
				Rect r = new Rect();
				newUserLayout.getWindowVisibleDisplayFrame(r);
		        newUserSize.myInt = (r.bottom-r.top);
			}
		});
		
		login_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loginLayout.setVisibility(View.INVISIBLE);
				Rect r = new Rect();
				loginLayout.getWindowVisibleDisplayFrame(r);
				
				if(loginSize.myInt > (r.bottom-r.top)){
					InputMethodManager inputMethodManager = (InputMethodManager)  context.getSystemService(Activity.INPUT_METHOD_SERVICE);
					inputMethodManager.toggleSoftInput(0, 0);
				}
				mainLayout.setVisibility(View.VISIBLE);
				
			}
		});
		
		new_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mainLayout.setVisibility(View.VISIBLE);
				newUserScroll.setVisibility(View.INVISIBLE);
				newUserLayout.setVisibility(View.INVISIBLE);
			}
		});
		
		facebookLogin.setReadPermissions(Arrays.asList("email"));
		
		facebookLogin.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(final GraphUser user) {
            	if(sessionCallback){
            		sessionCallback = false;
	            	if(user == null){
	            		System.out.println("no user");
	            	}
	            	if(user != null){
	            		graphUser = user;
	            		System.out.println(user.getId() );
	            		final Session session = Session.getActiveSession();
	            		
	            		logInFacebook(context, session, false);
	            	}
            	}
            }
        });
			
		
		new_submit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View curFocus = getCurrentFocus();
				if(curFocus != null){
					curFocus.clearFocus();
					curFocus.requestFocus();
				}
				
				String username = new_username.getText().toString();
				String email = new_email.getText().toString();
				String firstname = new_firstname.getText().toString();
				String lastname = new_lastname.getText().toString();
				String password = new_password.getText().toString();
				
				if(username.length() == 0){
					new_username.setError("A username is required");
				}
				if(email.length() == 0){
					new_email.setError("An email address is required");
				}
				if(firstname.length() == 0){
					new_firstname.setError("A first name is required");
				}
				if(lastname.length() == 0){
					new_lastname.setError("A last name is required");
				}
				if(password.length() == 0){
					new_password.setError("You really do need a password");
				}
				else if(new_passwordConfirm.length() == 0){
					new_passwordConfirm.setError("Please retype your password");
				}
				
				if( (new_username.getError() == null)
						&& (new_email.getError() == null)
						&& (new_firstname.getError() == null)
						&& (new_lastname.getError() == null)
						&& (new_password.getError() == null)
						&& (new_passwordConfirm.getError() == null) ){
					
					System.out.println("EVERYTHING IS AWESOME");
					
					mPDialog.setMessage("Loading...");
	        	    mPDialog.show();  
					
	        	    new_userError.setVisibility(View.INVISIBLE);
	        	    
					NewUser newUser = new NewUser(DBhttpRequest.getInstance(), username, password, email, firstname, lastname, "GroupWallet");
					newUser.setNewUserListener(new NewUserListener(){

						@Override
						public void newUserPreExecute() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void newUserComplete(String result) {
							if(result.contains("success")){
		        	   			profileEdit.putInt("id", Profile.getInstance().getUserID());
		        	   			profileEdit.putString("username", Profile.getInstance().getUserName());
		        	   			profileEdit.putString("password", Profile.getInstance().getPassword());
		        	   			profileEdit.putString("firstName", Profile.getInstance().getFirstName());
		        	   			profileEdit.putString("lastName", Profile.getInstance().getLastName());
		        	   			profileEdit.putString("email", Profile.getInstance().getEmail());
		        	   			profileEdit.putString("fbID", Profile.getInstance().getFbID());
		        	   			profileEdit.putString("privateToken", Profile.getInstance().getPrivateToken());
		        	   			profileEdit.putString("publicToken", Profile.getInstance().getPublicToken());
		        	   			profileEdit.commit();
		        	   			UsersController.getInstance().insertUser(Profile.getInstance());
		        	   			
		        	   			mPDialog.hide();  
		        	   			intent.putExtra("NewUser", true);
		        	   			startActivity(intent);
	        	   			}
							else if(result.contains("not unique") || result.contains("bad")){
								if(result.contains("not unique email")){
									new_email.setError("Email already in use");
								}
								else if(result.contains("bad email")){
									new_email.setError("Invalid Email");
								}
								if(result.contains("not unique username")){
									new_username.setError("username already in use");
								}
								else if(result.contains("bad username")){
									new_username.setError("Invalid Username");
								}
								mPDialog.hide();  
							}
	        	   			else if(result.contains("timeout")){
	        	   				mPDialog.hide();  
	        	   				new_userError.setText("Timeout Error :(");
	        	   				new_userError.setVisibility(View.VISIBLE);
	        	   			}
	        	   			else if(result.contains("unknownHost")){
	        	   				mPDialog.hide();  
	        	   				new_userError.setText("Failure: Possible Internet Error :/");
	        	   				new_userError.setVisibility(View.VISIBLE);
	        	   			}
	        	   			else{
	        	   				mPDialog.hide();  
	        	   				new_userError.setVisibility(View.VISIBLE);
	        	   			}
						}

						@Override
						public void newUserCancelled() {
							mPDialog.hide();  
						}
						
					});
					newUser.execute(getString(R.string.newUserURL));
					
				}
				
				
				
			}
			
		});
		
		
		login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        	    mPDialog.setMessage("Loading...");
        	    mPDialog.show();  
        	    loginError.setVisibility(View.INVISIBLE);
            	LogIn logIn = new LogIn(DBhttpRequest.getInstance(), userLogin.getText().toString(), passwordLogin.getText().toString());
        	   	logIn.setLogInListener(new LogInListener(){
        	   		@Override
        	   		public void logInComplete(String result){
        	   			if(result.contains("success")){
	        	   			profileEdit.putInt("id", Profile.getInstance().getUserID());
	        	   			profileEdit.putString("username", Profile.getInstance().getUserName());
	        	   			profileEdit.putString("password", Profile.getInstance().getPassword());
	        	   			profileEdit.putString("firstName", Profile.getInstance().getFirstName());
	        	   			profileEdit.putString("lastName", Profile.getInstance().getLastName());
	        	   			profileEdit.putString("email", Profile.getInstance().getEmail());
	        	   			profileEdit.putString("fbID", Profile.getInstance().getFbID());
	        	   			profileEdit.putString("privateToken", Profile.getInstance().getPrivateToken());
	        	   			profileEdit.putString("publicToken", Profile.getInstance().getPublicToken());
	        	   			profileEdit.commit();
	        	   			UsersController.getInstance().insertUser(Profile.getInstance());
	        	   			
	        	   			mPDialog.hide();  
	        	   			intent.putExtra("NewUser", false);
	        	   			startActivity(intent);
        	   			}
        	   			else if(result.contains("timeout")){
        	   				mPDialog.hide();  
        	   				loginError.setText("Timeout Error :(");
        	   				loginError.setVisibility(View.VISIBLE);
        	   			}
        	   			else if(result.contains("unknownHost")){
        	   				mPDialog.hide();  
        	   				loginError.setText("Possible Internet Error :/");
        	   				loginError.setVisibility(View.VISIBLE);
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
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	uiHelper.onResume();
    }
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if ( (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
            new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Cancelled")
                .setMessage("Permission not Granted")
                .setPositiveButton("ok", null)
                .show();
            
        } 
        else{
    		sessionCallback = true;
        }
    }
    
    public void logInFacebook(final Context context, final Session session, final Boolean newUser){
    	LogInFacebook logInFacebook = new LogInFacebook(DBhttpRequest.getInstance(), graphUser, session.getAccessToken(), newUser);
    	mPDialog.setMessage("Loading...");
	    mPDialog.show();  
    	logInFacebook.setLoginFBListener(new LoginFBListener(){

			@Override
			public void LoginComplete(String result) {
				if(result.contains("success")){
					profileEdit.putInt("id", Profile.getInstance().getUserID());
    	   			profileEdit.putString("username", Profile.getInstance().getUserName());
    	   			profileEdit.putString("password", Profile.getInstance().getPassword());
    	   			profileEdit.putString("firstName", Profile.getInstance().getFirstName());
    	   			profileEdit.putString("lastName", Profile.getInstance().getLastName());
    	   			profileEdit.putString("email", Profile.getInstance().getEmail());
    	   			profileEdit.putString("fbID", Profile.getInstance().getFbID());
    	   			profileEdit.putString("privateToken", Profile.getInstance().getPrivateToken());
    	   			profileEdit.putString("publicToken", Profile.getInstance().getPublicToken());
    	   			profileEdit.commit();
    	   			UsersController.getInstance().insertUser(Profile.getInstance());
    	   			
    	   			mPDialog.hide();  
    	   			intent.putExtra("NewUser", newUser);
    	   			startActivity(intent);
				}
				else if(result.contains("none")){
					new AlertDialog.Builder(context)
				    .setTitle("No Accout")
				    .setMessage("Do you want to create a new account for Facebook user "+ graphUser.getName()+"?")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            logInFacebook(context, session, true);
				        }
				     })
				    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            session.closeAndClearTokenInformation();
				        }
				     })
				     .show();
				}
				else if(result.contains("failure")){
					if(result.contains("Token")){
						Log.i(LOG, "bad fb token, need to re-login");
					}
					Log.i(LOG, "some failure happened");
				}
				else if( result.contains("timeout") ){
					Log.i(LOG, "timeout error from LogInFacebook");
				}
				else if( result.contains("unknownHost") ){
					Log.i(LOG, "unknown host error from LogInFacebook");
				}
			}

			@Override
			public void LoginCancelled() {
				// TODO Auto-generated method stub
				
			}
			
		});
		logInFacebook.execute(getString(R.string.loginFacebookURL));
    }
	
	public static void hideSoftKeyboard(Context context2) {
	    InputMethodManager inputMethodManager = (InputMethodManager)  context2.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(((Activity) context2).getCurrentFocus().getWindowToken(), 0);
	}
	
	private class MyInt{
		public Integer myInt;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
        	mainLayout.setVisibility(View.VISIBLE);
			loginLayout.setVisibility(View.INVISIBLE);
			newUserScroll.setVisibility(View.INVISIBLE);
			newUserLayout.setVisibility(View.INVISIBLE);
            return true;
        }
        return false;
    }

}
