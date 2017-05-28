package com.george.pharmacyapp.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class PharmacyContract {
    //Contract class to store all the names of the tables and the names of the columns as Constants

    //Creating an empty constructor so there are no errors when we refer to it or prevent accidentally instantiating the contract class
    private PharmacyContract() {
    }

    //Creting the content authority
    public static final String CONTENT_AUTHORITY = "com.example.android.pharmacy";

    //Creating the base uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //creating a string to add when needed to create a specific path
    public static final String PATH_PRODUCTS = "products";

    //Public class to store the name of the table and the columns that belong to it
    public static final class PharmacyEntry implements BaseColumns {

        //Uri to be used for accessing the table "products" of the database
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // Name of database table for pharmacy products
        public final static String TABLE_NAME = "products";

        //Unique id for every pharmacy product
        public final static String _ID = BaseColumns._ID;

        //Name of the product of type TEXT
        public final static String COLUMN_NAME = "name";

        //Quantity of the product of type INTEGER
        public final static String COLUMN_QUANTITY = "quantity";

        //Price of the product of type INTEGER
        public final static String COLUMN_PRICE = "price";

        //Image of the product.We will use the "address" of the product in the memory storage of the
        //phone.It will be of type TEXT
        public final static String COLUMN_IMAGE = "image";
    }
}
