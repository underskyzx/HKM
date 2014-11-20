package com.themike10452.hellscorekernelmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.io.File;


public class MainActivity extends FragmentActivity {

    public static MainActivity instance;

    public static String appVersion;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ViewPager.OnPageChangeListener ChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int arg0) {
            if (mDrawerList != null)
                mDrawerList.setItemChecked(arg0, true);

            ActionBar actionBar = getActionBar();

            switch (arg0) {
                case 0:
                    if (actionBar != null)
                        actionBar.setTitle(getString(R.string.cpuTab));
                    break;
                case 1:
                    if (actionBar != null)
                        actionBar.setTitle(getString(R.string.gpuTab));
                    break;
                case 2:
                    if (actionBar != null)
                        actionBar.setTitle(getString(R.string.miscTab));
                    break;
                case 3:
                    if (actionBar != null)
                        actionBar.setTitle(getString(R.string.gammaTab));
                    break;
                case 4:
                    if (actionBar != null)
                        actionBar.setTitle(getString(R.string.touchControl));
                    break;
                case 5:
                    if (actionBar != null)
                        actionBar.setTitle(getString(R.string.soundTab));
                    break;
                case 6:
                    if (actionBar != null)
                        actionBar.setTitle(getString(R.string.infoTab));
            }

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };
    private ActionBarDrawerToggle drawerToggle;
    private DBHelper dbH;
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
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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
        PagerAdapter mAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(ChangeListener);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                actionBar.setElevation(1);
        }

        String[] drawerItems = new String[]{
                getString(R.string.cpuTab).toUpperCase(),
                getString(R.string.gpuTab).toUpperCase(),
                getString(R.string.miscTab).toUpperCase(),
                getString(R.string.gammaTab).toUpperCase(),
                getString(R.string.touchControl).toUpperCase(),
                getString(R.string.soundTab).toUpperCase(),
                getString(R.string.infoTab).toUpperCase(),
                getString(R.string.title_activity_profiles).toUpperCase(),
                getString(R.string.title_activity_monitoring).toUpperCase()
        };

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
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
            if (!new File("/sys/kernel/msm_mpdecision").exists())
                if (!BatteryProfilesService.isRunning && getSharedPreferences("SharedPrefs", MODE_PRIVATE).getBoolean("Enable_Profiles_Service", false))
                    startService(new Intent(this, BatteryProfilesService.class));
        } catch (Exception ignored) {
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
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
                        if (!new File("/sys/kernel/msm_mpdecision").exists())
                            activityDelayed(new Intent(getApplicationContext(), ProfilesActivity.class), delay);
                        else
                            MyTools.toast(getApplicationContext(), "Not available for hellsCore b49 and up.");
                        break;
                    case 8:
                        activityDelayed(new Intent(getApplicationContext(), MonitoringActivity.class), delay);
                        break;
                }
            }
        }

    }

}

class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    public ScreenSlidePagerAdapter(FragmentManager fm) {
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
