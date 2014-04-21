/**
 * Author: Jonathan Harrison
 * Date: 3.12.14
 * Description: 
 */

package com.whereone.groupWallet.customAdapters;

import java.text.NumberFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.models.User;

public class RelationshipsListAdapter extends ArrayAdapter<User> {
	
	public RelationshipsListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    
	    this.context = context;
	}

	private List<User> users;
	private TransactionsController transactionTable;
	private Integer walletId;
	private Integer userId;
	private Context context;
	private RelationshipListAdapterListener listener;
	
	public void setRelationshipListAdapterListener(RelationshipListAdapterListener listener){
		this.listener = listener;
	}
	
	public interface RelationshipListAdapterListener{
		public void rowClicked(User user);
		
	}

	public RelationshipsListAdapter(Context context, int resource, List<User> _users, TransactionsController recordTable, Integer _userId, Integer _walletId) {

	    super(context, resource, _users);

	    this.users = _users;
	    this.transactionTable = recordTable;
	    this.walletId = _walletId;
	    this.userId = _userId;
	    this.context = context;
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    View v = convertView;

	    if (v == null) {

	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.relationship_row, null);

	    }

	    final User user = users.get(position);
	    
	    v.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				listener.rowClicked(user);
			}
	    	
	    });

	    if (user != null) {

	        TextView name = (TextView) v.findViewById(R.id.relationship_name); 
	        TextView fullName = (TextView) v.findViewById(R.id.relationship_fullName);
	        TextView owe = (TextView) v.findViewById(R.id.relationship_owe);
	        TextView owed = (TextView) v.findViewById(R.id.relationship_owed);
	        TextView total = (TextView) v.findViewById(R.id.relationship_total);

	        if (name != null) {
	        	name.setText( "@" + user.getUserName() );
	        }
	        if (fullName != null){
	        	fullName.setText( user.getFirstName() + " " + user.getLastName() );
	        }
	        Double Owe = 0.0;
	        Double Owed = 0.0;
	        
	        if (owe != null) {
	        	Owe = transactionTable.getOweUserWallet(userId, user.getUserID(), walletId);
	        	String formattedOwe = NumberFormat.getCurrencyInstance().format((Owe));
	            owe.setText( formattedOwe );
	        }
	        if (owed != null) {
	        	Owed = transactionTable.getOweUserWallet(user.getUserID(), userId, walletId);
	        	String formattedOwed = NumberFormat.getCurrencyInstance().format((Owed));
	            owed.setText( formattedOwed );
	        }
	        if (total != null) {
	        	String formatted = NumberFormat.getCurrencyInstance().format((Owed-Owe));
	        	total.setText( formatted );
	        	if((Owed - Owe) < 0){
	        		total.setTextColor(context.getResources().getColor(R.color.red));
	        	}
	        	else{
	        		total.setTextColor(context.getResources().getColor(R.color.black));
	        	}
	        }
	    }

	    return v;

	}
}
