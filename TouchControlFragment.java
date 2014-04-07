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
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;

public class TouchControlFragment extends Fragment {

    public static final String setOnBootFileName = "90hellscore_touch_settings";
    private File setOnBootFile;
    private File setOnBootAgent;
    private File scriptsDir;
    private Switch dt2wSwitch, s2wSwitch;
    private CheckBox setOnBoot;

    public TouchControlFragment() {

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
                MyTools.playSound(getActivity());
                saveAll();
                //create setOnBootFiles
                if (setOnBoot.isChecked())
                    prepareBootScripts();
                else
                    removeBootFile();
                return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_touch_control, container, false);
        setHasOptionsMenu(true);

        dt2wSwitch = (Switch) view.findViewById(R.id.dt2wSwitch);
        s2wSwitch = (Switch) view.findViewById(R.id.s2wSwitch);

        setOnBoot = (CheckBox) view.findViewById(R.id.setOnBoot);

        String dataDir = MyTools.getDataDir(getActivity());
        scriptsDir = new File(dataDir + File.separator + "scripts");
        setOnBootFile = new File(scriptsDir + File.separator + setOnBootFileName);
        setOnBootAgent = new File(this.getString(R.string.setOnBootAgentFile));

        refreshAll();

        return view;
    }

    private void saveAll() {
        MyTools.write(MyTools.parseIntFromBoolean(dt2wSwitch.isChecked()), this.getString(R.string.DT2W_PATH));
        MyTools.write(MyTools.parseIntFromBoolean(s2wSwitch.isChecked()), this.getString(R.string.S2W_PATH));
        showOutMsg(0);
    }

    private void showOutMsg(int i) {
        if (i == 0) {
            showOutMsg(-1);
            Toast.makeText(getActivity(), R.string.toast_done_succ, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshAll() {
        int tmpValue = MyTools.catInt(this.getString(R.string.DT2W_PATH), -1);
        dt2wSwitch.setChecked(parseBooleanFromInt(tmpValue));
        tmpValue = MyTools.catInt(this.getString(R.string.S2W_PATH), -1);
        s2wSwitch.setChecked(parseBooleanFromInt(tmpValue));
        setOnBoot.setChecked(
                setOnBootFile.exists() && setOnBootAgent.exists() &&
                        !setOnBootFile.isDirectory() && !setOnBootAgent.isDirectory()
        );
    }

    private void prepareBootScripts() {
        try {
            if (!scriptsDir.exists())
                scriptsDir.mkdirs();

            if (setOnBootFile.exists() && !setOnBootFile.isDirectory()) {
                setOnBootFile.delete();
            }


            setOnBootFile.createNewFile();


            String[] values;
            String[] destinations;

            if (MyTools.catInt(this.getString(R.string.DT2W_PATH), -1) != -1) {
                if (MyTools.catInt(this.getString(R.string.S2W_PATH), -1) != -1) {
                    values = new String[2];
                    destinations = new String[2];
                    values[0] = MyTools.readFile(this.getString(R.string.DT2W_PATH));
                    values[1] = MyTools.readFile(this.getString(R.string.S2W_PATH));
                    destinations[0] = this.getString(R.string.DT2W_PATH);
                    destinations[1] = this.getString(R.string.S2W_PATH);
                } else {
                    values = new String[1];
                    destinations = new String[1];
                    values[0] = MyTools.readFile(this.getString(R.string.DT2W_PATH));
                    destinations[0] = this.getString(R.string.DT2W_PATH);
                }
            } else {
                if (MyTools.catInt(this.getString(R.string.S2W_PATH), -1) != -1) {
                    values = new String[1];
                    destinations = new String[1];
                    values[0] = MyTools.readFile(this.getString(R.string.S2W_PATH));
                    destinations[0] = this.getString(R.string.S2W_PATH);
                } else {
                    values = null;
                    destinations = null;
                }
            }

            if (values != null) {
                MyTools.fillScript(setOnBootFile, values, destinations, "");
                MyTools.createBootAgent(getActivity(), scriptsDir);
            }
        } catch (Exception e) {
            File innerLog = new File(MyTools.getDataDir(getActivity()) + File.separator + "inner_log.log");
            MyTools.log(getActivity(), e.toString(), innerLog.toString());
            setOnBootFailed();
        }
    }

    private void removeBootFile() {
        MyTools.removeFile(setOnBootFile);
    }

    private void setOnBootFailed() {
        final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setMessage("Set on boot failed");
        b.setCancelable(false);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                refreshAll();
            }
        });
        setOnBoot.setChecked(false);
        removeBootFile();
        b.show();
    }

    private boolean parseBooleanFromInt(int i) {
        try {
            switch (i) {
                case 1:
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

}
