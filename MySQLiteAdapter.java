package com.themike10452.hellscorekernelmanager;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MySQLiteAdapter {

    public static ArrayList<String> selectColumn(Context c, String table, String column) {
        DBHelper dbh;
        dbh = new DBHelper(c);
        SQLiteDatabase db = dbh.getWritableDatabase();
        ArrayList<String> ret = new ArrayList<String>();
        assert db != null;
        Cursor crsr = db.query(table, new String[]{column}, null, null, null, null, column);
        while (crsr.moveToNext()) {
            int ind = crsr.getColumnIndex(column);
            ret.add(crsr.getString(ind));
        }
        db.close();
        return ret;
    }

    public static void createColorProfiles(Context context) {
        AssetManager manager = context.getAssets();
        clearTable(context, DBHelper.COLOR_PROFILES_TABLE);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(manager.open("color_profiles.dat")));
            String values = reader.readLine();
            while (values != null) {
                if (values.length() != 0) {
                    String q = "INSERT INTO " + DBHelper.COLOR_PROFILES_TABLE + " VALUES (" + values + ");";
                    insert(context, q);
                }
                values = reader.readLine();
            }
            reader.close();
        } catch (Exception ignored) {
        }
    }

    public static void createCpuProfiles(Context context) {
        String[] profiles = Library.getCpuProfiles();
        for (String item : profiles) {
            insert(context, DBHelper.CPU_PROFILES_TABLE, item.split("-"));
        }
    }

    private static void insert(Context context, String q) {
        DBHelper dbh;
        dbh = new DBHelper(context);
        SQLiteDatabase db = dbh.getWritableDatabase();
        try {
            assert db != null;
            db.execSQL(q);
            db.close();
        } catch (Exception e) {
            assert db != null;
            db.close();
        }
    }

    public static boolean insert(Context context, String table, String[] values) {
        DBHelper dbh;
        dbh = new DBHelper(context);
        SQLiteDatabase db = dbh.getWritableDatabase();
        String v = quote(values[0]);
        for (int i = 1; i < values.length; i++)
            v += ", " + quote(values[i]);
        String q = "INSERT INTO " + table + " values (" + v + ");";

        try {
            assert db != null;
            db.execSQL(q);
            db.close();
            return true;
        } catch (SQLException e) {
            assert db != null;
            db.close();
            return false;
        }
    }

    public static void insertOrUpdate(Context context, String table, String[] columns, String[] values) {
        DBHelper dbh;
        dbh = new DBHelper(context);
        SQLiteDatabase db = dbh.getWritableDatabase();
        String v = quote(values[0]);
        for (int i = 1; i < values.length; i++)
            v += ", " + quote(values[i]);
        String q = "INSERT INTO " + table + " values (" + v + ");";
        try {
            assert db != null;
            db.execSQL(q);
        } catch (SQLException e) {
            String qu = "UPDATE " + table + " SET " + columns[1] + "=" + quote(values[1]);
            for (int i = 2; i < columns.length; i++) {
                qu += ", " + columns[i] + "=" + quote(values[i]);
            }
            qu += " WHERE " + columns[0] + " = " + quote(values[0]) + ";";
            try {
                assert db != null;
                db.execSQL(qu);
            } catch (SQLException ignored) {
            }
        }
        assert db != null;
        db.close();
    }

    public static String[] select(Context context, String table, String key, String value, String[] col) {
        DBHelper dbh;
        dbh = new DBHelper(context);
        SQLiteDatabase db = dbh.getWritableDatabase();
        String whereClause = key + " = ?";
        String[] whereArgs = {value};
        assert db != null;
        Cursor crsr = db.query(table, null, whereClause, whereArgs, null, null, null);
        StringBuffer sb = new StringBuffer();
        while (crsr.moveToNext()) {
            for (String s : col)
                sb.append(crsr.getString(crsr.getColumnIndex(s))).append("~");
        }
        db.close();
        return new String(sb).split("~");
    }

    public static boolean delete(Context context, String table, String key, String value) {
        DBHelper dbh;
        dbh = new DBHelper(context);
        SQLiteDatabase db = dbh.getWritableDatabase();
        String whereClause = key + " = ?";
        String[] whereArgs = {value};

        try {
            assert db != null;
            db.delete(table, whereClause, whereArgs);
        } catch (Exception e) {
            return false;
        }
        db.close();
        return true;
    }

    public static boolean clearTable(Context context, String table) {
        DBHelper dbh;
        dbh = new DBHelper(context);
        SQLiteDatabase db = dbh.getWritableDatabase();
        try {
            assert db != null;
            db.delete(table, null, null);
            db.close();
            return true;
        } catch (Exception e) {
            assert db != null;
            db.close();
            return false;
        }
    }

    private static String quote(String s) {
        return "'" + s + "'";
    }

}
