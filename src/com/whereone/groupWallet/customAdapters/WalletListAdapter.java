/**
 * Author: Jonathan Harrison
 * Date: 8/25/13
 * Description: This class is a list view adapter for a list of records
 */

package com.whereone.groupWallet.customAdapters;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.whereone.groupWallet.R;
import com.whereone.groupWallet.controllers.TransactionsController;
import com.whereone.groupWallet.models.Wallet;

public class WalletListAdapter extends ArrayAdapter<Wallet> {
	
	public WalletListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    
	    this.context = context;
	}

	private List<Wallet> wallets;
	private Integer userId;
	private Context context;

	public WalletListAdapter(Context context, int resource, List<Wallet> _wallets, Integer userId) {

	    super(context, resource, _wallets);

	    this.wallets = _wallets;
	    this.userId = userId;
	    this.context = context;
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

	    	
	    	AutoResizeTextView name = (AutoResizeTextView) v.findViewById(R.id.wallet_name); 
	        TextView total = (TextView) v.findViewById(R.id.wallet_total);
	        TextView owe = (TextView) v.findViewById(R.id.wallet_owe);
	        TextView owed = (TextView) v.findViewById(R.id.wallet_owed);
	        TextView totalWallet = (TextView) v.findViewById(R.id.wallet_total_wallet);

	        if (name != null) {
	        	String wname = wallet.getName();
	        	
	        	name.setText(wname.trim());
	        	
	        	//name.setTextSize(30);
	        	//name.resizeText();
	        }
	        Double Owe = 0.0;
	        Double Owed = 0.0;
	        Double totalAmount = 0.0;
	        DecimalFormat df = new DecimalFormat("#0.00");
	        if (owe != null) {
	        	Owe = TransactionsController.getInstance().getOweWallet(userId, wallet.getID());
	            owe.setText("$" + df.format(Owe).trim());
	        }
	        if (owed != null) {
	        	Owed = TransactionsController.getInstance().getOwedWallet(userId, wallet.getID());
	            owed.setText("$" + df.format(Owed).trim());
	        }
	        if (total != null) {
	        	
				String formatted = NumberFormat.getCurrencyInstance().format((Owed-Owe));
				total.setText( formatted );
				if((Owed < Owe)){
					total.setTextColor(context.getResources().getColor(R.color.red));
				}
	        }
	        if(totalWallet != null){
	        	totalAmount = TransactionsController.getInstance().getWalletTotal(wallet.getID());
	        	totalWallet.setText("$" + df.format(totalAmount).trim());
	        }
	    }

	    return v;

	}
}
