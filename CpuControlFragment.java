package com.themike10452.hellscorekernelmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import eu.chainfire.libsuperuser.Shell;

public class CpuControlFragment extends Fragment {

    public static final String setOnBootFileName = "99hellscore_cpu_settings";
    private static byte firstRun = 1;
    private static int voltageSteps = 28;
    private static int voltageStepValue = 12500;
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
                        VoltagesAdapter.incVols((global.getProgress() - (voltageSteps / 2)) * voltageStepValue);
                    }
                    d.dismiss();
                }
            });

            d.show();
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
            final String[] MAKO_CORES = {"0", "1", "2", "3", "4"};
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    getActivity());
            builder.setTitle(getString(R.string.alias_boosted_cpus));
            builder.setItems(MAKO_CORES,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boostedCpusDisplay.setText("" + (which));
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
                String g = MyTools.readFile(Library.GOV0);
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
    View.OnClickListener screenOffMaxButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Screen_Off Max")
                    .setItems(AVAIL_FREQ,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    screenOffMaxDisplay.setText(scaleDown(AVAIL_FREQ[i]));
                                }
                            }
                    )
                    .show();
        }
    };
    private Switch switch_scroff_single_core;
    private Boolean susfUnlocked;
    private View.OnClickListener suspendFreqButtonListener = new View.OnClickListener() {
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
            boostedCpusButton, suspendFreqButton, screenOffMaxButton, globalOffsetBtn;
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
    private TextView maxCpusDisplay, boostedCpusDisplay, suspendFreqDisplay, screenOffMaxDisplay;
    private Switch cpuIdle_c0, cpuIdle_c1, cpuIdle_c2, cpuIdle_c3;
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveAll();
                        if (setOnBoot.isChecked()) {
                            prepareScript(setOnBootFile, true);
                        } else {
                            MyTools.removeFile(setOnBootFile);
                            MyTools.removeFile(new File(scriptsDir + File.separator + subActivity1.setOnBootFileName));
                        }
                    }
                }).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
            String s = MyTools.readFile(Library.AVAIL_FREQ_PATH);
            AVAIL_FREQ = s.split(" ");
        } catch (Exception e) {
            AVAIL_FREQ = new String[]{"n/a", "n/a"};
            MyTools.longToast(getActivity(), "AVAIL_FREQ: " + e.toString());
        }
        try {
            String s = MyTools.readFile(Library.AVAIL_GOV_PATH);
            AVAIL_GOV = s.split(" ");
        } catch (Exception e) {
            AVAIL_GOV = new String[]{"n/a", "n/a"};
            MyTools.longToast(getActivity(), "AVAIL_GOV: " + e.toString());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cpu_control, container, false);
        setHasOptionsMenu(true);
        init_Voltages();
        initialize();
        refreshAll();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //initialize();
        //refreshAll();
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
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Library.stericson_busybox)));
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

        File scalingmaxfreq = new File(Library.MAX_FREQ0_PATH);
        if (!scalingmaxfreq.canRead() || !scalingmaxfreq.canWrite())
            MyTools.execTerminalCommand(new String[]{"chmod 666 " + scalingmaxfreq.toString()});

        susfLockState = (TextView) view.findViewById(R.id.susfLP);

        setOnBoot = (CheckBox) view.findViewById(R.id.setOnBoot);
        delay = (TextView) view.findViewById(R.id.text_delay);
        dataDir = MyTools.getDataDir(getActivity());
        scriptsDir = new File(dataDir + File.separator + "scripts");
        setOnBootFile = new File(scriptsDir + File.separator + setOnBootFileName);
        setOnBootAgent = new File(Library.setOnBootAgentFile);

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
        screenOffMaxDisplay = (TextView) view.findViewById(R.id.screenOffMaxDisplay);
        minCpusButton = (Button) view.findViewById(R.id.minCpusButton);
        maxCpusButton = (Button) view.findViewById(R.id.maxCpusButton);
        boostedCpusButton = (Button) view.findViewById(R.id.boostedCpusButton);
        suspendFreqButton = (Button) view.findViewById(R.id.suspendFreqButton);
        screenOffMaxButton = (Button) view.findViewById(R.id.screenOffMaxButton);

        // CPU IDLE
        cpuIdle_c0 = (Switch) view.findViewById(R.id.cpuIdle_c0);
        cpuIdle_c1 = (Switch) view.findViewById(R.id.cpuIdle_c1);
        cpuIdle_c2 = (Switch) view.findViewById(R.id.cpuIdle_c2);
        cpuIdle_c3 = (Switch) view.findViewById(R.id.cpuIdle_c3);

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
        screenOffMaxButton.setOnClickListener(screenOffMaxButtonListener);
        globalOffsetBtn.setOnClickListener(globalOffsetBtnListener);
        cpuGovernorChangeButton.setOnClickListener(cpuGovernorButtonListener);
        MAX_FREQ.setOnSeekBarChangeListener(maxfreqListenner);
        MIN_FREQ.setOnSeekBarChangeListener(minfreqListener);

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

        String[] govArray = {
                Library.GOV0,
                Library.GOV1,
                Library.GOV2,
                Library.GOV3
        };
        for (String GOV : govArray)
            MyTools.write(gov, GOV);

        String[] freqArray1 = {
                Library.MAX_FREQ0_PATH,
                Library.MAX_FREQ1_PATH,
                Library.MAX_FREQ2_PATH,
                Library.MAX_FREQ3_PATH
        };
        for (String freq : freqArray1)
            MyTools.write(mxc, freq);

        MyTools.write(mxc, "/sys/devices/system/cpu/cpufreq/" + gov + "/lmf_active_max_freq");

        String[] freqArray2 = {
                Library.MIN_FREQ0_PATH,
                Library.MIN_FREQ1_PATH,
                Library.MIN_FREQ2_PATH,
                Library.MIN_FREQ3_PATH
        };
        for (String freq : freqArray2)
            MyTools.write(mnc, freq);

        String cmnc = minCpusDisplay.getText().toString();
        String cmxc = maxCpusDisplay.getText().toString();
        String cbsc = boostedCpusDisplay.getContentDescription().toString();
        MyTools.write(cmnc, minCpusDisplay.getContentDescription().toString());
        MyTools.write(cmxc, maxCpusDisplay.getContentDescription().toString());
        MyTools.write(cbsc, boostedCpusButton.getContentDescription().toString());

        if (view.findViewById(R.id.switch_touchBoost).isEnabled()) {
            String boost_enabled = MyTools.parseIntFromBoolean(((Switch) view.findViewById(R.id.switch_touchBoost)).isChecked());
            MyTools.write(boost_enabled, view.findViewById(R.id.switch_touchBoost).getContentDescription().toString());
        }

        if (susfUnlocked) {
            String susf;
            try {
                susf = suspendFreqDisplay.getText().toString();
            } catch (Exception e) {
                MyTools.longToast(getActivity(), e.toString());
                susf = "n/a";
            }
            susf = scaleUp(susf);
            MyTools.write(susf, Library.SUSPEND_FREQ_PATH);
        }

        if (switch_scroff_single_core != null) {
            Log.d("TAG", ">>" + switch_scroff_single_core.isChecked());
            MyTools.write(MyTools.parseIntFromBoolean(switch_scroff_single_core.isChecked()), Library.SCREEN_OFF_SINGLE_CORE_PATH);
        }

        Switch[] s = {cpuIdle_c0, cpuIdle_c1, cpuIdle_c2, cpuIdle_c3};
        String[] t = {
                Library.CPU_IDLE_C0_PATH,
                Library.CPU_IDLE_C1_PATH,
                Library.CPU_IDLE_C2_PATH,
                Library.CPU_IDLE_C3_PATH
        };

        for (int i = 0; i < s.length; i++) {

            String tmp = "0";

            if (s[i].isChecked())
                tmp = "1";

            for (int j = 0; j <= 3; j++)
                MyTools.write(tmp, Library.CPU_IDLE_TRUNK_PATH + "cpu" + j + t[i]);
        }

        for (String tag : VoltagesAdapter.getPrints()) {
            if (tag.contains(":")) {
                String freq = tag.split(":")[0];
                String vol = tag.split(":")[1];
                MyTools.write(String.format("%s %s", freq, vol), Library.VDD_LEVELS);
            }

        }

        MyTools.write(MyTools.parseIntFromBoolean(((Switch) view.findViewById(R.id.screenOffMaxSwitch)).isChecked()), Library.SCREEN_OFF_MAX_STATE);
        MyTools.write(scaleUp(screenOffMaxDisplay.getText().toString()), Library.SCREEN_OFF_MAX_FREQ);

        SharedPreferences preferences = getActivity().getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        final char c = '|';
        String customProfileData = cpuGovDisplay.getText().toString() + c + AVAIL_FREQ[MAX_FREQ.getProgress()] + c + AVAIL_FREQ[MIN_FREQ.getProgress()] + c + maxCpusDisplay.getText().toString() + c + minCpusDisplay.getText().toString() + c + boostedCpusDisplay.getText().toString() + c + MyTools.readFile("/sys/devices/system/cpu/cpufreq/" + cpuGovDisplay.getText().toString().trim() + "/boostfreq", "null");
        editor.putString("CustomProfileData", customProfileData).apply();

        MainActivity.instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyTools.toast(getActivity(), R.string.toast_done_succ);
            }
        });
    }

    void refreshAll() {

        try {
            cpuGovDisplay.setText(MyTools.readFile(Library.GOV0));
        } catch (Exception e) {
            cpuGovDisplay.setText("n/a");
        }
        String CURRENT_MAX_FREQ;
        try {
            //MyTools.execTerminalCommand(new String[]{"chmod 666 " + Library.MAX_FREQ0_PATH});
            CURRENT_MAX_FREQ = MyTools.readFile(Library.MAX_FREQ0_PATH.trim());
        } catch (Exception e) {
            CURRENT_MAX_FREQ = "n/a";
        }
        String CURRENT_MIN_FREQ;
        try {
            CURRENT_MIN_FREQ = MyTools.readFile(Library.MIN_FREQ0_PATH.trim());
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
            minCpusDisplay.setText(MyTools.readFile(Library.MIN_CPUS_ONLINE_PATH0));
            minCpusDisplay.setContentDescription(Library.MIN_CPUS_ONLINE_PATH0);
        } catch (Exception e) {
            try {
                minCpusDisplay.setText(MyTools.readFile(Library.MIN_CPUS_ONLINE_PATH1));
                minCpusDisplay.setContentDescription(Library.MIN_CPUS_ONLINE_PATH1);
                ((TextView) view.findViewById(R.id.textView4)).setText(Library.sectionTitle_mpdecision);
            } catch (Exception ignored) {
                e.printStackTrace();
                minCpusDisplay.setText("n/a");
            }
        }
        try {
            maxCpusDisplay.setText(MyTools.readFile(Library.MAX_CPUS_ONLINE_PATH0));
            maxCpusDisplay.setContentDescription(Library.MAX_CPUS_ONLINE_PATH0);
        } catch (Exception e) {
            try {
                maxCpusDisplay.setText(MyTools.readFile(Library.MAX_CPUS_ONLINE_PATH1));
                maxCpusDisplay.setContentDescription(Library.MAX_CPUS_ONLINE_PATH1);
            } catch (IOException e1) {
                maxCpusDisplay.setText("n/a");
                maxCpusDisplay.setContentDescription("null");
            }
        }
        try {
            String str = MyTools.readFile(Library.BOOSTED_CPUS_PATH);
            boostedCpusDisplay.setText(str);
            boostedCpusDisplay.setContentDescription(String.format("%s %s %s %s", str, str, str, str));
            boostedCpusButton.setContentDescription(Library.BOOSTED_CPUS_PATH);
        } catch (Exception e) {
            try {
                String boost_enabled = MyTools.readFile(Library.TOUCH_BOOST_PATH).trim();
                ((Switch) view.findViewById(R.id.switch_touchBoost)).setChecked(boost_enabled.equals("1"));
                String str = MyTools.readFile(Library.TOUCH_BOOST_FREQS_PATH);
                boostedCpusDisplay.setText(scaleDown(str));
                boostedCpusDisplay.setContentDescription(String.format("%s %s %s %s", str, str, str, str));
                //switch to mode 2
                boostedCpusButton.setContentDescription(Library.TOUCH_BOOST_FREQS_PATH);
                view.findViewById(R.id.switch_touchBoost).setEnabled(true);
                view.findViewById(R.id.switch_touchBoost).setVisibility(View.VISIBLE);
                view.findViewById(R.id.switch_touchBoost).setContentDescription(Library.TOUCH_BOOST_PATH);
                //TODO
                ((TextView) view.findViewById(R.id.textView10)).setText("Touch Boost");
                view.findViewById(R.id.unit0).setVisibility(View.VISIBLE);
                boostedCpusButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //TODO
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog
                                .setTitle("Touch Boost")
                                .setItems(AVAIL_FREQ, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String str = AVAIL_FREQ[i];
                                        boostedCpusDisplay.setText(scaleDown(str));
                                        boostedCpusDisplay.setContentDescription(String.format("%s %s %s %s", str, str, str, str));
                                    }
                                })
                                .show();
                    }
                });
            } catch (Exception ignored) {
                boostedCpusDisplay.setText("n/a");
                boostedCpusDisplay.setContentDescription("null");
            }

        }
        try {
            suspendFreqDisplay.setText(scaleDown(MyTools.readFile(Library.SUSPEND_FREQ_PATH)));
            suspendFreqDisplay.setContentDescription(Library.SUSPEND_FREQ_PATH);
        } catch (Exception e) {
            try {
                String tmp = MyTools.readFile(Library.SCREEN_OFF_SINGLE_CORE_PATH).trim();
                suspendFreqDisplay.setContentDescription(Library.SCREEN_OFF_SINGLE_CORE_PATH);
                suspendFreqButton.setVisibility(View.INVISIBLE);
                suspendFreqDisplay.setVisibility(View.INVISIBLE);
                view.findViewById(R.id.susfLP).setVisibility(View.GONE);
                view.findViewById(R.id.unit1).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.textView7)).setText("Screen_Off Single Core");
                if (switch_scroff_single_core == null) {
                    switch_scroff_single_core = new Switch(getActivity());
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_START, R.id.switch_touchBoost);
                    params.addRule(RelativeLayout.ALIGN_END, R.id.switch_touchBoost);
                    params.addRule(RelativeLayout.ALIGN_TOP, R.id.separator5);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.separator6);
                    switch_scroff_single_core.setLayoutParams(params);
                    ((RelativeLayout) view.findViewById(R.id.master)).addView(switch_scroff_single_core);
                }
                switch_scroff_single_core.setChecked(tmp.equals("1"));

            } catch (Exception ee) {
                suspendFreqDisplay.setText("n/a");
            }
        }

        try {
            String s = MyTools.readFile(Library.SCREEN_OFF_MAX_FREQ);
            screenOffMaxDisplay.setText(scaleDown(s));
            s = MyTools.readFile(Library.SCREEN_OFF_MAX_STATE);
            ((Switch) view.findViewById(R.id.screenOffMaxSwitch))
                    .setChecked(MyTools.parseBoolFromInteger(Integer.parseInt(s.trim())));
        } catch (Exception e) {
            view.findViewById(R.id.screenOffMaxDisplay).setVisibility(View.GONE);
            view.findViewById(R.id.screenOffMaxButton).setVisibility(View.GONE);
            view.findViewById(R.id.screenOffMaxSwitch).setVisibility(View.GONE);
            view.findViewById(R.id.textView14).setVisibility(View.GONE);
            view.findViewById(R.id.separator6).setVisibility(View.GONE);
            view.findViewById(R.id.scoffAlias).setVisibility(View.GONE);
        }

        Switch[] s = {cpuIdle_c0, cpuIdle_c1, cpuIdle_c2, cpuIdle_c3};
        String[] t = {
                Library.CPU_IDLE_C0_PATH,
                Library.CPU_IDLE_C1_PATH,
                Library.CPU_IDLE_C2_PATH,
                Library.CPU_IDLE_C3_PATH
        };

        for (int i = 0; i < s.length; i++) {
            String ss;
            try {
                ss = MyTools.readFile(Library.CPU_IDLE_TRUNK_PATH + "cpu0" + t[i]);
            } catch (Exception e) {
                ss = "n/a";
            }
            if (ss.equals("1")) {
                s[i].setChecked(true);
            } else {
                s[i].setChecked(false);
            }
        }

        init_Voltages();

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

    private void init_Voltages() {
        vdd_list = MyTools.catToList(Library.VDD_LEVELS);

        if (!vdd_list.get(0).equals("ladyGaga")) {

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

            VoltagesAdapter adapter = new VoltagesAdapter(getActivity(), R.layout.voltage_view, vols, freqs, voltageSteps, voltageStepValue);
            int count = adapter.getCount();
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.voltage_vault);
            linearLayout.removeAllViews();
            for (int i = 0; i < count; i++) {
                View custom = adapter.getView(i, null, null);
                linearLayout.addView(custom);
            }

        }
    }

    private void prepareScript(File file, boolean f) {

        try {

            if (!scriptsDir.exists())
                scriptsDir.mkdirs();


            String tmpSusf = switch_scroff_single_core == null ? scaleUp(suspendFreqDisplay.getText().toString()) : MyTools.parseIntFromBoolean(switch_scroff_single_core.isChecked());
            if (!susfUnlocked)
                tmpSusf = null;

            String tmpSoffMax1 = null;
            String tmpSoffMax2 = null;

            if (screenOffMaxDisplay.getVisibility() == View.VISIBLE) {
                tmpSoffMax1 = MyTools.parseIntFromBoolean(((Switch) view.findViewById(R.id.screenOffMaxSwitch)).isChecked());
                tmpSoffMax2 = scaleUp(screenOffMaxDisplay.getText().toString());
            }

            String[] values = new String[]{
                    cpuGovDisplay.getText().toString(),
                    cpuGovDisplay.getText().toString(),
                    cpuGovDisplay.getText().toString(),
                    cpuGovDisplay.getText().toString(),

                    AVAIL_FREQ[MAX_FREQ.getProgress()],
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
                    MyTools.parseIntFromBoolean(((Switch) view.findViewById(R.id.switch_touchBoost)).isChecked()),
                    boostedCpusDisplay.getContentDescription().toString(),
                    tmpSusf,
                    tmpSoffMax1,
                    tmpSoffMax2,

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
                    Library.GOV0,
                    Library.GOV1,
                    Library.GOV2,
                    Library.GOV3,

                    Library.MAX_FREQ0_PATH,
                    Library.MAX_FREQ1_PATH,
                    Library.MAX_FREQ2_PATH,
                    Library.MAX_FREQ3_PATH,
                    "/sys/devices/system/cpu/cpufreq/" + cpuGovDisplay.getText().toString().trim() + "/lmf_active_max_freq",

                    Library.MIN_FREQ0_PATH,
                    Library.MIN_FREQ1_PATH,
                    Library.MIN_FREQ2_PATH,
                    Library.MIN_FREQ3_PATH,

                    minCpusDisplay.getContentDescription().toString(),
                    maxCpusDisplay.getContentDescription().toString(),
                    view.findViewById(R.id.switch_touchBoost).getContentDescription().toString(),
                    boostedCpusButton.getContentDescription().toString(),
                    suspendFreqDisplay.getContentDescription().toString(),
                    Library.SCREEN_OFF_MAX_STATE,
                    Library.SCREEN_OFF_MAX_FREQ,

                    Library.CPU_IDLE_TRUNK_PATH + "cpu0" +
                            Library.CPU_IDLE_C0_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu1" +
                            Library.CPU_IDLE_C0_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu2" +
                            Library.CPU_IDLE_C0_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu3" +
                            Library.CPU_IDLE_C0_PATH,

                    Library.CPU_IDLE_TRUNK_PATH + "cpu0" +
                            Library.CPU_IDLE_C1_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu1" +
                            Library.CPU_IDLE_C1_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu2" +
                            Library.CPU_IDLE_C1_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu3" +
                            Library.CPU_IDLE_C1_PATH,

                    Library.CPU_IDLE_TRUNK_PATH + "cpu0" +
                            Library.CPU_IDLE_C2_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu1" +
                            Library.CPU_IDLE_C2_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu2" +
                            Library.CPU_IDLE_C2_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu3" +
                            Library.CPU_IDLE_C2_PATH,

                    Library.CPU_IDLE_TRUNK_PATH + "cpu0" +
                            Library.CPU_IDLE_C3_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu1" +
                            Library.CPU_IDLE_C3_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu2" +
                            Library.CPU_IDLE_C3_PATH,
                    Library.CPU_IDLE_TRUNK_PATH + "cpu3" +
                            Library.CPU_IDLE_C3_PATH
            };

            String flags = ":delay45:";

            if (!f)
                flags = "null";


            MyTools.fillScript(file, values, destinations, flags);

            ArrayList<String> vals = new ArrayList<String>();

            for (String print : VoltagesAdapter.getPrints())
                if (print.contains(":")) {
                    vals.add(print.split(":")[0] + " " + print.split(":")[1]);
                }

            MyTools.completeScriptWith(file, vals, new String[]{Library.VDD_LEVELS});

            File gov = new File("/sys/devices/system/cpu/cpufreq/" + MyTools.readFile(Library.GOV0));
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