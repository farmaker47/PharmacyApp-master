package com.george.pharmacyapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.george.pharmacyapp.data.DatabaseUtil;
import com.george.pharmacyapp.data.PharmacyContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditPharmacyItem extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Initializing the views
    private EditText mEditName, mEditQuantity, mEditPrice;
    private ImageView mImage;
    private ImageButton mMinusButton, mPlusButton;
    private Button mOrderButton, mDeleteButton;

    //Giving the loader a number so to call it at initialization
    private final int PHARMACY_EDIT_LOADER = 0;

    //Making two final static integers and giving a number to be used in starting activity for result and for sending an e-mail
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int SEND_MAIL_REQUEST = 1;

    //A private Uri to determine whether we are in edit or savng a new product
    private Uri mCurrentPharmacyUri;

    //Log tag to use it for debug
    private static final String LOG_TAG = EditPharmacyItem.class.getSimpleName();

    //Uri thatis used vi ImageView to load the imagefrom gallery
    private Uri imageUri;

    //Making quantity a global variable
    private int quantity = 0;

    //Global private strings
    private String editName, editPrice, editQuantity, editImage;

    //String to use it on onsaveinstant state of the image
    private static final String STATE_URI = "STATE_URI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pharmacy_item);

        //Finding,casting and storing the views to variables
        mEditName = (EditText) findViewById(R.id.editTextName);
        mEditQuantity = (EditText) findViewById(R.id.editTextQuantity);
        mEditPrice = (EditText) findViewById(R.id.editTextPrice);
        mImage = (ImageView) findViewById(R.id.image);
        mOrderButton = (Button) findViewById(R.id.orderButton);
        mDeleteButton = (Button) findViewById(R.id.deleteButton);
        mMinusButton = (ImageButton) findViewById(R.id.minusButton);
        mPlusButton = (ImageButton) findViewById(R.id.plusButton);

        //Getting the intent and the data from the MainActivity
        Intent intent = getIntent();
        mCurrentPharmacyUri = intent.getData();

        //Determine if the the EditActivity is on edit or saving a new product
        if (mCurrentPharmacyUri == null) {
            setTitle("Add a Product");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
            //Also making the deleteButton invisible
            mDeleteButton.setVisibility(View.INVISIBLE);

        } else {
            setTitle("Edit Product");
            //Initializing the Loader in edit product mode
            getSupportLoaderManager().initLoader(PHARMACY_EDIT_LOADER, null, this);
        }

        //Setting an onClickListener at ImageView to get image from gallery
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFromGallery();
            }
        });

        //Setting an onClickListener at button that decreases quantity
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextCurrentQuantity = mEditQuantity.getText().toString().trim();
                //if quantity is 0 then stop so not to get into negative numbers
                if (editTextCurrentQuantity.equals("") || editTextCurrentQuantity.equals("0")) {
                    quantity = 0;
                    return;
                } else {
                    //if quantity is above 0 then decrease by one
                    quantity = Integer.parseInt(editTextCurrentQuantity);
                    quantity -= 1;
                }
                display(quantity);

            }
        });

        //Setting an onClickListener at button that increases quantity
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextCurrentQuantity = mEditQuantity.getText().toString().trim();
                if (editTextCurrentQuantity.equals("")) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(editTextCurrentQuantity);
                }
                quantity += 1;
                //method to display the quantity to the editText
                display(quantity);
            }
        });

        //Setting an onClickListener to the orderButton
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        //Setting an onClickListener to the deleteButton
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

    }

    //method to display the quantity to the editText
    private void display(int quantity) {
        mEditQuantity.setText(String.valueOf(quantity));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentPharmacyUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //delete function at delete button in options menu
        if (id == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }

        //save function of save button
        //In addition we store the db to an externalfile of phone's memory because some phones are not rooted
        if (id == R.id.action_save) {
            insertProduct();
            DatabaseUtil.copyDatabaseToExtStg(this);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    //Saving the instance state of the image
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (imageUri != null)
            outState.putString(STATE_URI, imageUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            imageUri = Uri.parse(savedInstanceState.getString(STATE_URI));

            ViewTreeObserver viewTreeObserver = mImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImage.setImageBitmap(getBitmapFromUri(imageUri));
                }
            });
        }
    }

    //Starting the loader
    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        // The projection contains all the columns since the activity displays all the product data
        String[] projection = {
                PharmacyContract.PharmacyEntry._ID,
                PharmacyContract.PharmacyEntry.COLUMN_NAME,
                PharmacyContract.PharmacyEntry.COLUMN_QUANTITY,
                PharmacyContract.PharmacyEntry.COLUMN_PRICE,
                PharmacyContract.PharmacyEntry.COLUMN_IMAGE};

        return new CursorLoader(this,   // Parent activity context
                mCurrentPharmacyUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PharmacyContract.PharmacyEntry.COLUMN_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(PharmacyContract.PharmacyEntry.COLUMN_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(PharmacyContract.PharmacyEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            editName = cursor.getString(nameColumnIndex);
            editImage = cursor.getString(imageColumnIndex);
            editQuantity = cursor.getString(quantityColumnIndex);
            editPrice = cursor.getString(priceColumnIndex);

            // Update the views on the screen with the values from the database
            mEditName.setText(editName);
            mEditQuantity.setText(editQuantity);
            mEditPrice.setText(editPrice);

            imageUri = Uri.parse(editImage);
            mImage.setImageBitmap(getBitmapFromUri(imageUri));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //Insert a product or update one
    private void insertProduct() {

        //determine if the activity is in editing or saving mode
        if (mCurrentPharmacyUri == null) {
            String nameString = mEditName.getText().toString().trim();
            String quantityString = mEditQuantity.getText().toString().trim();
            String priceString = mEditPrice.getText().toString().trim();
            String imageString;

            if (imageUri == null) {
                imageString = "";
            } else {
                imageString = imageUri.toString();
            }

            ContentValues values = new ContentValues();
            values.put(PharmacyContract.PharmacyEntry.COLUMN_NAME, nameString);
            values.put(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY, quantityString);
            values.put(PharmacyContract.PharmacyEntry.COLUMN_PRICE, priceString);
            values.put(PharmacyContract.PharmacyEntry.COLUMN_IMAGE, imageString);


            Uri uri = getContentResolver().insert(PharmacyContract.PharmacyEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (uri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Fail saving the product",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Success saving the product",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //if uri from MainActivity is not null then Update product
            String nameString = mEditName.getText().toString().trim();
            String quantityString = mEditQuantity.getText().toString().trim();
            String priceString = mEditPrice.getText().toString().trim();
            String imageString;

            if (imageUri == null) {
                imageString = "";
            } else {
                imageString = imageUri.toString();
            }

            ContentValues values = new ContentValues();
            values.put(PharmacyContract.PharmacyEntry.COLUMN_NAME, nameString);
            values.put(PharmacyContract.PharmacyEntry.COLUMN_QUANTITY, quantityString);
            values.put(PharmacyContract.PharmacyEntry.COLUMN_PRICE, priceString);
            values.put(PharmacyContract.PharmacyEntry.COLUMN_IMAGE, imageString);

            int rowsAffected = getContentResolver().update(mCurrentPharmacyUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "Fail updating the product",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "Success updating the product",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    //Creating a confirmation delete dialog
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("DELETE PRODUCT ??");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Method to delete a product
    private void deleteProduct() {

        int rowsAffected = getContentResolver().delete(mCurrentPharmacyUri, null, null);

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, "Fail  deleting the product",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, "Success deleting the product",
                    Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    //Method to select image from gallery
    private void getImageFromGallery() {
        Intent intent;

        //determine if mobile phone is below sdk 19
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        //Starting the gallery to chooose image
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (data != null) {
                imageUri = data.getData();
                Log.i(LOG_TAG, "Uri: " + imageUri.toString());

                //Setting the image to the imageview depending the specific uri
                mImage.setImageBitmap(getBitmapFromUri(imageUri));
            }
        }
    }

    //Method to create bitmap from uri
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    //Methd to sen an e-mail to the supplier
    private void sendEmail() {
        if (editName != null) {
            String subject = "Order details";
            String stream = "Hello Sir! Please send us the below order \n\n"
                    + "Name:\t" + editName + "\n"
                    + "Quantity:\t" + editQuantity + "\n"
                    + "Price:\t" + editPrice + "\n";

            Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                    .setStream(imageUri)
                    .setSubject(subject)
                    .setText(stream)
                    .getIntent();

            // Provide read access
            shareIntent.setData(imageUri);
            shareIntent.setType("message/rfc822");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (Build.VERSION.SDK_INT < 21) {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            } else {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }


            startActivityForResult(Intent.createChooser(shareIntent, "Share with"), SEND_MAIL_REQUEST);

        } else {
            Toast.makeText(this, "No details available", Toast.LENGTH_SHORT).show();
            return;
        }
    }

}