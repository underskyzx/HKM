package com.themike10452.hellscorekernelmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import eu.chainfire.libsuperuser.Shell;

public class CpuControlFragment extends Fragment {

    public static final String setOnBootFileName = "99hellscore_cpu_settings";
    private static byte firstRun = 1;
    private static int voltageSteps = 14;
    private static int voltageStepValue = 25000;
    private final SeekBar.OnSeekBarChangeListener voltagesListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            TextView tv;

            if (view.findViewById(R.id.voltages).getVisibility() == View.VISIBLE) {
                for (int i = 0; i < vols.length; i++) {
                    if (volSeekBars.get(i).getVisibility() != View.GONE)
                        if (volSeekBars.get(i).getId() == seekBar.getId()) {
                            tv = volDisplay.get(i);
                            tv.setText((Integer.parseInt(vols[i]) +
                                    ((seekBar.getProgress() - voltageSteps / 2) * voltageStepValue)) + "");
                            if (Integer.parseInt(tv.getText().toString()) < 600000)
                                tv.setText("600000");
                        }
                }
            }
        }
    };
    private final View.OnClickListener cpuGovernorButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(AVAIL_GOV, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cpuGovDisplay.setText(AVAIL_GOV[which]);
                }
            }).show();
        }
    };
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MyTools.toast(getActivity(), getString(R.string.toast_longPress_toUse));
        }
    };
    private final CompoundButton.OnCheckedChangeListener setOnBootListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (!b)
                delay.setVisibility(View.INVISIBLE);
            else
                delay.setVisibility(View.VISIBLE);
        }
    };
    private final View.OnClickListener boostedCpusButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String[] MAKO_CORES = {"1", "2", "3", "4"};
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    getActivity());
            builder.setTitle(getString(R.string.alias_boosted_cpus));
            builder.setItems(MAKO_CORES,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boostedCpusDisplay.setText("" + (which + 1));
                        }
                    }
            );
            builder.show();
        }
    };
    private final View.OnClickListener maxCpusButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String[] MAKO_CORES = {"1", "2", "3", "4"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.alias_max_cpus))
                    .setItems(MAKO_CORES,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    maxCpusDisplay.setText("" + (which + 1));
                                    int cmn = Integer.parseInt(minCpusDisplay.getText().toString());
                                    if (cmn - 1 > which)
                                        minCpusDisplay.setText("" + (which + 1));
                                }
                            }
                    ).show();
        }
    };
    private final View.OnClickListener minCpusButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String[] MAKO_CORES = {"1", "2", "3", "4"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.alias_min_cpus))
                    .setItems(MAKO_CORES,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    minCpusDisplay.setText("" + (which + 1));
                                    int cmx = Integer
                                            .parseInt(maxCpusDisplay.getText().toString());
                                    if (cmx - 1 < which)
                                        maxCpusDisplay.setText("" + (which + 1));
                                }
                            }
                    ).show();
        }
    };
    private final View.OnLongClickListener cpuGovernorChangeButtonListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            Intent i = new Intent(getActivity(), subActivity1.class);
            Bundle b = new Bundle();
            try {
                String g = MyTools.readFile(getResources().getString(R.string.GOV0));
                File gov = new File("/sys/devices/system/cpu/cpufreq/" + g);
                if (!gov.exists() || !gov.isDirectory()) throw new Exception();
                MyTools.execTerminalCommand(new String[]{
                        "chmod 666 " + gov.toString() + File.separator + "*"
                });
                b.putString("key0", gov.toString());
                b.putBoolean("key1", setOnBoot.isChecked());
                b.putString("key2", g);
            } catch (Exception e) {
                MyTools.toast(getActivity(), getString(R.string.governor_noTweak));
                return true;
            }
            i.putExtras(b);
            getActivity().startActivity(i);
            return true;
        }
    };
    TextView delay;
    private Boolean susfUnlocked;
    private final View.OnClickListener suspendFreqButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (susfUnlocked)
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.alias_susFreq)
                        .setItems(AVAIL_FREQ,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int which) {
                                        suspendFreqDisplay.setText(scaleDown(AVAIL_FREQ[which]));
                                    }
                                }
                        ).show();
        }
    };

    private final View.OnLongClickListener suspendFreqButtonLCListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {

            susfUnlocked = !susfUnlocked;
            MySQLiteAdapter.insertOrUpdate(
                    getActivity(),
                    DBHelper.SETTINGS_TABLE,
                    new String[]{
                            DBHelper.SETTINGS_TABLE_KEY,
                            DBHelper.SETTINGS_TABLE_COLUMN1,
                            DBHelper.SETTINGS_TABLE_COLUMN2
                    },
                    new String[]{DBHelper.susfreqLock_entry,
                            susfUnlocked.toString(),
                            "null"
                    }
            );

            if (susfUnlocked)
                susfLockState.setText(R.string.TextView_lpl);
            else {
                susfLockState.setText(R.string.TextView_lpu);
                MyTools.longToast(getActivity(), R.string.toast_suspendFreq);
            }

            return true;
        }
    };

    private Button saveProfile, reloadProfile, minCpusButton, maxCpusButton,
            boostedCpusButton, suspendFreqButton, globalOffsetBtn;
    private View view;
    private TextView susfLockState;
    private byte recurse = 0;
    private CheckBox setOnBoot;
    private String dataDir;
    private final View.OnLongClickListener saveProfileListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            final File filesDir = new File(dataDir + File.separator + "files");
            filesDir.mkdir();
            final File file = new File(filesDir.toString() + File.separator + "storedProfile.dat");
            if (file.exists() && !file.isDirectory()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(false)
                        .setTitle(getString(R.string.areYouSure))
                        .setMessage(getString(R.string.overwrite_profile_warning))
                        .setPositiveButton(getString(R.string.button_overwrite), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                prepareScript(file, false);
                            }
                        })
                        .setNegativeButton(getString(R.string.button_abort), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                builder.show();
            } else {
                prepareScript(file, false);
            }
            return true;
        }
    };
    private final View.OnLongClickListener reloadProfileListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            final File file = new File(dataDir + File.separator + "files" + File.separator + "storedProfile.dat");
            if (!file.exists() || file.isDirectory()) {
                MyTools.toast(getActivity(), getString(R.string.toast_no_profile_found));
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.areYouSure))
                        .setNegativeButton(getString(R.string.button_no), null)
                        .setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MyTools.execTerminalCommand(new String[]{file.toString()});
                                for (byte b = 0; b < 4; b++)
                                    refreshAll();
                                MyTools.toast(getActivity(), getString(R.string.toast_profile_restored));
                            }
                        });
                builder.show();
            }

            return true;
        }
    };
    private Button cpuGovernorChangeButton;
    private File setOnBootFile, setOnBootAgent, scriptsDir;
    private SeekBar MAX_FREQ, MIN_FREQ;
    private TextView maxfreqDisplay, minfreqDisplay, cpuGovDisplay, minCpusDisplay;
    private TextView maxCpusDisplay, boostedCpusDisplay, suspendFreqDisplay;
    private Switch cpuIdle_c0, cpuIdle_c1, cpuIdle_c2, cpuIdle_c3;
    private ArrayList<SeekBar> volSeekBars;
    private final View.OnClickListener globalOffsetBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Dialog d = new Dialog(getActivity());
            d.setCanceledOnTouchOutside(true);
            d.setTitle(getString(R.string.button_globalOffset));
            d.setContentView(R.layout.global_offset);

            final SeekBar global = (SeekBar) d.findViewById(R.id.seekBar1);
            global.setMax(voltageSteps);
            global.setProgress(voltageSteps / 2);
            final TextView tv = (TextView) d.findViewById(R.id.title2);
            global.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                int value = 0;

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar,
                                              int progress, boolean fromUser) {
                    value = (seekBar.getProgress() - voltageSteps / 2)
                            * voltageStepValue;
                    String sign = (seekBar.getProgress() >= voltageSteps / 2) ? "+"
                            : "";
                    tv.setText(sign + value + " mv");
                }
            });

            Button ApplyV = (Button) d.findViewById(R.id.resetBtn);
            ApplyV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (global.getProgress() != voltageSteps / 2) {
                        for (SeekBar seekBar : volSeekBars) {
                            seekBar.setProgress(seekBar.getProgress()
                                    + (global.getProgress() - (voltageSteps / 2)));
                        }
                    }
                    d.dismiss();
                }
            });

            d.show();
        }
    };
    private ArrayList<TextView> volDisplay;
    private ArrayList<TextView> freqDisplay;
    private ArrayList<String> vdd_list;
    private String[] AVAIL_FREQ, AVAIL_GOV, vols, freqs;
    private final SeekBar.OnSeekBarChangeListener maxfreqListenner = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
            maxfreqDisplay.setText(scaleDown(AVAIL_FREQ[MAX_FREQ.getProgress()]));
            if (MAX_FREQ.getProgress() < MIN_FREQ.getProgress())
                MIN_FREQ.setProgress(MAX_FREQ.getProgress());
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
        }

    };
    private final SeekBar.OnSeekBarChangeListener minfreqListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            minfreqDisplay.setText(scaleDown(AVAIL_FREQ[MIN_FREQ.getProgress()]));
            if (MAX_FREQ.getProgress() < MIN_FREQ.getProgress())
                MAX_FREQ.setProgress(MIN_FREQ.getProgress());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

    };


    public CpuControlFragment() {
    }

    private static String scaleUp(String s) {
        try {
            int inc = 1000;
            return (Long.parseLong(s) * inc) + "";
        } catch (Exception e) {
            return "####";
        }
    }

    private static String scaleDown(String s) {
        try {
            int dec = 1000;
            return (Long.parseLong(s.trim()) / dec) + "";
        } catch (Exception e) {
            return "####";
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshAll();
                return true;
            case R.id.action_apply:
                saveAll();
                if (setOnBoot.isChecked())
                    prepareScript(setOnBootFile, true);
                else {
                    MyTools.removeFile(setOnBootFile);
                    MyTools.removeFile(new File(subActivity1.setOnBootFileName));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
            String s = MyTools.readFile(this.getString(R.string.AVAIL_FREQ_PATH));
            AVAIL_FREQ = s.split(" ");
        } catch (Exception e) {
            AVAIL_FREQ = new String[]{"n/a", "n/a"};
            MyTools.longToast(getActivity(), "AVAIL_FREQ: " + e.toString());
        }
        try {
            String s = MyTools.readFile(this.getString(R.string.AVAIL_GOV_PATH));
            AVAIL_GOV = s.split(" ");
        } catch (Exception e) {
            AVAIL_GOV = new String[]{"n/a", "n/a"};
            MyTools.longToast(getActivity(), "AVAIL_GOV: " + e.toString());
        }

        vdd_list = MyTools.catToList(this.getString(R.string.VDD_LEVELS));
        freqs = new String[vdd_list.size()];
        vols = new String[vdd_list.size()];
        for (int i = 0; i < freqs.length; i++) {
            if (vdd_list.get(i).contains(":")) {
                String[] tmp = vdd_list.get(i).split(":");
                freqs[i] = tmp[0].trim();
                vols[i] = tmp[1].trim();
            } else {
                freqs[i] = "n/a";
                vols[i] = "n/a";
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cpu_control, container, false);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        refreshAll();
        if (firstRun == 1) {
            firstRun = 0;
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        Shell.SH.run("busybox").get(0);
                        return false;
                    } catch (Exception e) {
                        return true;
                    }
                }

                @Override
                protected void onPostExecute(Boolean failed) {
                    if (failed) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(false)
                                .setMessage(getString(R.string.busybox_notFound_warning))
                                .setNeutralButton(R.string.button_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
                                        easyTracker.send(MapBuilder
                                                        .createEvent("Busybox not found",
                                                                "Busybox decision",
                                                                "Continue without busybox",
                                                                null)
                                                        .build()
                                        );
                                    }
                                })
                                .setPositiveButton(getString(R.string.button_getBusybox), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.stericson_busybox))));
                                        EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
                                        easyTracker.send(MapBuilder
                                                        .createEvent("Busybox not found",
                                                                "Busybox decision",
                                                                "Get busybox",
                                                                null)
                                                        .build()
                                        );
                                    }
                                })
                                .show();
                    }
                }

            }.execute();
        }
    }

    private void initialize() {

        File scalingmaxfreq = new File(getString(R.string.MAX_FREQ0_PATH));
        if (!scalingmaxfreq.canRead() || !scalingmaxfreq.canWrite())
            MyTools.execTerminalCommand(new String[]{"chmod 666 " + scalingmaxfreq.toString()});

        susfLockState = (TextView) view.findViewById(R.id.susfLP);

        setOnBoot = (CheckBox) view.findViewById(R.id.setOnBoot);
        delay = (TextView) view.findViewById(R.id.text_delay);
        dataDir = MyTools.getDataDir(getActivity());
        scriptsDir = new File(dataDir + File.separator + "scripts");
        setOnBootFile = new File(scriptsDir + File.separator + setOnBootFileName);
        setOnBootAgent = new File(this.getString(R.string.setOnBootAgentFile));

        // Profiles
        saveProfile = (Button) view.findViewById(R.id.storeSettingsButton);
        reloadProfile = (Button) view.findViewById(R.id.reloadSettingsButton);

        // CPU GOVERNOR AND CLOCK SPEED
        MAX_FREQ = (SeekBar) view.findViewById(R.id.seekBar);
        MIN_FREQ = (SeekBar) view.findViewById(R.id.seekBar1);
        maxfreqDisplay = (TextView) view.findViewById(R.id.maxfreqDisplay);
        minfreqDisplay = (TextView) view.findViewById(R.id.minfreqDisplay);
        cpuGovernorChangeButton = (Button) view.findViewById(R.id.cpuGovernorButton);
        cpuGovDisplay = (TextView) view.findViewById(R.id.textView3);

        // HOTPLUG
        minCpusDisplay = (TextView) view.findViewById(R.id.minCpusDisplay);
        maxCpusDisplay = (TextView) view.findViewById(R.id.maxCpusDisplay);
        boostedCpusDisplay = (TextView) view.findViewById(R.id.boostedCpusDisplay);
        suspendFreqDisplay = (TextView) view.findViewById(R.id.suspendFreqDisplay);
        minCpusButton = (Button) view.findViewById(R.id.minCpusButton);
        maxCpusButton = (Button) view.findViewById(R.id.maxCpusButton);
        boostedCpusButton = (Button) view.findViewById(R.id.boostedCpusButton);
        suspendFreqButton = (Button) view.findViewById(R.id.suspendFreqButton);

        // CPU IDLE
        cpuIdle_c0 = (Switch) view.findViewById(R.id.cpuIdle_c0);
        cpuIdle_c1 = (Switch) view.findViewById(R.id.cpuIdle_c1);
        cpuIdle_c2 = (Switch) view.findViewById(R.id.cpuIdle_c2);
        cpuIdle_c3 = (Switch) view.findViewById(R.id.cpuIdle_c3);

        // VOLTAGES
        volSeekBars = new ArrayList<SeekBar>();
        freqDisplay = new ArrayList<TextView>();
        volDisplay = new ArrayList<TextView>();

        volSeekBars.clear();
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar2));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar3));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar4));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar5));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar6));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar7));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar8));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar9));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar10));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar11));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar12));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar13));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar14));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar15));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar16));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar17));
        volSeekBars.add((SeekBar) view.findViewById(R.id.seekBar18));


        volDisplay.clear();
        volDisplay.add((TextView) view.findViewById(R.id.vol1));
        volDisplay.add((TextView) view.findViewById(R.id.vol2));
        volDisplay.add((TextView) view.findViewById(R.id.vol3));
        volDisplay.add((TextView) view.findViewById(R.id.vol4));
        volDisplay.add((TextView) view.findViewById(R.id.vol5));
        volDisplay.add((TextView) view.findViewById(R.id.vol6));
        volDisplay.add((TextView) view.findViewById(R.id.vol7));
        volDisplay.add((TextView) view.findViewById(R.id.vol8));
        volDisplay.add((TextView) view.findViewById(R.id.vol9));
        volDisplay.add((TextView) view.findViewById(R.id.vol10));
        volDisplay.add((TextView) view.findViewById(R.id.vol11));
        volDisplay.add((TextView) view.findViewById(R.id.vol12));
        volDisplay.add((TextView) view.findViewById(R.id.vol13));
        volDisplay.add((TextView) view.findViewById(R.id.vol14));
        volDisplay.add((TextView) view.findViewById(R.id.vol15));
        volDisplay.add((TextView) view.findViewById(R.id.vol16));
        volDisplay.add((TextView) view.findViewById(R.id.vol17));

        freqDisplay.clear();
        freqDisplay.add((TextView) view.findViewById(R.id.freq1));
        freqDisplay.add((TextView) view.findViewById(R.id.freq2));
        freqDisplay.add((TextView) view.findViewById(R.id.freq3));
        freqDisplay.add((TextView) view.findViewById(R.id.freq4));
        freqDisplay.add((TextView) view.findViewById(R.id.freq5));
        freqDisplay.add((TextView) view.findViewById(R.id.freq6));
        freqDisplay.add((TextView) view.findViewById(R.id.freq7));
        freqDisplay.add((TextView) view.findViewById(R.id.freq8));
        freqDisplay.add((TextView) view.findViewById(R.id.freq9));
        freqDisplay.add((TextView) view.findViewById(R.id.freq10));
        freqDisplay.add((TextView) view.findViewById(R.id.freq11));
        freqDisplay.add((TextView) view.findViewById(R.id.freq12));
        freqDisplay.add((TextView) view.findViewById(R.id.freq13));
        freqDisplay.add((TextView) view.findViewById(R.id.freq14));
        freqDisplay.add((TextView) view.findViewById(R.id.freq15));
        freqDisplay.add((TextView) view.findViewById(R.id.freq16));
        freqDisplay.add((TextView) view.findViewById(R.id.freq17));

        int n;

        ArrayList<String> tmpVDD = MyTools.catToList(this.getString(R.string.VDD_LEVELS));

        if (tmpVDD.size() <= volSeekBars.size()) {
            for (n = 0; n < tmpVDD.size(); n++) {
                volSeekBars.get(n).setMax(voltageSteps);
                volSeekBars.get(n).setProgress(voltageSteps / 2);
            }
            while (n < volDisplay.size()) {
                volSeekBars.get(n).setVisibility(View.GONE);
                freqDisplay.get(n).setVisibility(View.GONE);
                volDisplay.get(n).setVisibility(View.GONE);
                n++;
            }
        } else {
            view.findViewById(R.id.voltages).setVisibility(View.GONE);
        }

        globalOffsetBtn = (Button) view.findViewById(R.id.globalOffsetBtn);

        if (AVAIL_FREQ.length > 1) {
            MAX_FREQ.setMax(AVAIL_FREQ.length - 1);
            MIN_FREQ.setMax(AVAIL_FREQ.length - 1);
        }

        saveProfile.setOnClickListener(onClickListener);
        reloadProfile.setOnClickListener(onClickListener);
        setOnBoot.setOnCheckedChangeListener(setOnBootListener);
        saveProfile.setOnLongClickListener(saveProfileListener);
        reloadProfile.setOnLongClickListener(reloadProfileListener);
        cpuGovernorChangeButton.setOnLongClickListener(cpuGovernorChangeButtonListener);
        minCpusButton.setOnClickListener(minCpusButtonListener);
        maxCpusButton.setOnClickListener(maxCpusButtonListener);
        boostedCpusButton.setOnClickListener(boostedCpusButtonListener);
        suspendFreqButton.setOnClickListener(suspendFreqButtonListener);
        suspendFreqButton.setOnLongClickListener(suspendFreqButtonLCListener);
        globalOffsetBtn.setOnClickListener(globalOffsetBtnListener);
        cpuGovernorChangeButton.setOnClickListener(cpuGovernorButtonListener);
        MAX_FREQ.setOnSeekBarChangeListener(maxfreqListenner);
        MIN_FREQ.setOnSeekBarChangeListener(minfreqListener);

        if (volSeekBars != null)
            for (SeekBar volSeekBar : volSeekBars) {
                volSeekBar.setOnSeekBarChangeListener(voltagesListener);
            }

    }

    private void saveAll() {

        String gov;
        try {
            gov = cpuGovDisplay.getText().toString();
            EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
            easyTracker.send(MapBuilder
                            .createEvent("CPU_Governor",
                                    "governor_selected",
                                    gov,
                                    null)
                            .build()
            );
        } catch (Exception e) {
            MyTools.longToast(getActivity(), e.toString());
            gov = "n/a";
        }
        String mxc = AVAIL_FREQ[MAX_FREQ.getProgress()];
        String mnc = AVAIL_FREQ[MIN_FREQ.getProgress()];

        String[] govArray = {this.getString(R.string.GOV0),
                this.getString(R.string.GOV1), this.getString(R.string.GOV2),
                this.getString(R.string.GOV3)};
        for (String GOV : govArray)
            MyTools.write(gov, GOV);

        String[] freqArray1 = {
                this.getString(R.string.MAX_FREQ0_PATH),
                this.getString(R.string.MAX_FREQ1_PATH),
                this.getString(R.string.MAX_FREQ2_PATH),
                this.getString(R.string.MAX_FREQ3_PATH)};
        for (String freq : freqArray1)
            MyTools.write(mxc, freq);

        String[] freqArray2 = {
                this.getString(R.string.MIN_FREQ0_PATH),
                this.getString(R.string.MIN_FREQ1_PATH),
                this.getString(R.string.MIN_FREQ2_PATH),
                this.getString(R.string.MIN_FREQ3_PATH)};
        for (String freq : freqArray2)
            MyTools.write(mnc, freq);

        String cmnc = minCpusDisplay.getText().toString();
        String cmxc = maxCpusDisplay.getText().toString();
        String cbsc = boostedCpusDisplay.getText().toString();
        MyTools.write(cmnc, this.getString(R.string.MIN_CPUS_ONLINE_PATH));
        MyTools.write(cmxc, this.getString(R.string.MAX_CPUS_ONLINE_PATH));
        MyTools.write(cbsc, this.getString(R.string.BOOSTED_CPUS_PATH));

        if (susfUnlocked) {
            String susf;
            try {
                susf = suspendFreqDisplay.getText().toString();
            } catch (Exception e) {
                MyTools.longToast(getActivity(), e.toString());
                susf = "n/a";
            }
            susf = scaleUp(susf);
            MyTools.write(susf, this.getString(R.string.SUSPEND_FREQ_PATH));
        }

        Switch[] s = {cpuIdle_c0, cpuIdle_c1, cpuIdle_c2, cpuIdle_c3};
        String[] t = {this.getString(R.string.CPU_IDLE_C0_PATH),
                this.getString(R.string.CPU_IDLE_C1_PATH),
                this.getString(R.string.CPU_IDLE_C2_PATH),
                this.getString(R.string.CPU_IDLE_C3_PATH)};

        for (int i = 0; i < s.length; i++) {

            String tmp = "0";

            if (s[i].isChecked())
                tmp = "1";

            for (int j = 0; j <= 3; j++)
                MyTools.write(tmp, this.getString(R.string.CPU_IDLE_TRUNK_PATH)
                        + "cpu" + j + t[i]);
        }

        for (int i = 0; i < volDisplay.size(); i++) {
            try {
                String vdd = freqDisplay.get(i).getText().toString() + " "
                        + volDisplay.get(i).getText().toString();
                MyTools.write(vdd, this.getString(R.string.VDD_LEVELS));
            } catch (Exception e) {
                MyTools.longToast(getActivity(), e.toString());
            }
        }

        MyTools.toast(getActivity(), R.string.toast_done_succ);
    }

    void refreshAll() {

        try {
            cpuGovDisplay.setText(MyTools.readFile(this.getString(R.string.GOV0)));
        } catch (Exception e) {
            cpuGovDisplay.setText("n/a");
        }
        String CURRENT_MAX_FREQ;
        try {
            MyTools.execTerminalCommand(new String[]{"chmod 666 " + this.getString(R.string.MAX_FREQ0_PATH)});
            CURRENT_MAX_FREQ = MyTools.readFile(this.getString(R.string.MAX_FREQ0_PATH).trim());
        } catch (Exception e) {
            CURRENT_MAX_FREQ = "n/a";
        }
        String CURRENT_MIN_FREQ;
        try {
            CURRENT_MIN_FREQ = MyTools.readFile(this.getString(R.string.MIN_FREQ0_PATH).trim());
        } catch (Exception e) {
            CURRENT_MIN_FREQ = "n/a";
        }

        maxfreqDisplay.setText(scaleDown(CURRENT_MAX_FREQ));
        minfreqDisplay.setText(scaleDown(CURRENT_MIN_FREQ));

        for (int i = 0; i < AVAIL_FREQ.length; i++) {
            if (CURRENT_MIN_FREQ.trim().contains(AVAIL_FREQ[i].trim()))
                MIN_FREQ.setProgress(i);

            if (CURRENT_MAX_FREQ.trim().contains(AVAIL_FREQ[i].trim()))
                MAX_FREQ.setProgress(i);
        }

        try {
            minCpusDisplay.setText(MyTools.readFile(this
                    .getString(R.string.MIN_CPUS_ONLINE_PATH)));
        } catch (Exception e) {
            minCpusDisplay.setText("n/a");
        }
        try {
            maxCpusDisplay.setText(MyTools.readFile(this
                    .getString(R.string.MAX_CPUS_ONLINE_PATH)));
        } catch (Exception e) {
            maxCpusDisplay.setText("n/a");
        }
        try {
            boostedCpusDisplay.setText(MyTools.readFile(this
                    .getString(R.string.BOOSTED_CPUS_PATH)));
        } catch (Exception e) {
            boostedCpusDisplay.setText("n/a");
        }
        try {
            suspendFreqDisplay.setText(scaleDown(MyTools.readFile(this.getString(R.string.SUSPEND_FREQ_PATH))));
        } catch (Exception e) {
            suspendFreqDisplay.setText("n/a");
        }

        Switch[] s = {cpuIdle_c0, cpuIdle_c1, cpuIdle_c2, cpuIdle_c3};
        String[] t = {this.getString(R.string.CPU_IDLE_C0_PATH),
                this.getString(R.string.CPU_IDLE_C1_PATH),
                this.getString(R.string.CPU_IDLE_C2_PATH),
                this.getString(R.string.CPU_IDLE_C3_PATH)};

        for (int i = 0; i < s.length; i++) {
            String ss;
            try {
                ss = MyTools.readFile(this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu0" + t[i]);
            } catch (Exception e) {
                ss = "n/a";
            }
            if (ss.equals("1")) {
                s[i].setChecked(true);
            } else {
                s[i].setChecked(false);
            }
        }

        vdd_list = MyTools.catToList(this.getString(R.string.VDD_LEVELS));

        if (!vdd_list.get(0).equals("ladyGaga") && vdd_list.size() <= volDisplay.size()) {

            freqs = new String[vdd_list.size()];
            vols = new String[vdd_list.size()];
            for (int i = 0; i < freqs.length; i++) {
                if (vdd_list.get(i).contains(":")) {
                    String[] tmp = vdd_list.get(i).split(":");
                    freqs[i] = tmp[0].trim();
                    vols[i] = tmp[1].trim();
                } else {
                    freqs[i] = "n/a";
                    vols[i] = "n/a";
                }
            }

            for (int i = 0; i < freqs.length; i++) {
                volDisplay.get(i).setText(vols[i]);
                freqDisplay.get(i).setText(freqs[i]);
            }
            for (SeekBar sb : volSeekBars) {
                sb.setProgress(voltageSteps / 2);
            }

        } else {
            if (vdd_list.size() > volDisplay.size())
                MyTools.longToast(getActivity(), getString(R.string.toast_voltage_steps_overloaded));
            view.findViewById(R.id.voltages).setVisibility(View.GONE);
        }

        setOnBoot.setChecked(
                setOnBootFile.exists() && setOnBootAgent.exists() &&
                        !setOnBootFile.isDirectory() && !setOnBootAgent.isDirectory()
        );

        if (!setOnBoot.isChecked())
            delay.setVisibility(View.INVISIBLE);
        else
            delay.setVisibility(View.VISIBLE);

        String[] s0 = MySQLiteAdapter.select(getActivity(),
                DBHelper.SETTINGS_TABLE,
                DBHelper.SETTINGS_TABLE_KEY,
                DBHelper.susfreqLock_entry,
                new String[]{"value"});

        try {
            susfUnlocked = Boolean.parseBoolean(s0[0]);
        } catch (Exception e) {
            susfUnlocked = false;
        }

        if (susfUnlocked)
            susfLockState.setText(R.string.TextView_lpl);
        else
            susfLockState.setText(R.string.TextView_lpu);

        if ((maxfreqDisplay.getText().toString().equals(minfreqDisplay.getText().toString())
                || maxfreqDisplay.getText().toString().contains("##"))
                && recurse < 4) {
            recurse++;
            refreshAll();
        }

    }

    private void prepareScript(File file, boolean f) {

        try {

            if (!scriptsDir.exists())
                scriptsDir.mkdirs();


            String tmpSusf = scaleUp(suspendFreqDisplay.getText().toString());
            if (!susfUnlocked)
                tmpSusf = null;

            String[] values = new String[]{
                    cpuGovDisplay.getText().toString(),
                    cpuGovDisplay.getText().toString(),
                    cpuGovDisplay.getText().toString(),
                    cpuGovDisplay.getText().toString(),

                    AVAIL_FREQ[MAX_FREQ.getProgress()],
                    AVAIL_FREQ[MAX_FREQ.getProgress()],
                    AVAIL_FREQ[MAX_FREQ.getProgress()],
                    AVAIL_FREQ[MAX_FREQ.getProgress()],

                    AVAIL_FREQ[MIN_FREQ.getProgress()],
                    AVAIL_FREQ[MIN_FREQ.getProgress()],
                    AVAIL_FREQ[MIN_FREQ.getProgress()],
                    AVAIL_FREQ[MIN_FREQ.getProgress()],

                    minCpusDisplay.getText().toString(),
                    maxCpusDisplay.getText().toString(),
                    boostedCpusDisplay.getText().toString(),
                    tmpSusf,

                    MyTools.parseIntFromBoolean(cpuIdle_c0.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c0.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c0.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c0.isChecked()),

                    MyTools.parseIntFromBoolean(cpuIdle_c1.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c1.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c1.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c1.isChecked()),

                    MyTools.parseIntFromBoolean(cpuIdle_c2.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c2.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c2.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c2.isChecked()),

                    MyTools.parseIntFromBoolean(cpuIdle_c3.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c3.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c3.isChecked()),
                    MyTools.parseIntFromBoolean(cpuIdle_c3.isChecked()),
            };

            String[] destinations = new String[]{
                    this.getString(R.string.GOV0),
                    this.getString(R.string.GOV1),
                    this.getString(R.string.GOV2),
                    this.getString(R.string.GOV3),

                    this.getString(R.string.MAX_FREQ0_PATH),
                    this.getString(R.string.MAX_FREQ1_PATH),
                    this.getString(R.string.MAX_FREQ2_PATH),
                    this.getString(R.string.MAX_FREQ3_PATH),

                    this.getString(R.string.MIN_FREQ0_PATH),
                    this.getString(R.string.MIN_FREQ1_PATH),
                    this.getString(R.string.MIN_FREQ2_PATH),
                    this.getString(R.string.MIN_FREQ3_PATH),

                    this.getString(R.string.MIN_CPUS_ONLINE_PATH),
                    this.getString(R.string.MAX_CPUS_ONLINE_PATH),
                    this.getString(R.string.BOOSTED_CPUS_PATH),
                    this.getString(R.string.SUSPEND_FREQ_PATH),

                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu0" +
                            this.getString(R.string.CPU_IDLE_C0_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu1" +
                            this.getString(R.string.CPU_IDLE_C0_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu2" +
                            this.getString(R.string.CPU_IDLE_C0_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu3" +
                            this.getString(R.string.CPU_IDLE_C0_PATH),

                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu0" +
                            this.getString(R.string.CPU_IDLE_C1_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu1" +
                            this.getString(R.string.CPU_IDLE_C1_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu2" +
                            this.getString(R.string.CPU_IDLE_C1_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu3" +
                            this.getString(R.string.CPU_IDLE_C1_PATH),

                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu0" +
                            this.getString(R.string.CPU_IDLE_C2_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu1" +
                            this.getString(R.string.CPU_IDLE_C2_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu2" +
                            this.getString(R.string.CPU_IDLE_C2_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu3" +
                            this.getString(R.string.CPU_IDLE_C2_PATH),

                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu0" +
                            this.getString(R.string.CPU_IDLE_C3_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu1" +
                            this.getString(R.string.CPU_IDLE_C3_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu2" +
                            this.getString(R.string.CPU_IDLE_C3_PATH),
                    this.getString(R.string.CPU_IDLE_TRUNK_PATH) + "cpu3" +
                            this.getString(R.string.CPU_IDLE_C3_PATH),
            };

            String flags = ":delay45:";

            if (!f)
                flags = "null";


            MyTools.fillScript(file, values, destinations, flags);

            if ((view.findViewById(R.id.voltages)).getVisibility() != View.GONE) {
                ArrayList<String> vals = new ArrayList<String>();
                for (int i = 0; i < freqDisplay.size(); i++) {
                    if (freqDisplay.get(i).getVisibility() != View.GONE)
                        vals.add(freqDisplay.get(i).getText().toString() + " " + volDisplay.get(i).getText().toString());
                }
                MyTools.completeScriptWith(file, vals, new String[]{this.getString(R.string.VDD_LEVELS)});

                File gov = new File("/sys/devices/system/cpu/cpufreq/" + MyTools.readFile(getResources().getString(R.string.GOV0)));
                if (gov.exists() && gov.isDirectory()) {
                    File[] files = gov.listFiles();
                    if (files != null) {
                        Arrays.sort(files);
                        String[] Values = new String[files.length];
                        String[] Aliases = new String[values.length];

                        for (byte b = 0; b < files.length; b++) {
                            Aliases[b] = files[b].toString();
                            try {
                                String v = MyTools.readFile(files[b].toString());
                                Values[b] = v;
                            } catch (Exception e) {
                                Values[b] = "null";
                            }
                        }
                        if (f) {
                            MyTools.fillScript(subActivity1.getScriptPath(getActivity()), Values, Aliases, "null");
                        } else {
                            MyTools.completeScriptWith(file, Values, Aliases);
                            MyTools.toast(getActivity(), getString(R.string.toast_done));
                        }

                        if (f) {
                            MyTools.createBootAgent(getActivity(), scriptsDir);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (f)
                setOnBootFailed();
            else
                MyTools.toast(getActivity(), getString(R.string.toast_failed_saveProfile));
        }
    }

    private void setOnBootFailed() {
        final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setMessage(getString(R.string.toast_failed_setOnBoot));
        b.setCancelable(false);
        b.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                refreshAll();
            }
        });
        setOnBoot.setChecked(false);
        b.show();
    }

}