package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mike on 4/12/2014.
 */
public class MonitoringActivity extends Activity {

    public static boolean inForground;
    private String[][] battery_info, cpu_info;
    private String TIS;
    private List<String> time_in_state;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        try {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.button_color));
        } catch (Exception ingnored) {
        }
        battery_info = new String[][]{
                {getString(R.string.BATTERY_SYSFS) + "/temp", "n/a"},
                {getString(R.string.BATTERY_SYSFS) + "/health", "n/a"}
        };
        cpu_info = new String[][]{
                {
                        getString(R.string.CPU_SYSFS) + "/cpu%s/online", "n/a", "n/a", "n/a", "n/a"
                },
                {
                        getString(R.string.CPU_SYSFS) + "/cpu%s/cpufreq/scaling_max_freq", "-21", "-21", "-21", "-21"
                },
                {
                        getString(R.string.CPU_SYSFS) + "/cpu%s/cpufreq/scaling_min_freq", "-21", "-21", "-21", "-21"
                },
                {
                        getString(R.string.CPU_SYSFS) + "/cpu%s/cpufreq/scaling_cur_freq", "n/a", "n/a", "n/a", "n/a"
                },
                {
                        getString(R.string.CPU_TEMP_PATH), "n/a"
                }
        };
        TIS = getString(R.string.CPU_SYSFS) + "/cpu0/cpufreq/statis/time_in_state";
    }

    @Override
    protected void onResume() {
        super.onResume();
        inForground = true;
        update();
    }

    @Override
    protected void onPause() {
        super.onPause();
        inForground = false;
    }

    private void update() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                int x = MyTools.catInt(getString(R.string.MSM_THERMAL_PATH), -1);
                if (x != -1)
                    ((ProgressBar) findViewById(R.id.cpu_temp_progress)).setMax(x);
                for (byte b = 1; b < cpu_info[0].length; b++) {
                    MyTools.SUhardWrite("1", String.format(cpu_info[0][0], b - 1));
                    cpu_info[0][b] = MyTools.readFile(String.format(cpu_info[0][0], b - 1));
                    for (byte b2 = 1; b2 < cpu_info[1].length; b2++) {
                        cpu_info[1][b2] = MyTools.catInt(String.format(cpu_info[1][0], b2 - 1), -21) + "";
                        cpu_info[2][b2] = MyTools.catInt(String.format(cpu_info[2][0], b2 - 1), -21) + "";
                    }
                }

                while (inForground) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    battery_info[0][1] = MyTools.readFile(battery_info[0][0]);
                    battery_info[1][1] = MyTools.readFile(battery_info[1][0]);
                    cpu_info[4][1] = MyTools.readFile(cpu_info[4][0]);
                    for (byte b1 = 1; b1 < cpu_info[3].length; b1++) {
                        cpu_info[3][b1] = MyTools.catInt(String.format(cpu_info[3][0], b1 - 1), -21) + "";
                    }
                    time_in_state = MyTools.suCatToList(TIS);
                    publishProgress();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);

                ((TextView) findViewById(R.id.cpu0_maxFreq)).setText(cpu_info[1][1]);
                ((TextView) findViewById(R.id.cpu1_maxFreq)).setText(cpu_info[1][1]);
                ((TextView) findViewById(R.id.cpu2_maxFreq)).setText(cpu_info[1][1]);
                ((TextView) findViewById(R.id.cpu3_maxFreq)).setText(cpu_info[1][1]);

                ((TextView) findViewById(R.id.cpu0_minFreq)).setText(cpu_info[2][1]);
                ((TextView) findViewById(R.id.cpu1_minFreq)).setText(cpu_info[2][1]);
                ((TextView) findViewById(R.id.cpu2_minFreq)).setText(cpu_info[2][1]);
                ((TextView) findViewById(R.id.cpu3_minFreq)).setText(cpu_info[2][1]);

                if (Integer.parseInt(cpu_info[1][1]) > 0) {
                    ((ProgressBar) findViewById(R.id.cpu0_freq_progress)).setMax(Integer.parseInt(cpu_info[1][1]));
                    ((ProgressBar) findViewById(R.id.cpu1_freq_progress)).setMax(Integer.parseInt(cpu_info[1][1]));
                    ((ProgressBar) findViewById(R.id.cpu2_freq_progress)).setMax(Integer.parseInt(cpu_info[1][1]));
                    ((ProgressBar) findViewById(R.id.cpu3_freq_progress)).setMax(Integer.parseInt(cpu_info[1][1]));
                }

                String offline = getString(R.string.offline);

                if (!battery_info[0][1].contains("n/a")) {
                    double d = Double.parseDouble(battery_info[0][1]);
                    if (d > 99)
                        d /= 10;
                    ((TextView) findViewById(R.id.battery_temp_display)).setText(d + " °C");
                    ((ProgressBar) findViewById(R.id.battery_temp_progress)).setProgress((int) d);
                } else {
                    ((TextView) findViewById(R.id.battery_temp_display)).setText("n/a");
                    ((ProgressBar) findViewById(R.id.battery_temp_progress)).setProgress(0);
                }
                ((TextView) findViewById(R.id.battery_health_display)).setText(battery_info[1][1]);

                if (!cpu_info[4][1].contains("n/a")) {
                    int d = Integer.parseInt(cpu_info[4][1]);
                    ((TextView) findViewById(R.id.cpu_temp_display)).setText(d + " °C");
                    ((ProgressBar) findViewById(R.id.cpu_temp_progress)).setProgress(d);
                } else {
                    ((TextView) findViewById(R.id.battery_temp_display)).setText("n/a");
                    ((ProgressBar) findViewById(R.id.battery_temp_progress)).setProgress(0);
                }

                if (cpu_info[3][1].contains("-")) {
                    ((ProgressBar) findViewById(R.id.cpu0_freq_progress)).setProgress(0);
                    ((TextView) findViewById(R.id.cpu0_freq_display)).setText(offline);
                } else {
                    ((ProgressBar) findViewById(R.id.cpu0_freq_progress)).setProgress(Integer.parseInt(cpu_info[3][1]));
                    ((TextView) findViewById(R.id.cpu0_freq_display)).setText(cpu_info[3][1]);
                }
                if (cpu_info[3][2].contains("-")) {
                    ((ProgressBar) findViewById(R.id.cpu1_freq_progress)).setProgress(0);
                    ((TextView) findViewById(R.id.cpu1_freq_display)).setText(offline);
                } else {
                    ((ProgressBar) findViewById(R.id.cpu1_freq_progress)).setProgress(Integer.parseInt(cpu_info[3][2]));
                    ((TextView) findViewById(R.id.cpu1_freq_display)).setText(cpu_info[3][2]);
                }
                if (cpu_info[3][3].contains("-")) {
                    ((ProgressBar) findViewById(R.id.cpu2_freq_progress)).setProgress(0);
                    ((TextView) findViewById(R.id.cpu2_freq_display)).setText(offline);
                } else {
                    ((ProgressBar) findViewById(R.id.cpu2_freq_progress)).setProgress(Integer.parseInt(cpu_info[3][3]));
                    ((TextView) findViewById(R.id.cpu2_freq_display)).setText(cpu_info[3][3]);
                }
                if (cpu_info[3][4].contains("-")) {
                    ((ProgressBar) findViewById(R.id.cpu3_freq_progress)).setProgress(0);
                    ((TextView) findViewById(R.id.cpu3_freq_display)).setText(offline);
                } else {
                    ((ProgressBar) findViewById(R.id.cpu3_freq_progress)).setProgress(Integer.parseInt(cpu_info[3][4]));
                    ((TextView) findViewById(R.id.cpu3_freq_display)).setText(cpu_info[3][4]);
                }

            }
        }.execute();
    }
}
