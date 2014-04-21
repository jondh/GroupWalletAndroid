/**
 * Author: Jonathan Harrison
 * Date: 3/10/14
 * Description: 
 */

package com.whereone.groupWallet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


	
public class MessageDialog extends DialogFragment {
		
	private onSelect listener;
	
	public void setSelectListener(onSelect _selectListener) {
        this.listener = _selectListener;
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle("New Wallet");
        
	    final View view = inflater.inflate(R.layout.new_wallet_dialog, null);
	        
	    final EditText name = (EditText) view.findViewById(R.id.newWallet_text);
	        
        builder.setPositiveButton(R.string.confirmRecord, new DialogInterface.OnClickListener() {
                  
        	public void onClick(DialogInterface dialog, int id) {
        		listener.comfirmPressed(name.getText().toString());	
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

	public void show(android.app.FragmentManager fragmentManager,
			String string) {
		// TODO Auto-generated method stub
		
	}
	
	public interface onSelect{
		public void comfirmPressed(String name);
		public void cancelPressed();
	}
}

