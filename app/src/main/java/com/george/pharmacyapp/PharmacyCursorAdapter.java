package com.george.pharmacyapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.george.pharmacyapp.data.PharmacyContract;

/**
 * Created by farmaker1 on 27/05/2017.
 */

public class PharmacyCursorAdapter extends CursorAdapter {


    public PharmacyCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {


        final View listItemView = view;

        TextView name = (TextView) listItemView.findViewById(R.id.name);
        final TextView quantity = (TextView) listItemView.findViewById(R.id.quantity);
        TextView price = (TextView) listItemView.findViewById(R.id.price);
        Button salesButton = (Button) listItemView.findViewById(R.id.salesButton);

        String nameProduct = cursor.getString(cursor.getColumnIndexOrThrow(PharmacyContract.PharmacyEntry.COLUMN_NAME));
        final String quantityProduct = cursor.getString(cursor.getColumnIndexOrThrow(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY));
        String priceProduct = cursor.getString(cursor.getColumnIndexOrThrow(PharmacyContract.PharmacyEntry.COLUMN_PRICE));

        name.setText(nameProduct);
        quantity.setText(quantityProduct);
        price.setText(priceProduct);

        final int positionOfCurrentListItem = cursor.getInt(cursor.getColumnIndex(PharmacyContract.PharmacyEntry._ID));

        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ////////////////////////////////*Log.e("ButtonSales","Success");*/
                int quantityReduced = Integer.parseInt(quantityProduct);
                int quaReduced = 0;
                if (quantityReduced > 0) {
                    
                    quaReduced = quantityReduced - 1;
                }


                ContentValues values = new ContentValues();
                values.put(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY, quaReduced);

                    /*int listItemId = listItemView.get;*//*

                    View parentRow = (View) view.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);*/


                Uri listItemUri = ContentUris.withAppendedId(PharmacyContract.PharmacyEntry.CONTENT_URI, positionOfCurrentListItem);

                int rowsAffected = context.getContentResolver().update(listItemUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(context, "faili",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(context, "Success",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });


    }
}
