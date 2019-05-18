package com.simeonch.eshoppy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalDBHelper extends SQLiteOpenHelper {

    private static final String LOG = "LocalDBHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LocalLists";

    //column/field names
    private static final String KEY_itemName = "itemName";
    private static final String KEY_itemQuantity = "itemQuantity";
    private static final String KEY_itemPrice = "itemPrice";
    private static final String KEY_itemBrand = "itemBrand";
    private static final String KEY_myStatus = "itemStatus";

    //table name and create sql command
    private static String NewTableName = "FirstList";


    //called after constructor really
    public void CreateLocalList() {
        SQLiteDatabase db = this.getWritableDatabase();
        if(!NewTableName.equals("FirstList")) {
            Log.w("CARR", "Create table: " + NewTableName);
            //db.execSQL(createNewTableCommand);
            db.execSQL("CREATE TABLE " + NewTableName + "(" + KEY_itemName + " TEXT PRIMARY KEY,"
                    + KEY_itemQuantity + " INTEGER," + KEY_itemPrice + " REAL," + KEY_itemBrand + " TEXT," + KEY_myStatus + " INTEGER" + ")");
        }
    }

    //return tables/ "Lists"
    public List<String> GetTables() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> theLists = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                if(!c.getString(0).equals("android_metadata")) {
                    //Log.w("CARR", c.getString(0));
                    theLists.add(c.getString(0));
                }
                c.moveToNext();
            }
        }
        db.close();
        return theLists;
    }


    public void AddTableItem(String toTable, Item it) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_itemName, it.getItemName());
        values.put(KEY_itemQuantity, it.getItemQuantity());
        values.put(KEY_itemPrice, it.getItemPrice());
        values.put(KEY_itemBrand, it.getItemBrand());
        values.put(KEY_myStatus, it.getMyStatus());

        Log.w("CARR", "ADD LOCAL ITEM: " + it.getItemName());
        db.insert(toTable, null, values);
        db.close();
    }

    public void UpdateItem(String fromTable, Item it, String oldName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_itemName, it.getItemName());
        values.put(KEY_itemQuantity, it.getItemQuantity());
        values.put(KEY_itemPrice, it.getItemPrice());
        values.put(KEY_itemBrand, it.getItemBrand());
        values.put(KEY_myStatus, it.getMyStatus());

        String name = it.getItemName();
        if(oldName != null) {
            name = oldName;
        }

        db.update(fromTable, values, KEY_itemName + " = ?",  new String[]{name});
        Log.w("CARR", "update item: " + it.getItemName() + " q: " + it.getItemQuantity());
        db.close();
    }

    public ArrayList<Item> GetTableItems(String fromTable) {
        ArrayList<Item> toReturn = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String SelectQuery = "SELECT * FROM " + fromTable;
        Cursor c = db.rawQuery(SelectQuery, null);

        if(c.moveToFirst()) {
            while(!c.isAfterLast()) {
                Item newItem = new Item();
                //final Item newItem = new Item(itemName.toLowerCase(), 1, 1, "none", -1);
                newItem.setItemName(c.getString((c.getColumnIndex(KEY_itemName))));
                newItem.setQuantity(c.getInt((c.getColumnIndex(KEY_itemQuantity))));
                newItem.setItemPrice(c.getDouble((c.getColumnIndex(KEY_itemPrice))));
                newItem.setItemBrand(c.getString((c.getColumnIndex(KEY_itemBrand))));
                newItem.setMyStatus(c.getInt((c.getColumnIndex(KEY_myStatus))));

                toReturn.add(newItem);
                Log.w("CARR", newItem.getItemName());
                c.moveToNext();
            }
        }

        db.close();
        return toReturn;

    }

    public void UpdateItemQ(String fromTable, Item it) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_itemQuantity, it.getItemQuantity());

        db.update(fromTable, values, KEY_itemName + " = ?",  new String[]{it.getItemName()});
        Log.w("CARR", "update item quantity: " + it.getItemName() + " q: " + it.getItemQuantity());
        db.close();
    }

    public void ChangeItemStatus(String fromTable, Item it) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_myStatus, it.getMyStatus());

        db.update(fromTable, values, KEY_itemName + " = ?",  new String[]{it.getItemName()});
        Log.w("CARR", "update item status: " + it.getItemName() + "  " + it.getMyStatus());
        db.close();
    }

    public void DeleteItem(String fromTable, Item it) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(fromTable, KEY_itemName + " = ?", new String[]{it.getItemName()});
        Log.w("CARR", "delete local: " + it.getItemName());
        db.close();
    }

    //delete the table/ "list" (+clear contents)
    public void DeleteList(String toDel) {
        SQLiteDatabase db = this.getWritableDatabase();

        //db.delete(toDel, null, null);
        db.execSQL("delete from " + toDel);
        db.execSQL("DROP TABLE IF EXISTS " + toDel);
        db.close();
    }


    //accessing current db state
    public LocalDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //when creating a new table
    public LocalDBHelper(Context context, String name) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        NewTableName = name;
    }

    //called when app is freshly installed
    @Override
    public void onCreate(SQLiteDatabase db) {
//        if(NewTableName != null) {
//            db.execSQL(createNewTableCommand);
//        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        List<String> tables = GetTables();
        for(String s : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + s);
        }

        onCreate(db);
    }
}
