package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.io.File;

/**
 * Created by Mike on 4/19/2014.
 */
public final class ProfilesActivity extends Activity {

    private static String[] profileNames = {"PureBattery", "Battery", "Balanced", "Performance", "PurePerformance"};
    private final SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            try {
                ((TextView) findViewById(R.id.profileDisplay)).setText(profileNames[i]);
                String[] tok = Library.getCpuProfiles()[i].split("\\.");
                nextGov.setText(tok[0].split("-")[1]);
                nextMaxFreq.setText(tok[1]);
                nextMinFreq.setText(tok[2]);
                nextMaxCores.setText(tok[3]);
                nextMinCores.setText(tok[4]);
                nextBoostCores.setText(tok[5]);
                nextBoostFreq.setText(tok[6]);
                if (!b)
                    use_custom.setChecked(false);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private static String maxFreq, minFreq, maxCores, minCores, cpuBoost, boostFreq, governor;
    private static TextView currentMaxFreq, nextMaxFreq, currentMinFreq, nextMinFreq,
            currentMaxCores, nextMaxCores, currentMinCores, nextMinCores, currentGov, nextGov,
            currentBoostCores, nextBoostCores, currentBoostFreq, nextBoostFreq;
    private static Spinner sp1, sp2;
    private static EditText LTE, GTE;
    private final CheckBox.OnCheckedChangeListener use_custom_listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                seekBar.setProgress(0);
                ((TextView) findViewById(R.id.profileDisplay)).setText(getString(R.string.custom));
                nextGov.setText("");
                nextMaxFreq.setText("");
                nextMinFreq.setText("");
                nextMaxCores.setText("");
                nextMinCores.setText("");
                nextBoostCores.setText("");
                nextBoostFreq.setText("");
            } else {
                seekBar.setProgress(2);
            }
            seekBar.setEnabled(!b);
        }
    };
    SharedPreferences preferences;
    private SeekBar seekBar;
    private CheckBox use_custom;
    private Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception ignored) {
        }
        setContentView(R.layout.activity_profiles);
        thisActivity = this;
        currentGov = (TextView) findViewById(R.id.currentGovET);
        nextGov = (TextView) findViewById(R.id.nextGovET);
        currentMaxFreq = (TextView) findViewById(R.id.currentMXFET);
        nextMaxFreq = (TextView) findViewById(R.id.nextMXFET);
        currentMinFreq = (TextView) findViewById(R.id.currentMNFET);
        nextMinFreq = (TextView) findViewById(R.id.nextMNFET);
        currentMaxCores = (TextView) findViewById(R.id.currentMXCET);
        nextMaxCores = (TextView) findViewById(R.id.nextMXCET);
        currentMinCores = (TextView) findViewById(R.id.currentMNCET);
        nextMinCores = (TextView) findViewById(R.id.nextMNCET);
        currentBoostCores = (TextView) findViewById(R.id.currentBCET);
        nextBoostCores = (TextView) findViewById(R.id.nextBCET);
        currentBoostFreq = (TextView) findViewById(R.id.currentBFET);
        nextBoostFreq = (TextView) findViewById(R.id.nextBFET);
        use_custom = (CheckBox) findViewById(R.id.cb);
        use_custom.setOnCheckedChangeListener(use_custom_listener);
        seekBar = (SeekBar) findViewById(R.id.profileBar);
        seekBar.setOnSeekBarChangeListener(seekBarListener);
        sp1 = (Spinner) findViewById(R.id.sp1);
        sp2 = (Spinner) findViewById(R.id.sp2);
        sp1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, MyTools.addToArray(profileNames, getString(R.string.custom), 0)));
        sp2.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, MyTools.addToArray(profileNames, getString(R.string.custom), 0)));
        findViewById(R.id.moreBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.autoSwitch).setVisibility(View.VISIBLE);
                findViewById(R.id.moreBtn).setVisibility(View.GONE);
            }
        });
        LTE = (EditText) findViewById(R.id.battery_lt_p);
        GTE = (EditText) findViewById(R.id.battery_gt_p);
        preferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        maxFreq = MyTools.readFile(Library.MAX_FREQ0_PATH);
        minFreq = MyTools.readFile(Library.MIN_FREQ0_PATH);
        maxCores = MyTools.readFile(Library.MAX_CPUS_ONLINE_PATH);
        minCores = MyTools.readFile(Library.MIN_CPUS_ONLINE_PATH);
        cpuBoost = MyTools.readFile(Library.BOOSTED_CPUS_PATH);
        governor = MyTools.readFile(Library.GOV0);
        boostFreq = MyTools.readFile("/sys/devices/system/cpu/cpufreq/" + governor + "/boostfreq");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_apply:
                apply();
                return true;
        }
        return false;
    }

    private void refresh() {
        onStart();
        currentGov.setText(governor);
        currentMaxFreq.setText(maxFreq);
        currentMinFreq.setText(minFreq);
        currentMaxCores.setText(maxCores);
        currentMinCores.setText(minCores);
        currentBoostCores.setText(cpuBoost);
        currentBoostFreq.setText(boostFreq);
        String currentSettings = governor + "." + maxFreq + "." + minFreq + "." + maxCores + "." + minCores + "." + cpuBoost + "." + boostFreq;
        int ind = 0;
        String[] ops = Library.getCpuProfiles(getApplicationContext());
        try {
            for (String preset : ops) {
                String s = preset.split("-")[1];
                if (s.equals(currentSettings)) {
                    String[] tok = Library.getCpuProfiles()[ind].replace(".", " ").split(" ");
                    ((TextView) findViewById(R.id.profileDisplay)).setText(tok[0].split("-")[0]);
                    nextGov.setText(tok[0].split("-")[1]);
                    nextMaxFreq.setText(tok[1]);
                    nextMinFreq.setText(tok[2]);
                    nextMaxCores.setText(tok[3]);
                    nextMinCores.setText(tok[4]);
                    nextBoostCores.setText(tok[5]);
                    nextBoostFreq.setText(tok[6]);
                    seekBar.setProgress(ind);
                } else {
                    ind++;
                }
            }
        } catch (IndexOutOfBoundsException ignored) {
        } finally {
            if (ind >= ops.length) {
                ((TextView) findViewById(R.id.profileDisplay)).setText(getString(R.string.custom));
                use_custom.setChecked(true);
                seekBar.setEnabled(false);
            }
        }
        sp1.setSelection(preferences.getInt("batteryLT_profile_code", 0));
        sp2.setSelection(preferences.getInt("batteryGT_profile_code", 0));
        LTE.setText(preferences.getInt("batteryLT_edge", 50) + "");
        GTE.setText(preferences.getInt("batteryGT_edge", 50) + "");
    }

    private void apply() {

        final String[] values = new String[]{
                nextGov.getText().toString(),
                nextMaxFreq.getText().toString(),
                nextMinFreq.getText().toString(),
                nextMaxCores.getText().toString(),
                nextMinCores.getText().toString(),
                nextBoostCores.getText().toString(),
                nextBoostFreq.getText().toString(),
                nextMaxFreq.getText().toString()
        };
        final String[] dirs = new String[]{
                Library.GOV0,
                Library.MAX_FREQ0_PATH,
                Library.MIN_FREQ0_PATH,
                Library.MAX_CPUS_ONLINE_PATH,
                Library.MIN_CPUS_ONLINE_PATH,
                Library.BOOSTED_CPUS_PATH,
                "/sys/devices/system/cpu/cpufreq/" + nextGov.getText().toString() + "/boostfreq",
                "/sys/devices/system/cpu/cpufreq/" + nextGov.getText().toString() + "/lmf_active_max_freq"
        };
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(thisActivity);
                dialog.setMessage(getString(R.string.pleaseWait));
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                for (byte b = 0; b < values.length && b < dirs.length; b++) {
                    MyTools.SUhardWrite(values[b], dirs[b]);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                final SharedPreferences.Editor prefEditor = preferences.edit();
                prefEditor.putInt("batteryLT_profile_code", sp1.getSelectedItemPosition());
                prefEditor.putInt("batteryGT_profile_code", sp2.getSelectedItemPosition());
                prefEditor.putString("batteryLT_profile_alias", MyTools.addToArray(profileNames, getString(R.string.custom), 0)[sp1.getSelectedItemPosition()]);
                prefEditor.putString("batteryGT_profile_alias", MyTools.addToArray(profileNames, getString(R.string.custom), 0)[sp2.getSelectedItemPosition()]);
                prefEditor.putInt("batteryLT_edge", Integer.parseInt(((EditText) findViewById(R.id.battery_lt_p)).getText().toString()));
                prefEditor.putInt("batteryGT_edge", Integer.parseInt(((EditText) findViewById(R.id.battery_gt_p)).getText().toString()));

                refresh();
                File SOB1 = new File(MyTools.getDataDir(getApplicationContext()) + File.separator + "scripts" + File.separator + CpuControlFragment.setOnBootFileName);
                File SOB2 = new File(MyTools.getDataDir(getApplicationContext()) + File.separator + "scripts" + File.separator + subActivity1.setOnBootFileName);
                if (SOB1.isFile()) {
                    try {
                        MyTools.completeScriptWith(SOB1, values, dirs);
                        if (SOB2.isFile()) {
                            MyTools.completeScriptWith(SOB2, new String[]{nextBoostFreq.getText().toString()}, new String[]{"/sys/devices/system/cpu/cpufreq/" + nextGov.getText().toString() + "/boostfreq"});
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (sp1.getSelectedItemPosition() + sp2.getSelectedItemPosition() != 0) {
                    prefEditor.putBoolean("EnableProfileAutoSwitch", true);
                } else {
                    prefEditor.putBoolean("EnableProfileAutoSwitch", false);
                }
                char c = '|';
                String customProfileData = governor + c + maxFreq + c + minFreq + c + maxCores + c + minCores + c + cpuBoost + c + boostFreq;
                prefEditor.putString("CustomProfileData", customProfileData);
                prefEditor.commit();

                refresh();

                dialog.dismiss();
                MyTools.toast(getApplicationContext(), R.string.toast_done_succ);
            }
        }.execute();
    }
}
