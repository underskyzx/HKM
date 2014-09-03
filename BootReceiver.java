package com.themike10452.hellscorekernelmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.themike10452.hellscorekernelmanager.Blackbox.Blackbox;
import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.io.File;

import eu.chainfire.libsuperuser.Shell;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent ignored) {

        String kernel = MyTools.readFile("/proc/version", "n/a");
        if (kernel.toLowerCase().contains("hells") || Blackbox.tool4(context))
            try {
                Shell.SH.run(String.format("echo `date +%s` -- boot intent received >> %s", "+%T", "/sdcard/HKM.log"));
                if (!(new File(Library.kgamma_blue).exists())) {
                    Intent cm = new Intent(context, BackgroudService.class);
                    context.startService(cm);
                }
                Thread.sleep(30000, 0);
                Intent intent = new Intent(context, OnBootService.class);
                context.startService(intent);
                Shell.SH.run(String.format("echo `date %s` -- boot service called >> %s", "+%T", "/sdcard/HKM.log"));
                if (!BatteryProfilesService.isRunning && context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE).getBoolean("Enable_Profiles_Service", false))
                    context.startService(new Intent(context, BatteryProfilesService.class));
            } catch (Exception e) {
            } catch (Error err) {
            }
    }
}