package com.george.pharmacyapp;


import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.george.pharmacyapp.data.PharmacyContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private PharmacyCursorAdapter mCursorAdapter;

    private static final int PRODUCT_LOADER = 3;
    private ListView list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditPharmacyItem.class);
                /*Uri currentPetUri = PharmacyContract.PharmacyEntry.CONTENT_URI;

                intent.setData(currentPetUri);*/
                startActivity(intent);
            }
        });

        list = (ListView)findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        list.setEmptyView(emptyView);

        mCursorAdapter = new PharmacyCursorAdapter(this,null);

        list.setAdapter(mCursorAdapter);

        getSupportLoaderManager().initLoader(PRODUCT_LOADER,null,this);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                Intent intent = new Intent(MainActivity.this,EditPharmacyItem.class);

                Uri currentProductUri = ContentUris.withAppendedId(PharmacyContract.PharmacyEntry.CONTENT_URI,id);

                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_all) {

            int itemCount = list.getAdapter().getCount();
            if(itemCount==0){
                Toast.makeText(this,"No items to delete",Toast.LENGTH_SHORT).show();
                return true;
            }else{
                showDeleteConfirmationDialog();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog(){
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("DELETE All PRODUCTS?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        int rowsAffected = getContentResolver().delete(PharmacyContract.PharmacyEntry.CONTENT_URI, null, null);

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, "No items to delete!",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, "Success",
                    Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PharmacyContract.PharmacyEntry._ID,
                PharmacyContract.PharmacyEntry.COLUMN_NAME,
                PharmacyContract.PharmacyEntry.COLUMN_PRICE,
                PharmacyContract.PharmacyEntry.COLUMN_QUANTITY };

        // Perform a query on the pets table
        /*Cursor cursor = db.query(
                PetEntry.TABLE_NAME,   // The table to query
                projection,            // The columns to return
                null,                  // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order*/


        return new CursorLoader(this,
                PharmacyContract.PharmacyEntry.CONTENT_URI,
                projection,
                null,null,null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
