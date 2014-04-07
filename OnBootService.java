package com.themike10452.hellscorekernelmanager;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.themike10452.hellscorekernelmanager.Blackbox.Blackbox;

import java.io.File;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Mike on 4/6/2014.
 */
public class OnBootService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                int errorcode = 0;
                try {
                    PackageManager m = getPackageManager();
                    String dataDir = getPackageName();
                    PackageInfo p = m.getPackageInfo(dataDir, 0);
                    dataDir = p.applicationInfo.dataDir;
                    File[] scripts = (new File(dataDir + "/scripts")).listFiles();
                    for (File script : scripts) {
                        Log.d("TAG", "sh " + script.toString() + " nodelay");
                        Shell.SU.run(new String[]{
                                "chmod 775 " + script.toString(),
                                "sh " + script.toString() + " nodelay"
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    errorcode += 1;
                }

                File sound = new File(getFilesDir() + File.separator
                        + SoundControlFragment.setOnBootFileName);

                if (sound.exists() && sound.isFile())
                    try {
                        String soundLock = getString(R.string.SOUND_LOCK_PATH);
                        MyTools.SUhardWrite("0", soundLock);


                        String data = (Shell.SU.run("cat " + sound.toString())).get(0);
                        data = data.replace(".", " ");
                        String[] values = data.split("::");
                        if (values.length != 5)
                            throw new Exception();
                        for (byte i = 0; i < values.length; i++) {
                            String value = values[i];
                            String val = null, dir = null;
                            switch (i) {
                                case 0:
                                case 2:
                                    short b3 = Short.parseShort((value.split(" "))[0]);
                                    short b4 = Short.parseShort((value.split(" "))[1]);
                                    if (i != 0)
                                        Shell.SH.run(String.format("echo %s %s >> %s", b3, b4, "/sdcard/HKM_REPORT.txt"));
                                    val = Blackbox.tool1(b3, b4, (byte) 2);
                                    break;
                                case 1:
                                    short b1 = Short.parseShort((value.split(" "))[0]);
                                    short b2 = Short.parseShort((value.split(" "))[1]);
                                    val = Blackbox.tool2(b1, b2, (byte) 2);
                                    break;
                                case 4:
                                case 5:
                                    short b0 = Short.parseShort(value);
                                    val = Blackbox.tool1(b0);
                                    break;
                            }
                            switch (i) {
                                case 0:
                                    dir = getString(R.string.HP_GAIN_PATH);
                                    break;
                                case 1:
                                    dir = getString(R.string.HP_PA_GAIN_PATH);
                                    break;
                                case 2:
                                    dir = getString(R.string.SPEAKER_GAIN_PATH);
                                    break;
                                case 3:
                                    dir = getString(R.string.MIC_GAIN_PATH);
                                    break;
                                case 4:
                                    dir = getString(R.string.CAMMIC_GAIN_PATH);
                                    break;
                            }

                            MyTools.SUhardWrite(val, dir);
                        }

                        MyTools.SUhardWrite("1", soundLock);

                    } catch (Exception e) {
                        e.printStackTrace();
                        errorcode += 2;
                    }
                return errorcode;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.io);
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
                switch (integer) {
                    case 2:
                        MyTools.longToast(getApplicationContext(), R.string.toast_failed_soundControl);
                    case 0:
                        MyTools.longToast(getApplicationContext(), R.string.toast_done_succ);
                        break;
                    case 1:
                        MyTools.longToast(getApplicationContext(), R.string.toast_failed_setOnBoot + "(" + 1 + ")");
                        break;
                    case 3:
                        MyTools.longToast(getApplicationContext(), R.string.toast_failed_setOnBoot + "(" + 3 + ")");
                        break;
                }
            }
        }.execute();

    }

}
