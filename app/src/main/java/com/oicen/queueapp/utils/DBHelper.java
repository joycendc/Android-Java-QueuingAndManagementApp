package com.oicen.queueapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends SQLiteOpenHelper {
    static final  int DB_VERSION = 1;
    public static final String  CREATE_ITEM_TABLE = "CREATE TABLE " + DBContract.TABLE_NAME + "(id INTEGER PRIMARY KEY," + DBContract.NAME+" TEXT,"
            + DBContract.DESC+ " TEXT," + DBContract.PRICE + " TEXT," + DBContract.CAT_ID+ " INTEGER);";
    public static final String  CREATE_CAT_TABLE = "CREATE TABLE " + DBContract.CAT_TABLE_NAME + "(id INTEGER PRIMARY KEY," + DBContract.CAT_NAME +" TEXT);";
    public static final String DROP_ITEM_TABLE = "DROP TABLE IF EXISTS " + DBContract.TABLE_NAME;
    public static final String DROP_CAT_TABLE = "DROP TABLE IF EXISTS " + DBContract.CAT_TABLE_NAME;
    Context context;

    private static String DB_PATH = "";
    private SQLiteDatabase myDataBase;

    public DBHelper(Context context){
        super(context, DBContract.DB_NAME, null, DB_VERSION);
        this.context = context;
        DB_PATH = context.getDatabasePath(DBContract.DB_NAME).toString();
    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();

        if (dbExist) {
            // do nothing - database already exist
        }
        else {
            // By calling this method and
            // the empty database will be
            // created into the default system
            // path of your application
            // so we are gonna be able
            // to overwrite that database
            // with our database.
            this.getWritableDatabase();
            try {
                copyDataBase();
            }
            catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDataBase()
    {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }
        catch (SQLiteException e) {
            // database doesn't exist yet.
            Log.e("message", "" + e);
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    private void copyDataBase() throws IOException {
        // Open your local db as the input stream
        InputStream myInput  = context.getAssets().open(DBContract.DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the
        // inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        // Open the database
        String myPath = DB_PATH;
        myDataBase = SQLiteDatabase.openDatabase( myPath, null,SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close()
    {
        // close the database.
        if (myDataBase != null)  myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CAT_TABLE);
        db.execSQL(CREATE_ITEM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_ITEM_TABLE);
        db.execSQL(DROP_CAT_TABLE);
        onCreate(db);
    }

    public Cursor readItemsFromLocalDB(SQLiteDatabase db){
        String[] projection = {DBContract.ID, DBContract.NAME, DBContract.DESC, DBContract.PRICE, DBContract.NAME, DBContract.CAT_ID};
        return (db.query(DBContract.TABLE_NAME, projection, null, null, null, null, null));
    }

    public Cursor readItemsFromLocalDB(SQLiteDatabase db, int id){
        String[] projection = {DBContract.ID, DBContract.NAME, DBContract.DESC, DBContract.PRICE, DBContract.NAME, DBContract.CAT_ID};
        return (db.query(DBContract.TABLE_NAME, projection, "cat_id=?", new String[] { String.valueOf(id) }, null, null, null));
    }

    public Cursor readItemsFromLocalDB(SQLiteDatabase db, String query){
        String[] projection = {DBContract.ID, DBContract.NAME, DBContract.DESC, DBContract.PRICE, DBContract.NAME, DBContract.CAT_ID};
        return (db.query(DBContract.TABLE_NAME, projection, DBContract.NAME + " LIKE ?", new String[] { "%" +query+"%" }, null, null, null));
    }

    public void saveItemsToLocalDB(int id, String name, String desc, String price, int cat_id, SQLiteDatabase db){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.ID, id);
        contentValues.put(DBContract.NAME, name);
        contentValues.put(DBContract.DESC, desc);
        contentValues.put(DBContract.PRICE, price);
        contentValues.put(DBContract.CAT_ID, cat_id);
        db.insert(DBContract.TABLE_NAME, null, contentValues);
    }

    public Cursor readCatFromLocalDB(SQLiteDatabase db){
        String[] projection = {DBContract.CATID, DBContract.CAT_NAME};
        return (db.query(DBContract.CAT_TABLE_NAME, projection, null, null, null, null, null));
    }

    public void saveCatToLocalDB(String name, SQLiteDatabase db){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.CAT_NAME, name);
        db.insert(DBContract.CAT_TABLE_NAME, null, contentValues);
    }
}
