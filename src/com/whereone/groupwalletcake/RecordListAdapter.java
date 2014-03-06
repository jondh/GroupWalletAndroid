/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is a list view adapter for a list of records
 */

package com.whereone.groupwalletcake;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.whereone.groupWallet.models.Record;

public class RecordListAdapter extends ArrayAdapter<Record> {
	
	public RecordListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    // TODO Auto-generated constructor stub
	}

	private List<Record> records;

	public RecordListAdapter(Context context, int resource, List<Record> _records) {

	    super(context, resource, _records);

	    this.records = _records;

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

	        if (tt != null) {
	            tt.setText(record.getUser());
	        }
	        if (tt1 != null) {

	            tt1.setText(record.getAmount().toString());
	        }
	        if (tt3 != null) {

	            tt3.setText(record.getComment());
	        }
	    }

	    return v;

	}
}
