package com.themike10452.hellscorekernelmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.chainfire.libsuperuser.Shell;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent ignored) {

        Shell.SH.run(String.format("echo intent received @ `date %s` > %s", "+%T", "/sdcard/HKM.log"));
        Intent intent = new Intent(context, OnBootService.class);
        context.startService(intent);

        return;
    }
}