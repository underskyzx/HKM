package com.themike10452.hellscorekernelmanager;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.themike10452.hellscorekernelmanager.Blackbox.Blackbox;

import java.io.File;


public class SoundControlFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    public static String setOnBootFileName = "sound";
    private View view;
    private boolean linkRulesApply, linked = true;
    private CheckBox setOnBoot;
    private File setOnBootFile;

    private EditText left_hp_gain_display, right_hp_gain_display,
            left_hp_pa_display, right_hp_pa_display,
            speaker_gain_display, mic_gain_display, camMic_gain_display;

    private SeekBar left_hp_gain, right_hp_gain,
            left_hp_pa, right_hp_pa,
            speaker_gain, mic_gain, camMic_gain;

    public SoundControlFragment() {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_sound_control, container, false);
        setHasOptionsMenu(true);

        String s = MyTools.readFile(getString(R.string.SOUND_CONTROL_VERSION_PATH));
        if (!s.equals("n/a")) {
            TextView tv = (TextView) view.findViewById(R.id.title);
            if (s.contains(":"))
                tv.setText(tv.getText().toString().replace("###", (s.split(":"))[1]));
            else
                tv.setText(tv.getText().toString().replace("###", (s.split(":"))[1]));
        }

        left_hp_gain_display = (EditText) view.findViewById(R.id.left_hp_gain_display);
        right_hp_gain_display = (EditText) view.findViewById(R.id.right_hp_gain_display);
        left_hp_pa_display = (EditText) view.findViewById(R.id.left_hp_pa_display);
        right_hp_pa_display = (EditText) view.findViewById(R.id.right_hp_pa_display);
        speaker_gain_display = (EditText) view.findViewById(R.id.speaker_gain_display);
        mic_gain_display = (EditText) view.findViewById(R.id.mic_gain_display);
        camMic_gain_display = (EditText) view.findViewById(R.id.camMic_gain_display);

        left_hp_gain = (SeekBar) view.findViewById(R.id.left_hp_gain);
        left_hp_gain.setOnSeekBarChangeListener(this);
        right_hp_gain = (SeekBar) view.findViewById(R.id.right_hp_gain);
        right_hp_gain.setOnSeekBarChangeListener(this);
        left_hp_pa = (SeekBar) view.findViewById(R.id.left_hp_pa);
        left_hp_pa.setOnSeekBarChangeListener(this);
        right_hp_pa = (SeekBar) view.findViewById(R.id.right_hp_pa);
        right_hp_pa.setOnSeekBarChangeListener(this);
        speaker_gain = (SeekBar) view.findViewById(R.id.speaker_gain);
        speaker_gain.setOnSeekBarChangeListener(this);
        mic_gain = (SeekBar) view.findViewById(R.id.mic_gain);
        mic_gain.setOnSeekBarChangeListener(this);
        camMic_gain = (SeekBar) view.findViewById(R.id.camMic_gain);
        camMic_gain.setOnSeekBarChangeListener(this);

        try {
            String ss = (MySQLiteAdapter.select(getActivity(), DBHelper.SETTINGS_TABLE,
                    DBHelper.SETTINGS_TABLE_KEY, DBHelper.sound_linkLR_entry,
                    new String[]{DBHelper.SETTINGS_TABLE_COLUMN1}))[0];
            if (!ss.equals("true") && !ss.equals("false"))
                throw new Exception();
            linked = Boolean.parseBoolean(ss);
        } catch (Exception e) {
            MainActivity.showDonationDialog(getActivity());
            linked = true;
            MySQLiteAdapter.insertOrUpdate(
                    getActivity(),
                    DBHelper.SETTINGS_TABLE,
                    new String[]{
                            DBHelper.SETTINGS_TABLE_KEY,
                            DBHelper.SETTINGS_TABLE_COLUMN1,
                            DBHelper.SETTINGS_TABLE_COLUMN2
                    },
                    new String[]{
                            DBHelper.sound_linkLR_entry,
                            Boolean.toString(linked),
                            "null"
                    }
            );
        }

        setOnBoot = (CheckBox) view.findViewById(R.id.setOnBoot);

        ((CheckBox) view.findViewById(R.id.link)).setChecked(linked);

        ((CheckBox) view.findViewById(R.id.link)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MySQLiteAdapter.insertOrUpdate(
                        getActivity(),
                        DBHelper.SETTINGS_TABLE,
                        new String[]{
                                DBHelper.SETTINGS_TABLE_KEY,
                                DBHelper.SETTINGS_TABLE_COLUMN1,
                                DBHelper.SETTINGS_TABLE_COLUMN2
                        },
                        new String[]{
                                DBHelper.sound_linkLR_entry,
                                Boolean.toString(b),
                                "null"
                        }
                );
            }
        });

        refresh(false);

        return view;
    }

    private void refresh(boolean bool) {

        linkRulesApply = bool;

        File file = new File(getActivity().getFilesDir() + File.separator + setOnBootFileName);
        setOnBoot.setChecked(file.exists() && file.isFile());

        String a, b, c, d, e;
        {
            a = MyTools.readFile(getString(R.string.HP_GAIN_PATH));
            try {
                short a1 = Blackbox.tool3(Short.parseShort((a.split(" "))[0]), (byte) 1);
                short a2 = Blackbox.tool3(Short.parseShort((a.split(" "))[1]), (byte) 1);
                left_hp_gain_display.setText(Short.toString(a1));
                left_hp_gain.setProgress(a1 + 20);
                right_hp_gain_display.setText(Short.toString(a2));
                right_hp_gain.setProgress(a2 + 20);
            } catch (Exception ex) {
                ex.printStackTrace();
                left_hp_gain_display.setText("n/a");
                right_hp_gain_display.setText("n/a");
            }
        }
        {
            b = MyTools.readFile(getString(R.string.HP_PA_GAIN_PATH));
            try {
                short b1 = Blackbox.tool3(Short.parseShort((b.split(" "))[0]), (byte) 2);
                short b2 = Blackbox.tool3(Short.parseShort((b.split(" "))[1]), (byte) 2);
                left_hp_pa_display.setText(Short.toString(b1));
                left_hp_pa.setProgress(b1 + 6);
                right_hp_pa_display.setText(Short.toString(b2));
                right_hp_pa.setProgress(b2 + 6);
            } catch (Exception ex) {
                ex.printStackTrace();
                left_hp_pa_display.setText("n/a");
                right_hp_pa_display.setText("n/a");
            }
        }
        {
            c = MyTools.readFile(getString(R.string.SPEAKER_GAIN_PATH));
            try {
                short c1 = Blackbox.tool3(Short.parseShort((c.split(" "))[0]), (byte) 1);
                short c2 = Blackbox.tool3(Short.parseShort((c.split(" "))[1]), (byte) 1);
                speaker_gain_display.setText(Short.toString(c1));
                speaker_gain.setProgress(c1 + 20);
                speaker_gain_display.setText(Short.toString(c2));
                speaker_gain.setProgress(c2 + 20);
            } catch (Exception ex) {
                ex.printStackTrace();
                speaker_gain_display.setText("n/a");
                speaker_gain_display.setText("n/a");
            }
        }
        {
            d = MyTools.readFile(getString(R.string.MIC_GAIN_PATH));
            try {
                short d1 = Blackbox.tool3(Short.parseShort((d.split(" "))[0]), (byte) 1);
                mic_gain_display.setText(Short.toString(d1));
                mic_gain.setProgress(d1 + 20);
            } catch (Exception ex) {
                ex.printStackTrace();
                mic_gain_display.setText("n/a");
            }
        }
        {
            e = MyTools.readFile(getString(R.string.CAMMIC_GAIN_PATH));
            try {
                short e1 = Blackbox.tool3(Short.parseShort((e.split(" "))[0]), (byte) 1);
                camMic_gain_display.setText(Short.toString(e1));
                camMic_gain.setProgress(e1 + 20);
            } catch (Exception ex) {
                ex.printStackTrace();
                camMic_gain_display.setText("n/a");
            }
        }
        linkRulesApply = !linkRulesApply;

    }

    private void save() {

        try {

            final String rep1 = Blackbox.tool1(
                    Short.parseShort(left_hp_gain_display.getText().toString()),
                    Short.parseShort(right_hp_gain_display.getText().toString()),
                    (byte) 2
            );
            final String rep2 = Blackbox.tool2(
                    Short.parseShort(left_hp_pa_display.getText().toString()),
                    Short.parseShort(right_hp_pa_display.getText().toString()),
                    (byte) 2
            );
            final String rep3 = Blackbox.tool1(
                    Short.parseShort(speaker_gain_display.getText().toString()),
                    Short.parseShort(speaker_gain_display.getText().toString()),
                    (byte) 2
            );
            final String rep4 = Blackbox.tool1(
                    Short.parseShort(mic_gain_display.getText().toString())
            );
            final String rep5 = Blackbox.tool1(
                    Short.parseShort(camMic_gain_display.getText().toString())
            );

            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        MyTools.SUhardWrite("0", getString(R.string.SOUND_LOCK_PATH));
                        MyTools.SUhardWrite(rep1, getString(R.string.HP_GAIN_PATH));
                        MyTools.SUhardWrite(rep2, getString(R.string.HP_PA_GAIN_PATH));
                        MyTools.SUhardWrite(rep3, getString(R.string.SPEAKER_GAIN_PATH));
                        MyTools.SUhardWrite(rep4, getString(R.string.MIC_GAIN_PATH));
                        MyTools.SUhardWrite(rep5, getString(R.string.CAMMIC_GAIN_PATH));
                        MyTools.SUhardWrite("1", getString(R.string.SOUND_LOCK_PATH));

                        //set on boot prep

                        if (setOnBoot.isChecked()) {
                            if (setOnBootFile == null)
                                setOnBootFile = new File(getActivity().getFilesDir()
                                        + File.separator + setOnBootFileName);
                            MyTools.write(
                                    String.format(
                                            "%s.%s::%s.%s::%s.%s::%s::%s",
                                            left_hp_gain_display.getText().toString(),
                                            right_hp_gain_display.getText().toString(),
                                            left_hp_pa_display.getText().toString(),
                                            right_hp_pa_display.getText().toString(),
                                            speaker_gain_display.getText().toString(),
                                            speaker_gain_display.getText().toString(),
                                            mic_gain_display.getText().toString(),
                                            camMic_gain_display.getText().toString()
                                    ),
                                    setOnBootFile.toString()
                            );

                        } else {
                            if (setOnBootFile == null)
                                setOnBootFile = new File(getActivity().getFilesDir()
                                        + File.separator + setOnBootFileName);
                            if (setOnBootFile.exists() && setOnBootFile.isFile())
                                setOnBootFile.delete();
                        }

                    } catch (Exception eee) {
                        eee.printStackTrace();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean successful) {
                    if (successful)
                        MyTools.toast(getActivity(), R.string.toast_done_succ);
                    else
                        MyTools.toast(getActivity(), R.string.somethingWentWrong);
                }
            }.execute();

        } catch (Exception eee) {
            eee.printStackTrace();
            MyTools.toast(getActivity(), R.string.somethingWentWrong);
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        boolean link = ((CheckBox) view.findViewById(R.id.link)).isChecked();
        switch (seekBar.getId()) {
            case R.id.left_hp_gain:
                left_hp_gain_display.setText(Integer.toString(seekBar.getProgress() - 20));
                if (link && linkRulesApply)
                    right_hp_gain.setProgress(seekBar.getProgress());
                break;
            case R.id.right_hp_gain:
                right_hp_gain_display.setText(Integer.toString(seekBar.getProgress() - 20));
                if (link && linkRulesApply)
                    left_hp_gain.setProgress(seekBar.getProgress());
                break;
            case R.id.left_hp_pa:
                left_hp_pa_display.setText(Integer.toString(seekBar.getProgress() - 6));
                if (link && linkRulesApply)
                    right_hp_pa.setProgress(seekBar.getProgress());
                break;
            case R.id.right_hp_pa:
                right_hp_pa_display.setText(Integer.toString(seekBar.getProgress() - 6));
                if (link && linkRulesApply)
                    left_hp_pa.setProgress(seekBar.getProgress());
                break;
            case R.id.speaker_gain:
                speaker_gain_display.setText(Integer.toString(seekBar.getProgress() - 20));
                break;
            case R.id.mic_gain:
                mic_gain_display.setText(Integer.toString(seekBar.getProgress() - 20));
                break;
            case R.id.camMic_gain:
                camMic_gain_display.setText(Integer.toString(seekBar.getProgress() - 20));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh(false);
                return true;
            case R.id.action_apply:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
