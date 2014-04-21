package com.whereone.groupWallet;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;


public class TextValidate {
	
	public void validate(final EditText editText, final String regExp, final Integer length, Boolean clearError){
		editText.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				editText.removeTextChangedListener(this);
				String temp = s.toString();
				if(regExp != null){
					temp = temp.replaceAll(regExp, "");
				}
				if(length != null){
					if(temp.length() > length){
						editText.setError("Max Length of " + length);
						temp = temp.substring(0, temp.length() -1);
					}
					else{
						editText.setError(null);
					}
				}
				if(!s.toString().contentEquals(temp)){
					editText.setText(temp);
					editText.setSelection(temp.length());
				}
				editText.addTextChangedListener(this);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
			}
			
		});
		
		if(clearError){
			editText.setOnFocusChangeListener(new OnFocusChangeListener(){
	
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					editText.setError(null);
				}
				
			});
		}
	}
	
	public String getAlphaNumericUnderscore(){
		return "[^a-zA-Z0-9_]";
	}
	
	public String getAlphaNumericUnderSpace(){
		return "[^a-zA-Z0-9_ ]";
	}
	
	public String getEmailCheck(){
		return "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
	}

}
