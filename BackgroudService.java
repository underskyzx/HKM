package com.themike10452.hellscorekernelmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;

public class BackgroudService extends Service {

    private String[] Values;
    private String kgamma_r;
    private String kgamma_g;
    private String kgamma_b;
    private String kcal;
    private boolean work;

    public void onCreate() {
        String loaded;
        try {
            loaded = (MySQLiteAdapter.select(getBaseContext(), DBHelper.SETTINGS_TABLE, DBHelper.SETTINGS_TABLE_KEY,
                    this.getString(R.string.LOADED_COLOR_PROFILE), new String[]{DBHelper.SETTINGS_TABLE_COLUMN1}))[0];
        } catch (Exception e) {
            loaded = "~CUSTOM~";
        }
        if (loaded.equals("~CUSTOM~")) {
            Values = null;
            this.stopSelf();
        } else {
            Values = MySQLiteAdapter.select(getBaseContext(),
                    DBHelper.COLOR_PROFILES_TABLE,
                    DBHelper.COLOR_PROFILES_TABLE_KEY,
                    loaded,
                    new String[]{"red", "green", "blue", "cal"});
        }

        kgamma_r = this.getString(R.string.kgamma_r);
        kgamma_g = this.getString(R.string.kgamma_g);
        kgamma_b = this.getString(R.string.kgamma_b);
        kcal = this.getString(R.string.kcal);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        work = true;
        Thread t = new tasker();
        t.start();
        return Service.START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        work = false;

    }

    private class tasker extends Thread {

        public void run() {
            if (Values != null && Values.length == 4) {
                while (work) {
                    MyTools.write(Values[0], kgamma_r);
                    MyTools.write(Values[1], kgamma_g);
                    MyTools.write(Values[2], kgamma_b);
                    MyTools.write(Values[3], kcal);
                    if ((new File(getString(R.string.kgamma_apply))).exists())
                        MyTools.execTerminalCommand(new String[]{
                                String.format("echo 1 > %s", getString(R.string.kgamma_apply))
                        });
                    try {
                        long cycle = 15000;
                        Thread.sleep(cycle);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.interrupt();
        }

    }
}
