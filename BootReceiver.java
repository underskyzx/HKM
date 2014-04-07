package com.themike10452.hellscorekernelmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import eu.chainfire.libsuperuser.Shell;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent ignored) {

        try {
            Shell.SH.run(String.format("echo boot intent received @ `date %s` >> %s", "+%T", "/sdcard/HKM.log"));
            if (!(new File(context.getString(R.string.kgamma_blue)).exists())) {
                Intent cm = new Intent(context, BackgroudService.class);
                context.startService(cm);
            }
            Thread.sleep(45000, 0);
            Intent intent = new Intent(context, OnBootService.class);
            context.startService(intent);
            Shell.SH.run(String.format("echo boot service called @ `date %s` >> %s", "+%T", "/sdcard/HKM.log"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}