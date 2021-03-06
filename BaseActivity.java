package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.themike10452.hellscorekernelmanager.Blackbox.Blackbox;
import com.themike10452.hellscorekernelmanager.Blackbox.Library;

public class BaseActivity extends Activity {

    private static BaseActivity instance;

    public BaseActivity() {
        instance = this;
    }

    public static BaseActivity getInstance() {
        if (instance != null)
            return instance;
        else
            return new BaseActivity();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        final SharedPreferences preferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
        String kernel;
        try {
            kernel = MyTools.readFile("/proc/version");
        } catch (Exception ignored) {
            kernel = "n/a";
        }
        if (kernel.toLowerCase().contains("hells") || Blackbox.tool4(getApplicationContext())) {
            boolean b;
            try {
                b = MyTools.readFile("/sys/fs/selinux/enforce").equals("1") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
            } catch (Throwable ignored) {
                b = false;
            }
            if (b && !preferences.getBoolean("hide_enforcingDialog", false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                Dialog d = builder.setPositiveButton("Go to PlayStore", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=com.mrbimc.selinux"));
                        startActivity(intent);
                        finish();
                    }
                })
                        .setNegativeButton("Hide", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                preferences.edit().putBoolean("hide_enforcingDialog", true).apply();
                                load();
                            }
                        })
                        .setNeutralButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                load();
                            }
                        })
                        .setCancelable(false)
                        .setTitle("SELinux Enforcing")
                        .setMessage("If SELinux is set to permissive mode, there is relatively little to worry about, but when it is set to enforcing, the part of your app running as root may run into all sorts of unexpected restrictions.\n\n-- Chainfire\n\nPlease install SELinux Mode Changer from PlayStore to change SELinux mode to permissive, otherwise this app (and others) will misbehave.")
                        .show();
                ((TextView) d.findViewById(android.R.id.message)).setTextSize(16);
            } else {
                load();
            }
        } else {
            findViewById(R.id.progressBar).setVisibility(TextView.GONE);
            findViewById(R.id.button).setVisibility(View.VISIBLE);
            TextView msg = (TextView) findViewById(R.id.messageView);
            msg.setTextColor(getResources().getColor(R.color.hellorange));
            msg.setText(getString(R.string.sorry_not_supported));
            final Button getHellscore = (Button) findViewById(R.id.button);
            final Activity activity = this;
            getHellscore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EasyTracker easyTracker = EasyTracker.getInstance(activity);
                    easyTracker.send(MapBuilder
                                    .createEvent("get_hellscore_kernel",
                                            "move_to_kernel",
                                            "get_hellscore_now",
                                            null)
                                    .build()
                    );
                    PackageManager manager = getPackageManager();
                    String packageName = "lb.themike10452.hellscorekernelupdater";

                    Intent intent = manager.getLaunchIntentForPackage(packageName);

                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        startActivity(intent);
                    } else {
                        try {
                            intent = manager.getLaunchIntentForPackage("com.android.vending");
                            ComponentName comp = new ComponentName("com.android.vending", "com.google.android.finsky.activities.LaunchUrlHandlerActivity"); // package name and activity
                            intent.setComponent(comp);
                            intent.setData(Uri.parse("market://details?id=" + packageName));
                            startActivity(intent);
                        } catch (Exception e) {
                            try {
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                startActivity(intent);
                            } catch (Exception e2) {
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                                startActivity(intent);
                            }
                        }
                    }
                }
            });
        }

    }

    private void load() {
        if (Build.DEVICE.trim().equalsIgnoreCase("hammerhead")) {
            Library.GPU_AVAIL_FREQ_PATH = Library.GPU_AVAIL_FREQ_PATH_HAMMERHEAD;
            Library.GPU_MAX_CLK_PATH = Library.GPU_MAX_CLK_PATH_HAMMERHEAD;
            Library.GPU_POLICY_PATH = Library.GPU_POLICY_PATH_HAMMERHEAD;
            Library.GPU_GOV_PATH = Library.GPU_GOV_PATH_HAMMERHEAD;
            Library.MSM_THERMAL_PATH = Library.MSM_THERMAL_PATH_HAMMERHEAD;
        }
        Intent i = new Intent(this, MainActivity.class);
        new ProgressTask(this, i).execute();
    }

}
