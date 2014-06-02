package com.themike10452.hellscorekernelmanager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.util.Timer;
import java.util.TimerTask;

public class BatteryProfilesService extends Service {
    static IntentFilter filter = null;
    static boolean isRunning;
    static Timer timer;

    public BatteryProfilesService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        final SharedPreferences preferences = getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
        (timer = new Timer()).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                while (isRunning) {
                    Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    int charging = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    if (!(charging == BatteryManager.BATTERY_PLUGGED_AC || charging == BatteryManager.BATTERY_PLUGGED_USB)) {
                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        apply(preferences, level);
                    } else {
                        apply(preferences);
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }, 0, 5000);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        timer.cancel();
    }

    void apply(SharedPreferences preferences, int level) {
        String toApply;
        int lowerBorn = preferences.getInt("batteryLT_edge", -101);
        int upperBorn = preferences.getInt("batteryGT_edge", 101);
        String currentProfile = preferences.getString("battery_current_profile", "Custom");
        MediaPlayer player = MediaPlayer.create(getApplicationContext(), R.raw.doink);

        String[] values;

        if (level < lowerBorn) {
            values = Library.getCpuProfile(toApply = preferences.getString("batteryLT_profile_alias", null));
        } else if (level >= upperBorn) {
            values = Library.getCpuProfile(toApply = preferences.getString("batteryGT_profile_alias", null));
        } else {
            toApply = "Custom";
            String[] toks = preferences.getString("CustomProfileData", "empty").split("\\|");
            values = new String[]{toks[0], toks[1], toks[2], toks[3], toks[4], toks[5], toks[6]};
        }

        if (!toApply.equals(currentProfile)) {
            if (values != null) {
                values = MyTools.addToArray(values, values[1], values.length);
            } else {
                toApply = "Custom";
                String[] toks = preferences.getString("CustomProfileData", "empty").split("\\|");
                values = new String[]{toks[0], toks[1], toks[2], toks[3], toks[4], toks[5], toks[6], toks[1]};
            }

            final String[] finalValues = values;

            final String[] dirs = new String[]{
                    Library.GOV0,
                    Library.MAX_FREQ0_PATH,
                    Library.MIN_FREQ0_PATH,
                    Library.MAX_CPUS_ONLINE_PATH,
                    Library.MIN_CPUS_ONLINE_PATH,
                    Library.BOOSTED_CPUS_PATH,
                    "/sys/devices/system/cpu/cpufreq/" + values[0].trim() + "/boostfreq",
                    "/sys/devices/system/cpu/cpufreq/" + values[0].trim() + "/lmf_active_max_freq"
            };

            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... voids) {
                    for (int a = 0; a < finalValues.length; a++) {
                        MyTools.SUhardWrite(finalValues[a], dirs[a]);
                    }
                    return 0;
                }
            }.execute();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("battery_current_profile", toApply);
            editor.commit();
            player.start();
            final String str = toApply;
            Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), str + " Loaded", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void apply(SharedPreferences preferences) {

        String toApply;
        String currentProfile = preferences.getString("battery_current_profile", "Custom");

        String[] values;
        toApply = "Custom";
        String[] toks = preferences.getString("CustomProfileData", "empty").split("\\|");
        try {
            values = new String[]{toks[0], toks[1], toks[2], toks[3], toks[4], toks[5], toks[6], toks[1]};
        } catch (ArrayIndexOutOfBoundsException e) {
            return;
        }

        if (!toApply.equals(currentProfile)) {

            final String[] finalValues = values;

            final String[] dirs = new String[]{
                    Library.GOV0,
                    Library.MAX_FREQ0_PATH,
                    Library.MIN_FREQ0_PATH,
                    Library.MAX_CPUS_ONLINE_PATH,
                    Library.MIN_CPUS_ONLINE_PATH,
                    Library.BOOSTED_CPUS_PATH,
                    "/sys/devices/system/cpu/cpufreq/" + values[0].trim() + "/boostfreq",
                    "/sys/devices/system/cpu/cpufreq/" + values[0].trim() + "/lmf_active_max_freq"
            };

            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... voids) {
                    for (int a = 0; a < finalValues.length; a++) {
                        MyTools.SUhardWrite(finalValues[a], dirs[a]);
                    }
                    return 0;
                }
            }.execute();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("battery_current_profile", toApply);
            editor.commit();
            final String str = toApply;
            Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), str + " Loaded", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
