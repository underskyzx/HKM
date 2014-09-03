package com.themike10452.hellscorekernelmanager;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.io.File;


public class MainActivity extends FragmentActivity implements TabListener {

    public static MainActivity instance;

    public static String appVersion;
    private int counter = 0;

    private String[] drawerItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ViewPager.OnPageChangeListener ChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int arg0) {
            counter = 0;
            if (mDrawerList != null)
                mDrawerList.setItemChecked(arg0, true);
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
    private ActionBarDrawerToggle drawerToggle;
    private DBHelper dbH;
    private ActionBar actionBar;
    private ViewPager viewPager;

    private static boolean DatabaseExists(ContextWrapper context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    public static void showDonationDialog(final Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setTitle(activity.getString(R.string.title_donation));
        dialog.setContentView(R.layout.halp);
        dialog.setCancelable(true);
        dialog.findViewById(R.id.button_donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Library.donation_link)));
            }
        });
        ((EditText) dialog.findViewById(R.id.message))
                .setText(Library.donation_email);
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        else
            return super.onOptionsItemSelected(item);
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
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    dbH.getWritableDatabase();
                    MySQLiteAdapter.createColorProfiles(getApplicationContext());
                    MySQLiteAdapter.createCpuProfiles(getApplicationContext());
                    return null;
                }
            }.execute();
        } else {
            dbH.getWritableDatabase();
            if (DBHelper.FLAG_CHANGES_MADE) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        MySQLiteAdapter.createColorProfiles(getApplicationContext());
                        MySQLiteAdapter.createCpuProfiles(getApplicationContext());
                        return null;
                    }
                }.execute();
            }
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        Adapter mAdapter = new Adapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(ChangeListener);
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
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

        drawerItems = new String[]{
                getString(R.string.cpuTab),
                getString(R.string.gpuTab),
                getString(R.string.miscTab),
                getString(R.string.gammaTab),
                getString(R.string.touchControl),
                getString(R.string.soundTab),
                getString(R.string.infoTab),
                getString(R.string.title_activity_profiles).toUpperCase(),
                getString(R.string.title_activity_monitoring).toUpperCase()
        };

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.apptheme_ic_navigation_drawer,
                R.string.app_name,
                R.string.app_name
        );
        mDrawerLayout.setDrawerListener(drawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(
                this,
                R.layout.drawer_list_item,
                drawerItems
        ));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setItemChecked(0, true);
        try {
            if (!BatteryProfilesService.isRunning && getSharedPreferences("SharedPrefs", MODE_PRIVATE).getBoolean("Enable_Profiles_Service", false))
                startService(new Intent(this, BatteryProfilesService.class));
        } catch (Exception e) {
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
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
        if (mDrawerList != null)
            mDrawerList.setItemChecked(arg0.getPosition(), true);
        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(mDrawerList);
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
                        //stopService(new Intent(Intent.ACTION_MAIN));
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

    private void activityDelayed(final Intent intent, final int delay) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mDrawerLayout.closeDrawer(mDrawerList);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                startActivity(intent);
            }
        }.execute();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            int delay = 300;
            if (i < 7) {
                mDrawerList.setSelection(i);
                mDrawerLayout.closeDrawer(mDrawerList);
                viewPager.setCurrentItem(i, false);
            } else {
                mDrawerList.setItemChecked(viewPager.getCurrentItem(), true);
                mDrawerLayout.closeDrawer(mDrawerList);
                switch (i) {
                    case 7:
                        activityDelayed(new Intent(getApplicationContext(), ProfilesActivity.class), delay);
                        break;
                    case 8:
                        activityDelayed(new Intent(getApplicationContext(), MonitoringActivity.class), delay);
                        break;
                }
            }
        }

    }

}

class Adapter extends FragmentStatePagerAdapter {

    public Adapter(FragmentManager fm) {
        super(fm);
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
