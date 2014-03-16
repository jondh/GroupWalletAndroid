/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is a list view adapter for a list of records
 */

package com.whereone.groupWallet.customAdapters;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.models.Wallet;
import com.whereone.groupwalletcake.R;

public class WalletListAdapter extends ArrayAdapter<Wallet> {
	
	public WalletListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    
	}

	private List<Wallet> wallets;
	private TransactionsController transactionTable;
	private Integer userId;

	public WalletListAdapter(Context context, int resource, List<Wallet> _wallets, TransactionsController recordTable, Integer userId) {

	    super(context, resource, _wallets);

	    this.wallets = _wallets;
	    this.transactionTable = recordTable;
	    this.userId = userId;
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    View v = convertView;

	    if (v == null) {

	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.wallet_row, null);

	    }

	    Wallet wallet = wallets.get(position);

	    if (wallet != null) {

	    	
	    	TextView name = (TextView) v.findViewById(R.id.wallet_name); 
	        TextView owe = (TextView) v.findViewById(R.id.wallet_owe);
	        TextView owed = (TextView) v.findViewById(R.id.wallet_owed);
	        TextView total = (TextView) v.findViewById(R.id.wallet_total);

	        if (name != null) {
	        	String wname = wallet.getName();
	        	name.setText(wname);
	        }
	        Double Owe = 0.0;
	        Double Owed = 0.0;
	        DecimalFormat df = new DecimalFormat("#.##");
	        if (owe != null) {
	        	Owe = transactionTable.getOweWallet(userId, wallet.getID());
	            owe.setText("Spent on you: " + df.format(Owe));
	        }
	        if (owed != null) {
	        	Owed = transactionTable.getOwedWallet(userId, wallet.getID());
	            owed.setText("You have spent: " + df.format(Owed));
	        }
	        if (total != null) {
	        	if((Owed - Owe) > 0){
	        		total.setText("You are owed: " + df.format(Owed-Owe));
	        	}
	        	else if((Owed - Owe) < 0){
	        		total.setText("You owe: " + df.format(Owe-Owed));
	        	}
	        	else{
	        		total.setText("The debts are cleared");
	        	}
	        }
	    }

	    return v;

	}
}
