/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is a list view adapter for a list of records
 */

package com.whereone.groupWallet.customAdapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.whereone.groupWallet.controllers.UsersController;
import com.whereone.groupWallet.models.Record;
import com.whereone.groupwalletcake.R;
import com.whereone.groupwalletcake.R.id;
import com.whereone.groupwalletcake.R.layout;

public class RecordListAdapter extends ArrayAdapter<Record> {
	
	public RecordListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    
	}

	private List<Record> records;
	private UsersController userTable;

	public RecordListAdapter(Context context, int resource, UsersController _userTable, List<Record> _records) {

	    super(context, resource, _records);

	    this.records = _records;
	    this.userTable = _userTable;

	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    View v = convertView;

	    if (v == null) {

	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.record_row, null);

	    }

	    Record record = records.get(position);

	    if (record != null) {

	        TextView tt = (TextView) v.findViewById(R.id.record_name); 
	        TextView tt1 = (TextView) v.findViewById(R.id.record_amount);
	        TextView tt3 = (TextView) v.findViewById(R.id.record_description);
	        TextView dateView = (TextView) v.findViewById(R.id.record_date);

	        if (tt != null) {
	        	String owe = userTable.getUserFromId(record.getOweId());
	        	String owed = userTable.getUserFromId(record.getOwedId());
	        	tt.setText("OWE: " + owe + " OWED: " + owed);
	        	
	        }
	        if (tt1 != null) {

	            tt1.setText(record.getAmount().toString());
	        }
	        if (tt3 != null) {
	            tt3.setText(record.getComments());
	        }
	        if (dateView != null) {
	        	dateView.setText(record.getDate().toString());
	        }
	    }

	    return v;

	}
}
