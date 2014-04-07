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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class GpuControlFragment extends Fragment {

    public static final String setOnBootFileName = "90hellscore_gpu_settings";
    private CheckBox setOnBoot;
    private File setOnBootFile;
    private File setOnBootAgent;
    private File scriptsDir;
    private Spinner spinner;
    private SeekBar seekBar;
    private TextView maxClkDisplay;
    private String[] governors;
    private String[] freq;
    private ArrayList<String> FREQ;

    public GpuControlFragment() {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gpu_control,
                container, false);
        setHasOptionsMenu(true);

        File dataDir = new File(MyTools.getDataDir(getActivity()));
        scriptsDir = new File(dataDir + File.separator + "scripts");
        setOnBootFile = new File(scriptsDir + File.separator + setOnBootFileName);
        setOnBootAgent = new File(this.getString(R.string.setOnBootAgentFile));

        setOnBoot = (CheckBox) rootView.findViewById(R.id.setOnBoot);
        spinner = (Spinner) rootView.findViewById(R.id.spinner1);
        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        maxClkDisplay = (TextView) rootView.findViewById(R.id.maxClkDisplay);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.gpu_governors,
                android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        governors = getResources().getStringArray(R.array.gpu_governors);
        try {
            freq = MyTools.readFile(this.getString(R.string.GPU_AVAIL_FREQ_PATH)).split(" ");
        } catch (Exception e) {
            MyTools.longToast(getActivity(), "GPU_FREQ_TABLE: " + e.toString());
            freq = new String[]{"n/a"};
        }
        refreshAll();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                String gpu_max_clk;
                try {
                    gpu_max_clk = MyTools.readFile(getActivity().getString(R.string.MAX_GPUCLK_PATH));
                } catch (Exception e) {
                    MyTools.longToast(getActivity(), "gpu_max_clk" + e.toString());
                    gpu_max_clk = "n/a";
                }

                if (gpu_max_clk.contains("/")) {
                    maxClkDisplay.setText("###");
                } else {
                    maxClkDisplay.setText(scaleDown(FREQ.get(FREQ.size() - 1 - progress)));
                }
            }
        });

        return rootView;
    }

    void refreshAll() {

        ArrayList<String> gpuGovernors = new ArrayList<String>();
        Collections.addAll(gpuGovernors, governors);

        int ind;
        try {
            ind = gpuGovernors.indexOf(MyTools.readFile(this.getString(R.string.GPU_GOV_PATH)));
        } catch (Exception e) {
            ind = -1;
        }

        if (ind == -1)
            spinner.setSelection(0, true);
        else
            spinner.setSelection(ind, true);

        FREQ = new ArrayList<String>();
        Collections.addAll(FREQ, freq);

        seekBar.setMax(FREQ.size() - 1);
        String gpu_max_clk;
        try {
            gpu_max_clk = MyTools.readFile(this.getString(R.string.MAX_GPUCLK_PATH));
        } catch (Exception e) {
            MyTools.longToast(getActivity(), "gpu_max_clk: " + e.toString());
            gpu_max_clk = "n/a";
        }

        if (gpu_max_clk.equals("n/a")) {
            maxClkDisplay.setText("###");
        } else {
            maxClkDisplay.setText(scaleDown(gpu_max_clk));
            seekBar.setProgress(FREQ.size() - 1 - FREQ.indexOf(gpu_max_clk));
        }

        setOnBoot.setChecked(
                setOnBootFile.exists() && setOnBootAgent.exists() &&
                        !setOnBootFile.isDirectory() && !setOnBootAgent.isDirectory()
        );

    }

    private void saveAll() {
        int i = spinner.getSelectedItemPosition();
        switch (i) {
            case 0:
                MyTools.write(governors[i], this.getString(R.string.GPU_POLICY_PATH));
                break;
            default:
                MyTools.write("trustzone", this.getString(R.string.GPU_POLICY_PATH));
                MyTools.write(governors[i], this.getString(R.string.GPU_GOV_PATH));
                break;
        }
        String mxc = scaleUp(maxClkDisplay.getText().toString());
        if (!mxc.equals("zoro"))
            MyTools.write(mxc, this.getString(R.string.MAX_GPUCLK_PATH));

        MyTools.toast(getActivity(), R.string.toast_done_succ);
    }

    private String scaleDown(String s) {
        try {
            int dec = 1000000;
            return (Long.parseLong(s) / dec) + "";
        } catch (Exception e) {
            return "zoro";
        }
    }

    private String scaleUp(String s) {
        try {
            int inc = 1000000;
            return (Long.parseLong(s) * inc) + "";
        } catch (Exception e) {
            return "zoro";
        }
    }

    private void prepareBootScript() {
        try {
            if (!scriptsDir.exists())
                scriptsDir.mkdirs();

            if (setOnBootFile.exists() && !setOnBootFile.isDirectory())
                setOnBootFile.delete();


            String[] values;
            String[] destinations;

            switch (spinner.getSelectedItemPosition()) {
                case 0:
                    values = new String[]{
                            governors[spinner.getSelectedItemPosition()],
                            freq[freq.length - 1 - seekBar.getProgress()]
                    };

                    destinations = new String[]{
                            this.getString(R.string.GPU_POLICY_PATH),
                            this.getString(R.string.MAX_GPUCLK_PATH)
                    };
                    break;
                default:
                    values = new String[]{
                            "trustzone",
                            governors[spinner.getSelectedItemPosition()],
                            freq[freq.length - 1 - seekBar.getProgress()]
                    };
                    destinations = new String[]{
                            this.getString(R.string.GPU_POLICY_PATH),
                            this.getString(R.string.GPU_GOV_PATH),
                            this.getString(R.string.MAX_GPUCLK_PATH)
                    };
            }

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

    private void removeBootFile() {
        MyTools.removeFile(setOnBootFile);
    }
}
