package com.themike10452.hellscorekernelmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import eu.chainfire.libsuperuser.Shell;


public class MiscFragment extends Fragment {

    public static final String setOnBootFileName = "95hellscore_misc_settings";
    private static ViewGroup.LayoutParams XlayoutParams;
    private View view;
    private Spinner spinner1, spinner2;
    private EditText readAhead_field, thrott_field, vibrator_field;
    private Switch dynFsyncSwitch, fastChargeSwitch;
    private CheckBox setOnBoot;
    private String[] AVAIL_SCHED;
    private String AVAILABLE_TCP_SCHEDS;
    private File setOnBootAgent, setOnBootFile, scriptsDir;

    public MiscFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_misc, container, false);
        setHasOptionsMenu(true);

        String dataDir = MyTools.getDataDir(getActivity());
        scriptsDir = new File(dataDir + File.separator + "scripts");

        setOnBootFile = new File(scriptsDir + File.separator
                + setOnBootFileName);
        setOnBootAgent = new File(Library.setOnBootAgentFile);

        setOnBoot = (CheckBox) view.findViewById(R.id.setOnBoot);
        spinner1 = (Spinner) view.findViewById(R.id.spinner1);
        readAhead_field = (EditText) view.findViewById(R.id.editText1);
        thrott_field = (EditText) view.findViewById(R.id.editText3);
        dynFsyncSwitch = (Switch) view.findViewById(R.id.switch1);
        fastChargeSwitch = (Switch) view.findViewById(R.id.switch2);

        spinner2 = (Spinner) view.findViewById(R.id.spinner0);

        StringBuffer tmp;
        try {
            tmp = new StringBuffer(MyTools.readFile(Library.IO_SCHED_PATH));
        } catch (Exception e) {
            tmp = new StringBuffer("n/a");
        }
        try {
            String s = new String(tmp.deleteCharAt(tmp.indexOf("[")).deleteCharAt(tmp.indexOf("]")));

            AVAIL_SCHED = s.split(" ");
        } catch (Exception e1) {
            AVAIL_SCHED = new String[]{"null"};
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, AVAIL_SCHED);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);

        vibrator_field = (EditText) view.findViewById(R.id.editText2);

        refreshAll();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                if (XlayoutParams != null)
                    view.findViewById(R.id.editText4).setLayoutParams(XlayoutParams);
                saveAll();
                if (setOnBoot.isChecked())
                    prepareBootScript();
                else
                    removeBootFile();
                return true;
        }
        return false;
    }

    private void refreshAll() {
        view.clearFocus();

        if (XlayoutParams != null)
            view.findViewById(R.id.editText4).setLayoutParams(XlayoutParams);

        String tmp0;
        try {
            tmp0 = MyTools.readFile(Library.IO_SCHED_PATH);
        } catch (Exception e) {
            tmp0 = "n/a";
        }
        StringBuffer tmp = new StringBuffer(tmp0).delete(0, tmp0.indexOf('[') + 1);
        tmp = tmp.delete(tmp0.indexOf(']') - tmp0.indexOf('[') - 1, tmp0.length());
        tmp0 = new String(tmp);
        ArrayList<String> sched_list = new ArrayList<String>();
        Collections.addAll(sched_list, AVAIL_SCHED);
        try {
            spinner1.setSelection(sched_list.indexOf(tmp0));
        } catch (Exception e) {
            MyTools.longToast(getActivity(), "current_io_sched: " + e.toString());
            spinner1.setVisibility(View.GONE);
        }
        try {
            readAhead_field.setText(MyTools.readFile(Library.READ_AHEAD_BUFFER_PATH));
        } catch (Exception e) {
            MyTools.longToast(getActivity(), "readAhead: " + e.toString());
            readAhead_field.setText("n/a");
        }

        try {
            thrott_field.setText(MyTools.readFile(Library.MSM_THERMAL_PATH));
        } catch (Exception e) {
            MyTools.longToast(getActivity(), "msm_thermal: " + e.toString());
            thrott_field.setText("n/a");
        }

        try {
            if (MyTools.readFile(Library.DYN_FSYNC_PATH).equals("1")) {
                dynFsyncSwitch.setChecked(true);
            } else {
                dynFsyncSwitch.setChecked(false);
            }
        } catch (Exception e) {
            try {
                dynFsyncSwitch.setVisibility(View.GONE);
                view.findViewById(R.id.textView3).setVisibility(View.GONE);
                view.findViewById(R.id.separator5).setVisibility(View.GONE);
                dynFsyncSwitch = null;
                //MyTools.longToast(getActivity(), "FSync: " + e.toString());
            } catch (Exception ignored) {
            }
        }

        try {
            if (MyTools.readFile(Library.FASTCHARGE_PATH).equals("1")) {
                fastChargeSwitch.setChecked(true);
            } else {
                fastChargeSwitch.setChecked(false);
            }
        } catch (Exception e) {
            MyTools.longToast(getActivity(), "FastCharge: " + e.toString());
            fastChargeSwitch.setChecked(false);
        }

        try {
            vibrator_field.setText(MyTools.readFile(Library.VIBRATOR_AMP));
        } catch (Exception e) {
            vibrator_field.setText("n/a");
            MyTools.longToast(getActivity(), "Vib_Strength: " + e.toString());
        }

        try {
            AVAILABLE_TCP_SCHEDS = MyTools.readFile(Library.NET_TCP_AVAILABLE);
            String sx = MyTools.readFile(Library.NET_TCP_CONGST);
            String[] tab = AVAILABLE_TCP_SCHEDS.trim().split(" ");
            spinner2.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, tab));
            spinner2.setSelection(indexOf(tab, sx));
        } catch (Exception ignored) {
        } catch (Error ignored) {
        }

        try {
            String s = Shell.SH.run("getprop net.hostname").get(0).trim();
            ((EditText) view.findViewById(R.id.editText4)).setText(s);

            view.findViewById(R.id.editText4).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {

                    if (!b && XlayoutParams != null) {
                        view.findViewById(R.id.editText4).setLayoutParams(XlayoutParams);
                        return;
                    }

                    if (XlayoutParams == null)
                        XlayoutParams = view.getLayoutParams();

                    int i = getResources().getDisplayMetrics().widthPixels;
                    i /= 2;
                    i -= 15;
                    if (b) {
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(i, -1);
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                        view.setLayoutParams(params);
                    }


                }
            });
        } catch (Exception ignored) {
        }

        setOnBoot.setChecked(
                setOnBootFile.exists() && setOnBootAgent.exists() &&
                        !setOnBootFile.isDirectory() && !setOnBootAgent.isDirectory()
        );
    }

    private void saveAll() {
        try {
            if (Integer.parseInt(readAhead_field.getText().toString()) < 128)
                readAhead_field.setText("128");
            else if (Integer.parseInt(readAhead_field.getText().toString()) > 4096)
                readAhead_field.setText("4096");


            EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
            easyTracker.send(MapBuilder
                            .createEvent("IO_Sched",
                                    "IOSched_selected",
                                    AVAIL_SCHED[spinner1.getSelectedItemPosition()],
                                    null)
                            .build()
            );
            MyTools.write(AVAIL_SCHED[spinner1.getSelectedItemPosition()], Library.IO_SCHED_PATH);
            MyTools.write(readAhead_field.getText().toString(), Library.READ_AHEAD_BUFFER_PATH);

            if (dynFsyncSwitch != null)
                MyTools.write(MyTools.parseIntFromBoolean(dynFsyncSwitch.isChecked()), Library.DYN_FSYNC_PATH);
            MyTools.write(MyTools.parseIntFromBoolean(fastChargeSwitch.isChecked()), Library.FASTCHARGE_PATH);

            MyTools.write(thrott_field.getText().toString(), Library.MSM_THERMAL_PATH);

            if (Integer.parseInt(vibrator_field.getText().toString()) > 100)
                vibrator_field.setText("100");

            MyTools.write(vibrator_field.getText().toString(), Library.VIBRATOR_AMP);

            MyTools.execTerminalCommand(new String[]
                            {
                                    String.format("echo %s > %s", AVAILABLE_TCP_SCHEDS, Library.NET_TCP_ALLOWED),
                                    String.format("echo %s > %s", ((Spinner) view.findViewById(R.id.spinner0)).getSelectedItem().toString(), Library.NET_TCP_CONGST),
                                    "setprop net.hostname " + ((EditText) view.findViewById(R.id.editText4)).getText().toString(),
                                    "setprop net.ipv4.tcp_congestion_control " + ((Spinner) view.findViewById(R.id.spinner0)).getSelectedItem().toString()
                            }
            );

            MyTools.toast(getActivity(), R.string.toast_done_succ);
        } catch (Exception ignored) {
            MyTools.longToast(getActivity(), R.string.toast_failed);
        }
    }

    private void prepareBootScript() {
        try {
            if (!scriptsDir.exists())
                scriptsDir.mkdirs();

            if (setOnBootFile.exists() && !setOnBootFile.isDirectory())
                setOnBootFile.delete();

            setOnBootFile.createNewFile();

            String[] values = {
                    AVAIL_SCHED[spinner1.getSelectedItemPosition()],
                    readAhead_field.getText().toString(),
                    thrott_field.getText().toString(),
                    dynFsyncSwitch != null ? MyTools.parseIntFromBoolean(dynFsyncSwitch.isChecked()) : null,
                    MyTools.parseIntFromBoolean(fastChargeSwitch.isChecked()),
                    vibrator_field.getText().toString()
            };

            String[] destinations = {
                    Library.IO_SCHED_PATH,
                    Library.READ_AHEAD_BUFFER_PATH,
                    Library.MSM_THERMAL_PATH,
                    Library.DYN_FSYNC_PATH,
                    Library.FASTCHARGE_PATH,
                    Library.VIBRATOR_AMP
            };


            MyTools.fillScript(setOnBootFile, values, destinations, "");
            MyTools.completeScriptWith(setOnBootFile, new String[]
                    {
                            String.format("echo %s > %s", AVAILABLE_TCP_SCHEDS, Library.NET_TCP_ALLOWED),
                            String.format("echo %s > %s", ((Spinner) view.findViewById(R.id.spinner0)).getSelectedItem().toString(), Library.NET_TCP_CONGST),
                            "setprop net.hostname " + ((EditText) view.findViewById(R.id.editText4)).getText().toString(),
                            "setprop net.ipv4.tcp_congestion_control " + ((Spinner) view.findViewById(R.id.spinner0)).getSelectedItem().toString()

                    });
            MyTools.createBootAgent(getActivity(), scriptsDir);
        } catch (Exception e) {
            File innerLog = new File(MyTools.getDataDir(getActivity()) + File.separator + "inner_log.log");
            MyTools.log(getActivity(), e.toString(), innerLog.toString());
            setOnBootFailed();
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
        removeBootFile();
        b.show();
    }

    private void removeBootFile() {
        MyTools.removeFile(setOnBootFile);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public int indexOf(String[] strings, String string) {
        if (string != null && strings != null) {
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].equalsIgnoreCase(string)) {
                    return i;
                }
            }
        }
        return -1;
    }

}