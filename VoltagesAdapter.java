package com.themike10452.hellscorekernelmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mike on 8/26/2014.
 */

public class VoltagesAdapter extends ArrayAdapter {

    private static final ArrayList<String> PRINTS = new ArrayList<String>();
    private static Context mContext;
    private static int resId;
    private static String[] VOLS, FREQS;
    private static SeekBar[] SEEKBARS;
    private static int MAX_STEPS;
    private static int STEP_VALUE;

    public VoltagesAdapter(Context context, int resource, String[] vols, String[] freqs, int max_steps, int step_value) {
        super(context, resource, vols);
        mContext = context;
        resId = resource;
        VOLS = vols;
        FREQS = freqs;
        MAX_STEPS = max_steps;
        STEP_VALUE = step_value;
        SEEKBARS = new SeekBar[vols.length];
        PRINTS.clear();
    }

    public static ArrayList<String> getPrints() {
        return PRINTS;
    }

    public static String getPrint(int index) {
        String print = "n/a";
        try {
            print = PRINTS.get(index);
        } catch (Exception ignored) {
        }
        return print;

    }

    public static void incVols(int value) {
        for (SeekBar seekBar : SEEKBARS)
            seekBar.setProgress(seekBar.getProgress() + (value / STEP_VALUE));
    }

    private static String buildPrint(String s1, String s2) {
        return String.format("%s:%s", s1, s2);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View mView = convertView;
        if (mView == null) {
            mView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(resId, null);
        }

        if (PRINTS.size() >= position + 1) {
            PRINTS.remove(position);
        }
        PRINTS.add(position, FREQS[position] + ":" + VOLS[position]);

        SeekBar seekBar = (SeekBar) mView.findViewById(R.id._seekBar);
        SEEKBARS[position] = seekBar;
        final TextView vol = (TextView) mView.findViewById(R.id._vol);
        final TextView freq = (TextView) mView.findViewById(R.id._freq);

        seekBar.setMax(MAX_STEPS);
        seekBar.setProgress(MAX_STEPS / 2);
        freq.setText(FREQS[position]);
        vol.setText(VOLS[position]);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try {
                    int OLDV = Integer.parseInt(VOLS[position]);
                    int NEWV = (seekBar.getProgress() - (MAX_STEPS / 2)) * STEP_VALUE;
                    NEWV += OLDV;
                    vol.setText(Integer.toString(NEWV));
                    PRINTS.remove(position);
                    PRINTS.add(position, FREQS[position] + ":" + NEWV);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        return mView;
    }

}
