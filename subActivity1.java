package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


public class subActivity1 extends ListActivity {

    public static final String setOnBootFileName = "99hellscore_gov_tweaks";
    private Context c;
    private Activity a;
    private boolean setOnBoot;
    private File[] files;
    private ArrayList<String> names, values;
    private File scriptsDir;

    public static File getScriptPath(Activity c) {
        String dataDir = MyTools.getDataDir(c);
        return new File(dataDir + File.separator + "scripts" + File.separator + setOnBootFileName);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.listview);
        try {
            getActionBar().setHomeButtonEnabled(true);
        } catch (Exception ignored) {
            
        }
        String s = getIntent().getExtras().getString("key2");
        s = s.replaceFirst(s.charAt(0) + "", "" + Character.toUpperCase(s.charAt(0)));
        getActionBar().setTitle(s + " Tweaks");

        setOnBoot = getIntent().getExtras().getBoolean("key1");

        c = this;
        a = this;

        String dataDir = MyTools.getDataDir(this);
        scriptsDir = new File(dataDir + File.separator + "scripts");

        buildData();
        if (files.length > 0) {

            ListView l = getListView();

            l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                    final Dialog d = new Dialog(c);
                    d.setContentView(R.layout.editbox);
                    d.setTitle(names.get(i));
                    d.setCancelable(true);
                    final EditText e = (EditText) d.findViewById(R.id.editText);
                    final TextView tv = (TextView) view.findViewById(R.id.editText);
                    Button b = (Button) d.findViewById(R.id.button);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String s = null;
                            try {
                                s = e.getText().toString().trim();
                            } catch (Exception e) {
                                d.dismiss();
                            }

                            if (s != null && s.length() >= 1)
                                values.set(i, s);

                            d.dismiss();
                        }
                    });
                    e.setText(tv.getText().toString());
                    d.show();
                }
            });
        } else {
            MyTools.longToast(getApplicationContext(), getString(R.string.governor_noTweak));
            NavUtils.navigateUpTo(this, NavUtils.getParentActivityIntent(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, NavUtils.getParentActivityIntent(this));
                return true;
            case R.id.action_refresh:
                buildData();
                break;
            case R.id.action_apply:
                save();
                if (setOnBoot) {
                    prepareScript(getScriptPath(a), true);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        for (byte b = 0; b < files.length; b++)
            MyTools.write(values.get(b), files[b].toString());
        MyTools.toast(getApplicationContext(), R.string.toast_done_succ);
    }

    private void buildData() {
        files = (new File(getIntent().getExtras().getString("key0"))).listFiles();
        if (files.length > 0) {
            Arrays.sort(files);
            names = new ArrayList<String>();
            values = new ArrayList<String>();

            for (File file : files) {
                names.add(file.getName());
                try {
                    values.add(MyTools.readFile(file.toString()));
                } catch (Exception e) {
                    values.add("n/a");
                }
            }
        }
        setListAdapter(new CustomListAdapter(this, R.layout.listview, names, values));
    }

    private void prepareScript(File file, boolean b) {

        String[] v = new String[values.size()];
        String[] f = new String[v.length];
        for (byte i = 0; i < v.length; i++) {
            v[i] = values.get(i);
            f[i] = files[i].toString();
        }

        try {
            MyTools.fillScript(file, v, f, "null");
            MyTools.createBootAgent(this, scriptsDir);
        } catch (Exception e) {
            e.printStackTrace();
            MyTools.toast(getApplicationContext(), "Failed:2");
            MyTools.removeFile(file);
        }

    }

}
