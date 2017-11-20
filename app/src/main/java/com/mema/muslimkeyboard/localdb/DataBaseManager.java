package com.mema.muslimkeyboard.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by canopus on 12/4/17.
 */

public class DataBaseManager extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "muslim_imoji";
    private static final String TABLE_DIALOGS = "DIALOG";
    private static final String KEY_FROM_CITY = "dialogId";

    SQLiteDatabase db;

    public DataBaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_DIALOGS + "("
                + KEY_FROM_CITY + " TEXT" + ")";

        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIALOGS);
        onCreate(db);
    }


    public long addDialog(String dialog) {
        List<String> strings = getAllDialog();
        ContentValues values = new ContentValues();
        if (strings.size() == 0) {
            values.put(KEY_FROM_CITY, dialog);
            return db.insert(TABLE_DIALOGS, null, values);
        } else {
            for (String str : strings) {
                if (!str.trim().contains(dialog)) {
                    values.put(KEY_FROM_CITY, dialog);
                    return db.insert(TABLE_DIALOGS, null, values);
                }
            }
        }

        return 0;

    }

    public List<String> getAllDialog() {
        List<SQLiteOpenHelper> contactList = new ArrayList<SQLiteOpenHelper>();
        String selectQuery = "SELECT  * FROM " + TABLE_DIALOGS;
        ArrayList<String> strings = new ArrayList<>();
        try {
            db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    strings.add(cursor.getString(0));
                }

            }
        } catch (Exception e) {
            Log.d("", "getAllJourney: .");
        }

        return strings;
    }

}
