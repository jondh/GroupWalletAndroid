/**
 * Author: Jonathan Harrison
 * Date: 8/7/13
 * Description: This class is a popup dialog used to insert a record (transaction).
 */

package com.whereone.groupWallet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


	
public class RecordConfirmDialog extends DialogFragment {
		
	private onSelect listener;
	
	public static RecordConfirmDialog newInstance(String userFirstName, String userLastName, 
			String username, String amount, Boolean owe) {
		RecordConfirmDialog frag = new RecordConfirmDialog();
        Bundle args = new Bundle();
        args.putString("firstname", userFirstName);
        args.putString("lastname", userLastName);
        args.putString("username", username);
        args.putString("amount", amount);
        args.putBoolean("owe", owe);
        frag.setArguments(args);
        return frag;
    }
	
	public void setSelectListener(onSelect _selectListener) {
        this.listener = _selectListener;
    }
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        String firstname = getArguments().getString("firstname");
        String lastname = getArguments().getString("lastname");
        String username = getArguments().getString("username");
        String amount = getArguments().getString("amount");
        Boolean owe = getArguments().getBoolean("owe");
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle("Confirm Record");
        
        if(owe){
	        final View view = inflater.inflate(R.layout.insert_record_dialog_owe, null);
	        
	        TextView user = (TextView) view.findViewById(R.id.dialog_owe_user);
	        TextView row2 = (TextView) view.findViewById(R.id.dialog_owe_row2);
	        TextView row3 = (TextView) view.findViewById(R.id.dialog_owe_row3);
	        
	        
	        user.setText(firstname + ' ' + lastname + " (" + username + ")");
	        row2.setText("spent " + amount);
	        row3.setText("on you");
	        
	        builder.setView(view);
        }
        else{
        	final View view = inflater.inflate(R.layout.insert_record_dialog_owed, null);
        	
        	TextView row1 = (TextView) view.findViewById(R.id.dialog_owed_row1);
	        TextView row2 = (TextView) view.findViewById(R.id.dialog_owed_row2);
	        TextView row3 = (TextView) view.findViewById(R.id.dialog_owed_row3);
	        
	        row1.setText("You spent");
	        row2.setText(amount + " on");
	        row3.setText(firstname + ' ' + lastname + " (" + username + ")");
        	
	        builder.setView(view);
        }
        builder.setPositiveButton(R.string.confirmRecord, new DialogInterface.OnClickListener() {
                  
        	public void onClick(DialogInterface dialog, int id) {
        		listener.comfirmPressed();	
        		}
            });
        builder.setNegativeButton(R.string.cancelRecord, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		listener.cancelPressed();
        		}
        	});
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	public interface onSelect{
		public void comfirmPressed();
		public void cancelPressed();
	}
}

