package com.george.pharmacyapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.george.pharmacyapp.data.PharmacyContract;
import com.george.pharmacyapp.data.PharmacyDBHelper;

import static com.george.pharmacyapp.data.PharmacyContract.CONTENT_AUTHORITY;
import static com.george.pharmacyapp.data.PharmacyContract.PATH_PRODUCTS;

/**
 * Created by farmaker1 on 27/05/2017.
 */

public class PharmacyProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PharmacyProvider.class.getSimpleName();

    /**
     * Initialize the database helper object.
     */
    private PharmacyDBHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCT = 100;

    /**
     * URI matcher code for the content URI for a single product in the product table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS, PRODUCT);

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS + "/#", PRODUCT_ID);

    }


    @Override
    public boolean onCreate() {
        mDbHelper = new PharmacyDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                cursor = database.query(
                        PharmacyContract.PharmacyEntry.TABLE_NAME,   // The table to query
                        projection,            // The columns to return
                        selection,                  // The columns for the WHERE clause
                        selectionArgs,                  // The values for the WHERE clause
                        null,                  // Don't group the rows
                        null,                  // Don't filter by row groups
                        sortOrder);                   // The sort order
                break;
            case PRODUCT_ID:
                selection = PharmacyContract.PharmacyEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table where the _id equals a number to return a
                // Cursor containing that row of the table.
                cursor = database.query(PharmacyContract.PharmacyEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown " + uri);
        }

        //if the uri changes,we know we have to update cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        String name = values.getAsString(PharmacyContract.PharmacyEntry.COLUMN_NAME);
        if (name.equals("")) {
            //Sanity check for name
            Toast.makeText(getContext(), "Product requires a name", Toast.LENGTH_SHORT).show();
        }

        String price = values.getAsString(PharmacyContract.PharmacyEntry.COLUMN_PRICE);
        if (price.equals("")) {
            //Sanity check for price
            Toast.makeText(getContext(), "Product requires a price", Toast.LENGTH_LONG).show();
        }

        String quantity = values.getAsString(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY);
        if (quantity.equals("")) {
            //Sanity check for quantity
            Toast.makeText(getContext(), "Product requires quantity", Toast.LENGTH_LONG).show();
        }

        String image = values.getAsString(PharmacyContract.PharmacyEntry.COLUMN_IMAGE);
        if (image.equals("")) {
            //Sanity check for image
            Toast.makeText(getContext(), "Product requires image", Toast.LENGTH_LONG).show();
        }

        //Product has to have all the information to complete insertion
        if (image.equals("") || quantity.equals("") || price.equals("") || name.equals("")) {
            return null;
        }

        // Insert the new product with the given values
        long id = database.insert(PharmacyContract.PharmacyEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //notify all listeners that uri has changed
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                rowsDeleted = database.delete(PharmacyContract.PharmacyEntry.TABLE_NAME, selection, selectionArgs);
                // Delete all rows that match the selection and selection args
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = PharmacyContract.PharmacyEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(PharmacyContract.PharmacyEntry.TABLE_NAME, selection, selectionArgs);

                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PharmacyContract.PharmacyEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);

        }
    }

    //update products
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        String name = "";
        String quantity = "";
        String price = "";
        String image = "";

        // If the {@link PharmacyEntry#COLUMN_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PharmacyContract.PharmacyEntry.COLUMN_NAME)) {
            name = values.getAsString(PharmacyContract.PharmacyEntry.COLUMN_NAME);
            if (name.equals("")) {
                Toast.makeText(getContext(), "Product requires name", Toast.LENGTH_SHORT).show();
            }
        }

        // If the {@link PharmacyEntry#COLUMN_PRICE} key is present,
        // check that the price value is not null.
        if (values.containsKey(PharmacyContract.PharmacyEntry.COLUMN_PRICE)) {
            price = values.getAsString(PharmacyContract.PharmacyEntry.COLUMN_PRICE);
            if (price.equals("")) {
                Toast.makeText(getContext(), "No Prici", Toast.LENGTH_SHORT).show();
            }
        }

        // If the {@link PharmacyEntry#COLUMN_QUANTITY} key is present,
        // check that the quantity value is not null.
        if (values.containsKey(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY)) {
            quantity = values.getAsString(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY);
            if (quantity.equals("")) {
                Toast.makeText(getContext(), "No quantiti", Toast.LENGTH_SHORT).show();
            }
        }

        // If the {@link PharmacyEntry#COLUMN_IMAGE} key is present,
        // check that the image value is not null.
        if (values.containsKey(PharmacyContract.PharmacyEntry.COLUMN_IMAGE)) {
            image = values.getAsString(PharmacyContract.PharmacyEntry.COLUMN_IMAGE);
            if (image.equals("")) {
                Toast.makeText(getContext(), "No Imagi", Toast.LENGTH_SHORT).show();
            }
        }

        //Allinformation has to be present to update product
        //Because there is a sanity check for image presence and there is no method to delete the image from the imageview
        // we dont add sanity check here(we assume there is an image already)
        if (quantity.equals("") || price.equals("") || name.equals("")) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PharmacyContract.PharmacyEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;

    }

}
