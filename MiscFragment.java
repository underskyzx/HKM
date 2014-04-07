package com.themike10452.hellscorekernelmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class MiscFragment extends Fragment {

    public static final String setOnBootFileName = "95hellscore_misc_settings";
    private View view;
    private Spinner spinner1;
    private EditText readAhead_field, thrott_field, vibrator_field;
    private Switch dynFsyncSwitch, fastChargeSwitch;
    private CheckBox setOnBoot;
    private String[] AVAIL_SCHED;
    private File setOnBootAgent, setOnBootFile, scriptsDir;

    public MiscFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_misc, container, false);
        setHasOptionsMenu(true);

        String dataDir = MyTools.getDataDir(getActivity());
        scriptsDir = new File(dataDir + File.separator + "scripts");

        setOnBootFile = new File(scriptsDir + File.separator
                + setOnBootFileName);
        setOnBootAgent = new File(this.getString(R.string.setOnBootAgentFile));

        setOnBoot = (CheckBox) view.findViewById(R.id.setOnBoot);
        spinner1 = (Spinner) view.findViewById(R.id.spinner1);
        readAhead_field = (EditText) view.findViewById(R.id.editText1);
        thrott_field = (EditText) view.findViewById(R.id.editText3);
        dynFsyncSwitch = (Switch) view.findViewById(R.id.switch1);
        fastChargeSwitch = (Switch) view.findViewById(R.id.switch2);

        StringBuffer tmp;
        try {
            tmp = new StringBuffer(MyTools.readFile(this.getString(R.string.IO_SCHED_PATH)));
        } catch (Exception e) {
            tmp = new StringBuffer("n/a");
        }

        String s = new String(tmp.deleteCharAt(tmp.indexOf("[")).deleteCharAt(tmp.indexOf("]")));

        AVAIL_SCHED = s.split(" ");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, AVAIL_SCHED);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);

        vibrator_field = (EditText) view.findViewById(R.id.editText2);

        refreshAll();

        return view;
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
                    prepareBootScript();
                else
                    removeBootFile();
                return true;
        }
        return false;
    }

    private void refreshAll() {
        view.clearFocus();
        String tmp0;
        try {
            tmp0 = MyTools.readFile(this.getString(R.string.IO_SCHED_PATH));
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
            readAhead_field.setText(MyTools.readFile(this.getString(R.string.READ_AHEAD_BUFFER_PATH)));
        } catch (Exception e) {
            MyTools.longToast(getActivity(), "readAhead: " + e.toString());
            readAhead_field.setText("n/a");
        }

        try {
            thrott_field.setText(MyTools.readFile(getString(R.string.MSM_THERMAL_PATH)));
        } catch (Exception e) {
            MyTools.longToast(getActivity(), "msm_thermal: " + e.toString());
            thrott_field.setText("n/a");
        }

        try {
            if (MyTools.readFile(this.getString(R.string.DYN_FSYNC_PATH)).equals("1")) {
                dynFsyncSwitch.setChecked(true);
            } else {
                dynFsyncSwitch.setChecked(false);
            }
        } catch (Exception e) {
            dynFsyncSwitch.setChecked(false);
            MyTools.longToast(getActivity(), "FSync: " + e.toString());
        }

        try {
            if (MyTools.readFile(this.getString(R.string.FASTCHARGE_PATH)).equals("1")) {
                fastChargeSwitch.setChecked(true);
            } else {
                fastChargeSwitch.setChecked(false);
            }
        } catch (Exception e) {
            MyTools.longToast(getActivity(), "FastCharge: " + e.toString());
            fastChargeSwitch.setChecked(false);
        }

        try {
            vibrator_field.setText(MyTools.readFile(this.getString(R.string.VIBRATOR_AMP)));
        } catch (Exception e) {
            vibrator_field.setText("n/a");
            MyTools.longToast(getActivity(), "Vib_Strength: " + e.toString());
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
            MyTools.write(AVAIL_SCHED[spinner1.getSelectedItemPosition()], this.getString(R.string.IO_SCHED_PATH));
            MyTools.write(readAhead_field.getText().toString(), this.getString(R.string.READ_AHEAD_BUFFER_PATH));

            MyTools.write(MyTools.parseIntFromBoolean(dynFsyncSwitch.isChecked()), this.getString(R.string.DYN_FSYNC_PATH));
            MyTools.write(MyTools.parseIntFromBoolean(fastChargeSwitch.isChecked()), this.getString(R.string.FASTCHARGE_PATH));

            MyTools.write(thrott_field.getText().toString(), getString(R.string.MSM_THERMAL_PATH));

            if (Integer.parseInt(vibrator_field.getText().toString()) > 100)
                vibrator_field.setText("100");

            MyTools.write(vibrator_field.getText().toString(), this.getString(R.string.VIBRATOR_AMP));
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
                    MyTools.parseIntFromBoolean(dynFsyncSwitch.isChecked()),
                    MyTools.parseIntFromBoolean(fastChargeSwitch.isChecked()),
                    vibrator_field.getText().toString()
            };

            String[] destinations = {
                    this.getString(R.string.IO_SCHED_PATH),
                    this.getString(R.string.READ_AHEAD_BUFFER_PATH),
                    getString(R.string.MSM_THERMAL_PATH),
                    this.getString(R.string.DYN_FSYNC_PATH),
                    this.getString(R.string.FASTCHARGE_PATH),
                    this.getString(R.string.VIBRATOR_AMP)
            };


            MyTools.fillScript(setOnBootFile, values, destinations, "");
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

}