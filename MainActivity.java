package com.themike10452.hellscorekernelmanager;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

import java.io.File;


public class MainActivity extends FragmentActivity implements TabListener {

    public static String appVersion;
    private int counter = 0;
    private ViewPager.OnPageChangeListener ChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int arg0) {
            counter = 0;
            actionBar.setSelectedNavigationItem(arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            counter = 0;
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            counter = 0;
        }
    };
    private DBHelper dbH;
    private ActionBar actionBar;
    private ViewPager viewPager;

    private static boolean DatabaseExists(ContextWrapper context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Dialog dialog = new Dialog(this);
                String[] list = getResources().getStringArray(R.array.messages_assistance);
                String s = "";
                for (String st : list)
                    s += st + '\n';
                dialog.setTitle(getString(R.string.title_assistance));
                dialog.setContentView(R.layout.halp);
                dialog.setCancelable(true);
                ((TextView) dialog.findViewById(R.id.message)).setText(s);
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        MyTools.initiateSUShell(this);

        try {
            appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }

        setContentView(R.layout.activity_main);
        dbH = new DBHelper(this);
        if (!DatabaseExists(this, DBHelper.dbName)) {
            dbH.getWritableDatabase();
            MySQLiteAdapter.createProfiles(this);
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        Adapter mAdapter = new Adapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(ChangeListener);
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.apptheme_ic_navigation_drawer);
        }
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab cpuControlTab = actionBar.newTab();
        ActionBar.Tab touchControlTab = actionBar.newTab();
        ActionBar.Tab gammaControlTab = actionBar.newTab();
        ActionBar.Tab gpuControlTab = actionBar.newTab();
        ActionBar.Tab miscTab = actionBar.newTab();
        ActionBar.Tab soundControlTab = actionBar.newTab();
        ActionBar.Tab infoTab = actionBar.newTab();

        cpuControlTab.setText(R.string.cpuTab);
        gpuControlTab.setText(R.string.gpuTab);
        touchControlTab.setText(R.string.touchControl);
        gammaControlTab.setText(R.string.gammaTab);
        miscTab.setText(R.string.miscTab);
        soundControlTab.setText(R.string.soundTab);
        infoTab.setText(R.string.infoTab);

        cpuControlTab.setTabListener(this);
        touchControlTab.setTabListener(this);
        gammaControlTab.setTabListener(this);
        gpuControlTab.setTabListener(this);
        miscTab.setTabListener(this);
        soundControlTab.setTabListener(this);
        infoTab.setTabListener(this);

        actionBar.addTab(cpuControlTab);
        actionBar.addTab(gpuControlTab);
        actionBar.addTab(miscTab);
        actionBar.addTab(gammaControlTab);
        actionBar.addTab(touchControlTab);
        actionBar.addTab(soundControlTab);
        actionBar.addTab(infoTab);
    }

    @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
        counter++;
        if (counter == 2) {
            counter = 0;
            final Dialog dialog = new Dialog(this);
            dialog.setTitle(getString(R.string.title_about));
            dialog.setContentView(R.layout.about_dialog);
            TextView version = (TextView) dialog.findViewById(R.id.misc);

            try {
                appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                version.setText("v " + appVersion);
            } catch (Exception ignored) {
            }

            dialog.show();
        }
    }

    @Override
    public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
        counter = 0;
        viewPager.setCurrentItem(arg0.getPosition(), true);
    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.areYouSure))
                .setCancelable(true)
                .setTitle(getString(R.string.title_confirmation))
                .setPositiveButton(getString(R.string.button_leave), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stopService(new Intent(Intent.ACTION_MAIN));
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.button_stay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

}

class Adapter extends FragmentStatePagerAdapter {

    public Adapter(FragmentManager fm) {
        super(fm);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Fragment getItem(int arg0) {
        Fragment f = null;
        switch (arg0) {
            case 0:
                f = new CpuControlFragment();
                break;
            case 1:
                f = new GpuControlFragment();
                break;
            case 2:
                f = new MiscFragment();
                break;
            case 3:
                f = new GammaControlFragment();
                break;
            case 4:
                f = new TouchControlFragment();
                break;
            case 5:
                f = new SoundControlFragment();
                break;
            case 6:
                f = new InfoTabFragment();
        }
        return f;
    }

    @Override
    public int getCount() {
        return 7;
    }

}
