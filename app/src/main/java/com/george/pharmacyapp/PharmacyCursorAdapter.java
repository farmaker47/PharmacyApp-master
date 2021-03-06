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

    //creating the constructor
    public PharmacyCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        //inflating the list item view
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        final View listItemView = view;

        //finding the views and store them into variables
        TextView name = (TextView) listItemView.findViewById(R.id.name);
        final TextView quantity = (TextView) listItemView.findViewById(R.id.quantity);
        TextView price = (TextView) listItemView.findViewById(R.id.price);
        Button salesButton = (Button) listItemView.findViewById(R.id.salesButton);

        //finding dummy views and store to variable
        TextView dummyNameText = (TextView)listItemView.findViewById(R.id.dummyNameText);
        TextView dummyPriceText = (TextView)listItemView.findViewById(R.id.dummyPriceText);
        TextView dummyQuantityText = (TextView)listItemView.findViewById(R.id.dummyQuantityText);
        TextView euro = (TextView)listItemView.findViewById(R.id.euro);


        //calling the cursor to fetch the data from the columns of the database
        final String nameProduct = cursor.getString(cursor.getColumnIndexOrThrow(PharmacyContract.PharmacyEntry.COLUMN_NAME));
        final String quantityProduct = cursor.getString(cursor.getColumnIndexOrThrow(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY));
        final String priceProduct = cursor.getString(cursor.getColumnIndexOrThrow(PharmacyContract.PharmacyEntry.COLUMN_PRICE));

        //Setting text to dummy textviews
        dummyNameText.setText("Name:  ");
        dummyPriceText.setText("Price:  ");
        dummyQuantityText.setText("Quantity:  ");
        euro.setText(" €");

        //seting the text to the views..
        //Because atthe MainActivity we dont have an ImageView in the list item,we dont use one here
        name.setText(nameProduct);
        quantity.setText(quantityProduct);
        price.setText(priceProduct);

        //Getting the id of the specific row to use it in the onclick listener of the button
        final int positionOfCurrentListItem = cursor.getInt(cursor.getColumnIndex(PharmacyContract.PharmacyEntry._ID));

        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int quantityReduced = Integer.parseInt(quantityProduct);
                int quaReduced = 0;
                if (quantityReduced > 0) {

                    quaReduced = quantityReduced - 1;
                }

                ContentValues values = new ContentValues();
                values.put(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY, quaReduced);
                values.put(PharmacyContract.PharmacyEntry.COLUMN_NAME, nameProduct);
                values.put(PharmacyContract.PharmacyEntry.COLUMN_PRICE, priceProduct);

                //creating an uri depending the specific content uri and the current row id
                Uri listItemUri = ContentUris.withAppendedId(PharmacyContract.PharmacyEntry.CONTENT_URI, positionOfCurrentListItem);

                int rowsAffected = context.getContentResolver().update(listItemUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(context, "Fail Updating Quantity",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(context, "Success Updating Quantity",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });


    }
}
