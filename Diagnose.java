package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class Diagnose extends Activity {

    String dataDir;
    private CheckBox agentExists, agentCanExecute, cpuExists, cpuCanExecute, gpuExists,
            gpuCanExecute, miscExists, miscCanExecute, lcdExists, lcdCanExecute, touchExists,
            touchCanExecute, govExists, govCanExecute;
    private TextView bootLog;
    private Button fix, recBoot;
    private String v = "#", bb = "BB";
    private Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        thisActivity = this;

        try {
            getActionBar().setHomeButtonEnabled(true);
        } catch (Exception ingnored) {
        }

        dataDir = MyTools.getDataDir(this);
        agentExists = (CheckBox) findViewById(R.id.checkBox1a);
        agentCanExecute = (CheckBox) findViewById(R.id.checkBox1b);
        cpuExists = (CheckBox) findViewById(R.id.checkBox2a);
        cpuCanExecute = (CheckBox) findViewById(R.id.checkBox2b);
        gpuExists = (CheckBox) findViewById(R.id.checkBox3a);
        gpuCanExecute = (CheckBox) findViewById(R.id.checkBox3b);
        miscExists = (CheckBox) findViewById(R.id.checkBox4a);
        miscCanExecute = (CheckBox) findViewById(R.id.checkBox4b);
        lcdExists = (CheckBox) findViewById(R.id.checkBox5a);
        lcdCanExecute = (CheckBox) findViewById(R.id.checkBox5b);
        touchExists = (CheckBox) findViewById(R.id.checkBox6a);
        touchCanExecute = (CheckBox) findViewById(R.id.checkBox6b);
        govExists = (CheckBox) findViewById(R.id.checkBox7a);
        govCanExecute = (CheckBox) findViewById(R.id.checkBox7b);
        bootLog = (TextView) findViewById(R.id.bootLog);
        fix = (Button) findViewById(R.id.fixButton);
        recBoot = (Button) findViewById(R.id.recBoot);

    }

    @Override
    public void onStart() {
        super.onStart();
        new AsyncTask<Void, Void, Void>() {
            String s = "";
            List<String>
                    list1 = null
                    ,
                    list2 = null
                    ,
                    list3 = null
                    ,
                    list4 = null;

            @Override
            protected Void doInBackground(Void... voids) {
                String scriptsDir = dataDir + File.separator + "scripts";
                list1 = Shell.SU.run("ls -l /system/etc/init.d/");
                list2 = Shell.SU.run("ls -l " + scriptsDir);
                list3 = Shell.SU.run(new String[]{
                        "echo init.d:",
                        "cat /system/etc/init.d/*",
                        "echo scripts:",
                        "cat " + scriptsDir + "/*"
                });
                list4 = Shell.SH.run(new String[]{
                        "echo Last boot log:",
                        "cat /sdcard/HKM.log"
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void vd) {
                File report = new File(Environment.getExternalStorageDirectory() +
                        File.separator + getString(R.string.report_file));

                try {
                    report.delete();
                    report.createNewFile();
                    if (report.exists() && report.isFile()) {
                        PrintWriter p = new PrintWriter(new FileWriter(report, true));
                        if (list1 != null) {
                            p.println("\n## init.d permissions ##");
                            for (String ss : list1)
                                p.println(ss);
                            p.println();
                        }
                        if (list2 != null) {
                            p.println("\n## scripts permissions ##");
                            for (String ss : list2)
                                p.println(ss);
                            p.println();
                        }
                        if (list3 != null) {
                            p.println("\n## contents ##");
                            for (String ss : list3)
                                p.println(ss);
                            for (String ss : list4) {
                                s = s + ss + "\n";
                                p.println(ss);
                            }
                        }
                        p.flush();
                        p.close();
                        ((TextView) findViewById(R.id.bootLog)).setText(s);
                        new AlertDialog.Builder(thisActivity)
                                .setMessage(getString(R.string.diagnosis_done)
                                        .replace("###", Environment.getExternalStorageDirectory() +
                                                File.separator + getString(R.string.report_file))
                                        .replace("@@@", getString(R.string.my_email)))
                                .show();

                    }
                } catch (IOException ignored) {
                }
            }
        }.execute();

        refresh();
        final Activity activity = this;
        if (fix != null) {
            fix.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            Shell.SU.run("chmod 775 " + getString(R.string.setOnBootAgentFile));
                            Shell.SU.run("chmod 775 " + dataDir + File.separator + "scripts" + File.separator + "*");
                            return null;
                        }

                        @Override
                        public void onPostExecute(Boolean b) {
                            MyTools.toast(activity, activity.getString(R.string.toast_done));
                            refresh();
                        }
                    }.execute();

                }
            });
        }
        if (recBoot != null) {
            recBoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            try {
                                MyTools.createBootAgent(activity, new File(activity.getString(R.string.setOnBootAgentFile)));
                            } catch (Exception e) {
                                MyTools.longToast(activity, e.toString());
                                return false;
                            }
                            return true;
                        }

                        @Override
                        public void onPostExecute(Boolean completed) {
                            if (completed)
                                MyTools.toast(activity, activity.getString(R.string.toast_done_succ));
                            else
                                MyTools.toast(activity, activity.getString(R.string.toast_failed));
                            refresh();
                        }
                    }.execute();

                }
            });
        }
    }

    private void refresh() {

        File scriptsDir = new File(dataDir + File.separator + "scripts");

        File bootAgent = new File(getString(R.string.setOnBootAgentFile));
        setCheckbox(agentExists, agentCanExecute, bootAgent);

        File cpuBoot = new File(scriptsDir + File.separator + CpuControlFragment.setOnBootFileName);
        setCheckbox(cpuExists, cpuCanExecute, cpuBoot);

        File gpuBoot = new File(scriptsDir + File.separator + GpuControlFragment.setOnBootFileName);
        setCheckbox(gpuExists, gpuCanExecute, gpuBoot);

        File miscBoot = new File(scriptsDir + File.separator + MiscFragment.setOnBootFileName);
        setCheckbox(miscExists, miscCanExecute, miscBoot);

        File lcdBoot = new File(scriptsDir + File.separator + GammaControlFragment.setOnBootFileName);
        setCheckbox(lcdExists, lcdCanExecute, lcdBoot);

        File touchBoot = new File(scriptsDir + File.separator + TouchControlFragment.setOnBootFileName);
        setCheckbox(touchExists, touchCanExecute, touchBoot);

        File govBoot = new File(scriptsDir + File.separator + subActivity1.setOnBootFileName);
        setCheckbox(govExists, govCanExecute, govBoot);


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (Shell.SU.available()) {
                    try {
                        v = (Shell.SU.run("su -v")).get(0);
                    } catch (Exception e) {
                        Log.e("TAG", e.toString());
                        v = "n/a";
                    }
                    try {
                        bb = (Shell.SU.run("busybox")).get(0);
                    } catch (Exception e) {
                        Log.e("TAG", e.toString());
                        bb = "n/a";
                    }
                } else {
                    v = "No SU, how did you get here? :o";
                    bb = "n/a";
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void vv) {
                ((TextView) findViewById(R.id.suDisplay)).setText(v);
                ((TextView) findViewById(R.id.bbDisplay)).setText(bb);
            }
        }.execute();


    }

    private void setCheckbox(CheckBox checkbox1, CheckBox checkbox2, File file) {
        if (checkbox1 != null) {
            checkbox1.setClickable(false);
            checkbox1.setChecked(file.exists() && file.isFile() && !file.isDirectory());
        }
        if (checkbox2 != null) {
            checkbox2.setClickable(false);
            checkbox2.setChecked(file.exists() && file.isFile() && !file.isDirectory() && file.canExecute());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diagnose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }
        if (id == R.id.action_help) {
            help();
            return true;
        }
        return false;
    }

    private void help() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true)
                .setTitle(getString(R.string.action_help))
                .setMessage(getString(R.string.diagnosis_help))
                .show();
    }

}
