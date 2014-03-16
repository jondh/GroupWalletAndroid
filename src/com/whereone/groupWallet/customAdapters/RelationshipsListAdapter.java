/**
 * Author: Jonathan Harrison
 * Date: 3.12.14
 * Description: 
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
import com.whereone.groupWallet.models.User;
import com.whereone.groupwalletcake.R;

public class RelationshipsListAdapter extends ArrayAdapter<User> {
	
	public RelationshipsListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    
	}

	private List<User> users;
	private TransactionsController transactionTable;
	private Integer walletId;
	private Integer userId;

	public RelationshipsListAdapter(Context context, int resource, List<User> _users, TransactionsController recordTable, Integer _userId, Integer _walletId) {

	    super(context, resource, _users);

	    this.users = _users;
	    this.transactionTable = recordTable;
	    this.walletId = _walletId;
	    this.userId = _userId;
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    View v = convertView;

	    if (v == null) {

	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.relationship_row, null);

	    }

	    User user = users.get(position);

	    if (user != null) {

	        TextView name = (TextView) v.findViewById(R.id.relationship_name); 
	        TextView owe = (TextView) v.findViewById(R.id.relationship_owe);
	        TextView owed = (TextView) v.findViewById(R.id.relationship_owed);
	        TextView total = (TextView) v.findViewById(R.id.relationship_total);

	        if (name != null) {
	        	name.setText(user.getFirstName() + " " + user.getLastName() + " (@" + user.getUserName() + ")");
	        }
	        Double Owe = 0.0;
	        Double Owed = 0.0;
	        DecimalFormat df = new DecimalFormat("#.##");
	        if (owe != null) {
	        	Owe = transactionTable.getOweUserWallet(userId, user.getUserID(), walletId);
	            owe.setText("Spent on you: " + df.format(Owe));
	        }
	        if (owed != null) {
	        	Owed = transactionTable.getOweUserWallet(user.getUserID(), userId, walletId);
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
