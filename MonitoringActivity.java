package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.util.ArrayList;

/**
 * Created by Mike on 4/12/2014.
 */
public class MonitoringActivity extends Activity {

    public static boolean inForground;
    private String[][] battery_info, cpu_info;
    private ArrayList<String> time_in_state;

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
        } catch (Exception ingnored) {
        }
        battery_info = new String[][]{
                {Library.BATTERY_SYSFS + "/temp", "n/a"},
                {Library.BATTERY_SYSFS + "/health", "n/a"}
        };
        cpu_info = new String[][]{
                {
                        Library.CPU_SYSFS + "/cpu%s/online", "n/a", "n/a", "n/a", "n/a"
                },
                {
                        Library.CPU_SYSFS + "/cpu%s/cpufreq/scaling_max_freq", "-21", "-21", "-21", "-21"
                },
                {
                        Library.CPU_SYSFS + "/cpu%s/cpufreq/scaling_min_freq", "-21", "-21", "-21", "-21"
                },
                {
                        Library.CPU_SYSFS + "/cpu%s/cpufreq/scaling_cur_freq", "n/a", "n/a", "n/a", "n/a"
                },
                {
                        Library.CPU_TEMP_PATH, "n/a"
                }
        };
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
                int x = MyTools.catInt(Library.MSM_THERMAL_PATH, -1);
                if (x != -1)
                    ((ProgressBar) findViewById(R.id.cpu_temp_progress)).setMax(x);
                for (byte b = 1; b < cpu_info[0].length; b++) {
                    MyTools.SUhardWrite("1", String.format(cpu_info[0][0], b - 1));
                    cpu_info[0][b] = MyTools.readFile(String.format(cpu_info[0][0], b - 1), "n/a");
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
                    battery_info[0][1] = MyTools.readFile(battery_info[0][0], "n/a");
                    battery_info[1][1] = MyTools.readFile(battery_info[1][0], "n/a");
                    cpu_info[4][1] = MyTools.readFile(cpu_info[4][0], "n/a");
                    for (byte b1 = 1; b1 < cpu_info[3].length; b1++) {
                        cpu_info[3][b1] = MyTools.catInt(String.format(cpu_info[3][0], b1 - 1), -21) + "";
                    }
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

                LinearLayout tis = (LinearLayout) findViewById(R.id.time_in_state);
                time_in_state = MyTools.readToList(Library.CPU_TIME_IN_STATE);
                time_in_state.add(0, "Sleep " + (SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()) / 10);
                tis.removeAllViews();
                TimeInStateAdapter adapter = new TimeInStateAdapter(getApplicationContext(), R.layout.time_in_state_layout, time_in_state);
                for (int i = 0; i < time_in_state.size(); i++) {
                    tis.addView(adapter.getView(i, null, null));
                }

            }
        }.execute();
    }
}

class State {
    public int time;
    public String freq;

    public State(String entry) {
        freq = entry.split(" ")[0];
        time = Integer.parseInt(entry.split(" ")[1]);
    }
}

class TimeInStateAdapter extends ArrayAdapter<String> {

    public long sum;
    private ArrayList<String> stats;
    private Context context;

    public TimeInStateAdapter(Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
        this.context = context;
        stats = objects;
        sum = 0;
        for (String str : objects) {
            sum += new State(str).time;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.time_in_state_layout, null);

        State state = new State(stats.get(position++));

        TextView freq = (TextView) convertView.findViewById(R.id.freqDisplay);
        TextView perc = (TextView) convertView.findViewById(R.id.perc);
        TextView time = (TextView) convertView.findViewById(R.id.time);
        ProgressBar bar = (ProgressBar) convertView.findViewById(R.id.progressBar);

        freq.setText(state.freq);

        int i = (int) ((state.time * 100) / sum);

        perc.setText(i + "%");

        time.setText(format(state.time / 100));
        //time.setText(state.time + "");

        bar.setMax(100);
        bar.setProgress(i);

        return convertView;
    }

    private String format(long t) {
        String format = "%s:%s:%s";
        int s = 0, m = 0, h = 0;
        long i = t;
        while ((i -= 3600) >= 60) {
            h++;
        }
        i = t - (h * 3600);
        while ((i -= 60) >= 0) {
            m++;
        }
        i = t - (h * 3600) - (m * 60);
        while ((i--) >= 0) {
            s++;
        }

        if (s < 10) {
            if (m < 10) {
                if (h < 10) {
                    return String.format(format, "0" + h, "0" + m, "0" + s);
                } else {
                    return String.format(format, h, "0" + m, "0" + "0" + s);
                }
            } else {
                if (h < 10) {
                    return String.format(format, "0" + h, m, "0" + s);
                } else {
                    return String.format(format, h, m, "0" + s);
                }
            }
        } else {
            if (m < 10) {
                if (h < 10) {
                    return String.format(format, "0" + h, "0" + m, s);
                } else {
                    return String.format(format, h, "0" + m, s);
                }
            } else {
                if (h < 10) {
                    return String.format(format, "0" + h, m, s);
                } else {
                    return String.format(format, h, m, s);
                }
            }
        }
    }

}
