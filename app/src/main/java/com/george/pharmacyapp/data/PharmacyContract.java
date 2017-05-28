package com.george.pharmacyapp.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class PharmacyContract  {

    private PharmacyContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.pharmacy";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    public static final class PharmacyEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // Name of database table for pharmacy products
        public final static String TABLE_NAME = "products";

        //Unique id for every pharmacy prduct
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
