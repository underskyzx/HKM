package com.themike10452.hellscorekernelmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.io.File;
import java.util.ArrayList;

public class TouchControlFragment extends Fragment {

    public static final String setOnBootFileName = "90hellscore_touch_settings";
    private View view;
    private File setOnBootFile;
    private File setOnBootAgent;
    private File scriptsDir;
    private Switch dt2wSwitch, s2wSwitch, twSwitch;
    private TextView delay;
    private CheckBox setOnBoot;
    private String DT2W_PATH;
    private boolean enabled = true;

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
                if (!enabled)
                    return false;
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
        view = inflater.inflate(R.layout.fragment_touch_control, container, false);
        setHasOptionsMenu(true);

        dt2wSwitch = (Switch) view.findViewById(R.id.dt2wSwitch);
        s2wSwitch = (Switch) view.findViewById(R.id.s2wSwitch);
        twSwitch = (Switch) view.findViewById(R.id.twSwitch);

        delay = (TextView) view.findViewById(R.id.delay);
        setOnBoot = (CheckBox) view.findViewById(R.id.setOnBoot);

        String dataDir = MyTools.getDataDir(getActivity());
        scriptsDir = new File(dataDir + File.separator + "scripts");
        setOnBootFile = new File(scriptsDir + File.separator + setOnBootFileName);
        setOnBootAgent = new File(Library.setOnBootAgentFile);

        DT2W_PATH = new File(Library.DT2W_PATH).exists() ? Library.DT2W_PATH : Library.DT2W_PATH_S;

        twSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                view.findViewById(R.id.btn_twDelay).setEnabled(isChecked);
            }
        });

        view.findViewById(R.id.btn_twDelay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(getActivity());
                d.setContentView(R.layout.editbox);
                d.setTitle(R.string.alias_button_touchWakeDelay);
                d.setCancelable(true);
                d.show();
                final EditText editText = (EditText) d.findViewById(R.id.editText);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(delay.getText());
                final Button button = (Button) d.findViewById(R.id.button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editText.getText().length() > 0)
                            delay.setText(editText.getText().toString().trim());
                        d.dismiss();
                    }
                });

            }
        });

        refreshAll();

        return view;
    }

    private void saveAll() {
        MyTools.write(MyTools.parseIntFromBoolean(dt2wSwitch.isChecked()), DT2W_PATH);
        MyTools.write(MyTools.parseIntFromBoolean(s2wSwitch.isChecked()), Library.S2W_PATH);
        MyTools.write(MyTools.parseIntFromBoolean(twSwitch.isChecked()), Library.TOUCHWAKE_S);
        MyTools.write(delay.getText().toString(), Library.TOUCHWAKE_DELAY);
        showOutMsg(0);
    }

    private void showOutMsg(int i) {
        if (i == 0) {
            showOutMsg(-1);
            Toast.makeText(getActivity(), R.string.toast_done_succ, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshAll() {
        int tmpValue = MyTools.catInt(DT2W_PATH, -1);
        if (tmpValue == -1)
            view.findViewById(R.id.dt2w).setVisibility(View.GONE);
        else
            dt2wSwitch.setChecked(parseBooleanFromInt(tmpValue));

        tmpValue = MyTools.catInt(Library.S2W_PATH, -1);
        if (tmpValue == -1)
            view.findViewById(R.id.s2w).setVisibility(View.GONE);
        else
            s2wSwitch.setChecked(parseBooleanFromInt(tmpValue));

        tmpValue = MyTools.catInt(Library.TOUCHWAKE_S, -1);
        if (tmpValue == -1) {
            view.findViewById(R.id.tw).setVisibility(View.GONE);
        } else {
            twSwitch.setChecked(parseBooleanFromInt(tmpValue));
            try {
                delay.setText(MyTools.readFile(Library.TOUCHWAKE_DELAY));
            } catch (Exception e) {
                delay.setText("n/a");
            }
        }

        if (!isVisible(view.findViewById(R.id.s2w)) && !isVisible(view.findViewById(R.id.dt2w)) && !isVisible(view.findViewById(R.id.tw))) {
            ((TextView) view.findViewById(R.id.title1)).setText("Not available");
            enabled = false;
        }

        if (enabled)
            setOnBoot.setChecked(
                    setOnBootFile.exists() && setOnBootAgent.exists() &&
                            !setOnBootFile.isDirectory() && !setOnBootAgent.isDirectory()
            );
        else setOnBoot.setEnabled(false);
    }

    private void prepareBootScripts() {
        try {
            if (!scriptsDir.exists())
                scriptsDir.mkdirs();

            if (setOnBootFile.exists() && !setOnBootFile.isDirectory()) {
                setOnBootFile.delete();
            }


            setOnBootFile.createNewFile();


            String[] values = null;
            String[] destinations = null;

            ArrayList<String> vals = new ArrayList<>(), dests = new ArrayList<>();

            if (view.findViewById(R.id.dt2w).getVisibility() == View.VISIBLE) {
                vals.add(MyTools.parseIntFromBoolean(dt2wSwitch.isChecked()));
                dests.add(DT2W_PATH);
            }
            if (view.findViewById(R.id.s2w).getVisibility() == View.VISIBLE) {
                vals.add(MyTools.parseIntFromBoolean(s2wSwitch.isChecked()));
                dests.add(Library.S2W_PATH);
            }
            if (view.findViewById(R.id.tw).getVisibility() == View.VISIBLE) {
                vals.add(MyTools.parseIntFromBoolean(twSwitch.isChecked()));
                dests.add(Library.TOUCHWAKE_S);
                vals.add(delay.getText().toString());
                dests.add(Library.TOUCHWAKE_DELAY);
            }

            if (vals.size() > 0) {
                values = vals.toArray(new String[vals.size()]);
                destinations = dests.toArray(new String[dests.size()]);
            }

            if (enabled && values != null) {
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

    private boolean isVisible(View v) {
        return v.getVisibility() == View.VISIBLE;
    }

}
