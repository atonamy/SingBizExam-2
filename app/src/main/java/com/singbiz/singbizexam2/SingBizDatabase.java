package com.singbiz.singbizexam2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by archie on 16/2/16.
 */
    public class SingBizDatabase extends SQLiteOpenHelper {

    // Database Info
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "exam2_singbiz.db";

    // Table Names
    public static final String TABLE_PRODUCTS = "Products";
    public static final String TABLE_CATEGORIES = "Categories";

    //View Names
    public static final String VIEW_PRODUCTS = "ViewProducts";


    // Products Table Columns
    public static final String KEY_PRODUCT_ID = "_id";
    public static final String KEY_PRODUCT_REFERENCE = "reference";
    public static final String KEY_PRODUCT_NAME = "product_name";
    public static final String KEY_PRODUCT_PRICE= "price";
    public static final String KEY_PRODUCT_QUANTITY = "quantity";
    public static final String KEY_PRODUCT_DISCOUNT = "discount";

    // Categories Table Columns
    public static final String KEY_CATEGORY_TYPE = "category_type";
    public static final String KEY_CATEGORY_PRODUCT_REFERENCE = "product_id";

    private static final String CREATE_PRODUCTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTS +
            "(" +
                KEY_PRODUCT_ID + " INTEGER PRIMARY KEY," +
                KEY_PRODUCT_REFERENCE + "  TEXT NOT NULL," +
                KEY_PRODUCT_NAME + " TEXT NOT NULL," +
                KEY_PRODUCT_PRICE + " REAL NOT NULL," +
                KEY_PRODUCT_QUANTITY + " INTEGER NULL," +
                KEY_PRODUCT_DISCOUNT + " REAL NULL" +
            ")";

    private static final String CREATE_CATEGORIES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES +
            "(" +
                KEY_CATEGORY_TYPE + " INTEGER NOT NULL," +
                KEY_CATEGORY_PRODUCT_REFERENCE + " INTEGER NOT NULL CONSTRAINT fk_" + TABLE_PRODUCTS +
                    "_id REFERENCES " + TABLE_PRODUCTS + "(" + KEY_PRODUCT_ID + ") ON DELETE CASCADE" +
            ")";

    private static final String CREATE_PRODUCTS_VIEW = "CREATE VIEW IF NOT EXISTS " + VIEW_PRODUCTS + " AS SELECT " +
            TABLE_PRODUCTS + "." + KEY_PRODUCT_ID + "," +
            TABLE_PRODUCTS + "." + KEY_PRODUCT_REFERENCE + "," +
            TABLE_CATEGORIES + "." + KEY_CATEGORY_TYPE + "," +
            TABLE_PRODUCTS + "." + KEY_PRODUCT_NAME + "," +
            TABLE_PRODUCTS + "." + KEY_PRODUCT_PRICE + "," +
            TABLE_PRODUCTS + "." + KEY_PRODUCT_QUANTITY + "," +
            TABLE_PRODUCTS + "." + KEY_PRODUCT_DISCOUNT +
            " FROM " + TABLE_PRODUCTS + " " + TABLE_PRODUCTS +
            " LEFT JOIN " + TABLE_CATEGORIES + " " + TABLE_CATEGORIES + " ON " +
                TABLE_CATEGORIES + "." + KEY_CATEGORY_PRODUCT_REFERENCE + " = " + TABLE_PRODUCTS + "." + KEY_PRODUCT_ID;

    private static final String DELETE_VIEW_PRODUCTS = "DROP VIEW " + VIEW_PRODUCTS;
    private static final String DELETE_CATEGORIES = "DROP TABLE " + TABLE_CATEGORIES;
    private static final String DELETE_PRODUCTS = "DROP TABLE " + TABLE_PRODUCTS;

    private static SingBizDatabase sInstance = null;

    /*public static synchronized SingBizDatabase getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new SingBizDatabase(context.getApplicationContext());
        }
        return sInstance;
    }*/

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    public SingBizDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PRODUCTS_TABLE);
        db.execSQL(CREATE_CATEGORIES_TABLE);
        db.execSQL(CREATE_PRODUCTS_VIEW);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL(DELETE_VIEW_PRODUCTS);
            db.execSQL(DELETE_CATEGORIES);
            db.execSQL(DELETE_PRODUCTS);
            onCreate(db);
        }
    }

    /*@Override
    public void close() {
        super.close();
        sInstance = null;
    }*/

    public boolean addProduct(ProductItem productItem) {
        if(productItem == null)
            return false;
        SQLiteDatabase db = getWritableDatabase();
        boolean success = true;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PRODUCT_ID, (Integer) null);
            values.put(KEY_PRODUCT_REFERENCE, productItem.productReference);
            values.put(KEY_PRODUCT_NAME, productItem.productName);
            values.put(KEY_PRODUCT_PRICE, productItem.productPrice);

            db.insertOrThrow(TABLE_PRODUCTS, null, values);
            db.setTransactionSuccessful();
        }
        catch(Exception e) {
            success =  false;
        }
        db.endTransaction();

        return success;
    }

    public boolean deleteProduct(ProductItem productItem) {
        if(productItem == null)
            return false;
        SQLiteDatabase db = getWritableDatabase();
        boolean success = true;
        db.beginTransaction();
        try {
            success = (db.delete(TABLE_PRODUCTS, KEY_PRODUCT_ID + "=" + productItem.productId, null) > 0);
            db.setTransactionSuccessful();
        }
        catch(Exception e) {
            success =  false;
        }
        db.endTransaction();

        return success;
    }

    /*public boolean addCategory(ProductItem productItem) {
        if(productItem == null)
            return false;
        SQLiteDatabase db = getWritableDatabase();
        boolean success = true;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_CATEGORY_TYPE, productItem.productCategory.ordinal());
            values.put(KEY_CATEGORY_PRODUCT_REFERENCE, productItem.productId);

            db.insertOrThrow(TABLE_CATEGORIES, null, values);
            db.setTransactionSuccessful();
        }
        catch(Exception e) {
            success = false;
        }
        db.endTransaction();

        return success;
    }

    public boolean deleteCategory(ProductItem productItem) {
        if(productItem == null)
            return false;
        SQLiteDatabase db = getWritableDatabase();
        boolean success = true;
        db.beginTransaction();
        try {
            success = (db.delete(TABLE_CATEGORIES, KEY_CATEGORY_PRODUCT_REFERENCE + "=" +
                            productItem.productId + " AND " + KEY_CATEGORY_TYPE + "=" +
                            productItem.productCategory.ordinal(), null) > 0);
            db.setTransactionSuccessful();
        }
        catch(Exception e) {
            success =  false;
        }
        db.endTransaction();

        return success;
    }*/

    public boolean updateProductProperties(ProductItem productItem) {
        if(productItem == null)
            return false;
        SQLiteDatabase db = getWritableDatabase();
        boolean success = true;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PRODUCT_QUANTITY, productItem.getProductQuantity());
            values.put(KEY_PRODUCT_DISCOUNT, productItem.getProductDiscount());

            success = (db.update(TABLE_PRODUCTS, values, KEY_PRODUCT_ID + "=" +
                    productItem.productId, null) > 0);
            db.setTransactionSuccessful();
        }
        catch(Exception e) {
            success = false;
        }
        db.endTransaction();

        return success;
    }

    public ProductItem getProduct(ProductItem productItem) {
        if(productItem == null)
            return null;
        String query = "SELECT * FROM " + VIEW_PRODUCTS;
        if(productItem.productCategory != ProductItem.PRODUCT_CATEGORY.NONE)
            query += " WHERE " + KEY_CATEGORY_TYPE + " = " + productItem.productCategory.ordinal() +
                    " AND " + KEY_PRODUCT_ID + "=" +  + productItem.productId;
        else
            query += " WHERE " + KEY_CATEGORY_TYPE + " IS NULL" +
                    " AND " + KEY_PRODUCT_ID + "=" + productItem.productId;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            ProductItem product = populateProduct(cursor, null);
            cursor.close();
            return product;
        } else if(cursor != null)
            cursor.close();
        return null;
    }

    public List<ProductItem> getAllProducts(int take, int skip, String search) {
        String[] args = null;
        String query = "SELECT * FROM " + VIEW_PRODUCTS;
        if(search != null) {
            query += " WHERE " + KEY_PRODUCT_REFERENCE + " LIKE ? ESCAPE '#' OR " + KEY_PRODUCT_NAME + " LIKE ? ESCAPE '#'";
            search = search.replaceAll("%", "#%");
            search = search.replaceAll("_", "#_");
            search = search.replaceAll("#", "#^");
            args = new String[] {"%"+search+"%", "%"+search+"%"};
        }
        return getProducts(query, take, skip, false, args);
    }

    public List<ProductItem> getProductsByCategory(ProductItem.PRODUCT_CATEGORY category, int take, int skip, String search) {
        String[] args = null;
        String query = "SELECT * FROM " + VIEW_PRODUCTS;
        query += " WHERE " + KEY_CATEGORY_TYPE + "=" + category.ordinal();
        if(search != null) {
            query += " AND (" + KEY_PRODUCT_REFERENCE + " LIKE ? ESCAPE '#' OR " + KEY_PRODUCT_NAME + " LIKE ? ESCAPE '#')";
            search = search.replaceAll("%", "#%");
            search = search.replaceAll("_", "#_");
            search = search.replaceAll("#", "##");
            args = new String[] {"%"+search+"%", "%"+search+"%"};
        }
        return getProducts(query, take, skip, false, args);
    }

    public List<ProductItem> getCheckoutProducts(int take, int skip) {
        String query = "SELECT * FROM " + VIEW_PRODUCTS;
        query += " WHERE " + KEY_PRODUCT_QUANTITY + " IS NOT NULL";

        return getProducts(query, take, skip, true, null);
    }

    protected List<ProductItem> getProducts(String query, int take, int skip, boolean checkout, String[] args) {
        List<ProductItem> result = new ArrayList<>();

        query += " LIMIT " + take + " OFFSET " + skip;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, args);

        if(cursor.moveToFirst()) {
            do {
                ProductItem product = populateProduct(cursor, checkout);
                result.add(product);
            }while(cursor.moveToNext());
            cursor.close();
        } else if(cursor != null)
            cursor.close();

        return result;
    }

    protected ProductItem populateProduct(Cursor cursor, Boolean checkout) {

        Long category = cursor.getLong(cursor.getColumnIndex(KEY_CATEGORY_TYPE));
        Long quantity = cursor.getLong(cursor.getColumnIndex(KEY_PRODUCT_QUANTITY));
        Double discount = cursor.getDouble(cursor.getColumnIndex(KEY_PRODUCT_DISCOUNT));
        if(checkout == null)
            checkout = (quantity == null) ? false : true;

        ProductItem result = new ProductItem(cursor.getLong(cursor.getColumnIndex(KEY_PRODUCT_ID)),
                cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_REFERENCE)),
                (category == null) ? ProductItem.PRODUCT_CATEGORY.NONE : ProductItem.PRODUCT_CATEGORY.values()[category.intValue()],
                cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)),
                cursor.getDouble(cursor.getColumnIndex(KEY_PRODUCT_PRICE)),
                checkout.booleanValue()
        );

        if(quantity != null)
            result.setProductQuantity(quantity.intValue());
        if(discount != null)
            result.setProductDiscount(discount);

        return result;
    }
}
