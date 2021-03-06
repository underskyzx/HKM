package com.themike10452.hellscorekernelmanager;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static boolean FLAG_CHANGES_MADE;

    public static final String dbName = "hellscore";
    public static final String COLOR_PROFILES_TABLE = "color_profiles";
    public static final String CPU_PROFILES_TABLE = "cpu_profiles";
    public static final String SETTINGS_TABLE = "settings";
    public static final String COLOR_PROFILES_TABLE_KEY = "name";
    public static final String susfreqLock_entry = "susfreq_unlocked";
    public static final String SETTINGS_TABLE_KEY = "_id";
    public static final String SETTINGS_TABLE_COLUMN1 = "value";
    public static final String SETTINGS_TABLE_COLUMN2 = "file";
    public static final String CPU_PROFILES_TABLE_KEY = "_profiles";
    public static final String CPU_PROFILES_TABLE_COLUMN1 = "value";
    public static final String sound_linkLR_entry = "sound_LR_linked";

    private static final String CREATE_TABLE1 = "CREATE TABLE " + COLOR_PROFILES_TABLE + " ("
            + COLOR_PROFILES_TABLE_KEY + " VARCHAR(31) PRIMARY KEY, "
            + "red VARCHAR(255), green VARCHAR(255), blue VARCHAR(255), cal VARCHAR (255));";

    private static final String CREATE_TABLE2 = "CREATE TABLE " + SETTINGS_TABLE + " ("
            + SETTINGS_TABLE_KEY + " VARCHAR(255) PRIMARY KEY, "
            + SETTINGS_TABLE_COLUMN1 + " VARCHAR(255), "
            + SETTINGS_TABLE_COLUMN2 + " VARCHAR(255));";

    private static final String CREATE_TABLE3 = "CREATE TABLE " + CPU_PROFILES_TABLE + " ("
            + CPU_PROFILES_TABLE_KEY + " VARCHAR(255) PRIMARY KEY, "
            + CPU_PROFILES_TABLE_COLUMN1 + " VARCHAR(255));";

    public static final int dbVersion = 2;

    public DBHelper(Context context) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE1);
            db.execSQL(CREATE_TABLE2);
            db.execSQL(CREATE_TABLE3);
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        FLAG_CHANGES_MADE = true;
        switch (oldVersion) {
            case 1:
                db.execSQL(CREATE_TABLE3);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
