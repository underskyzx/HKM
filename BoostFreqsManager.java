package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.util.ArrayList;

/**
 * Created by Mike on 9/6/2014.
 */
public class BoostFreqsManager extends Activity {

    public static ArrayList<String> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boost_freqs);
        LinearLayout master = (LinearLayout) findViewById(R.id.master);
        Adapter adapter = new Adapter(getApplicationContext());
        for (int i = 0; i < 4; i++)
            master.addView(adapter.getView(i));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            CpuControlFragment.instance.updateBoostFreqs(values);
        } catch (Exception e) {
            MyTools.toast(getApplicationContext(), R.string.somethingWentWrong);
        }
    }

    private class Adapter extends ArrayAdapter {

        public Adapter(Context context) {
            super(context, R.layout.cpu_boost_freq_handler, new Integer[]{0, 1, 2, 3});
            values = MyTools.catToList(Library.TOUCH_BOOST_FREQS_PATH);
        }

        public View getView(int position) {
            return getView(position, null, null);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView == null ? ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cpu_boost_freq_handler, null) : convertView;

            ((TextView) v.findViewById(R.id.cpuID)).setText(String.format("CPU%s", position));
            try {
                final String[] elements = MyTools.listElements(Library.AVAIL_FREQ_PATH, " ");
                ArrayList<String> list = new ArrayList<String>();
                for (String s : elements)
                    list.add(s);

                final NumberPicker picker = (NumberPicker) v.findViewById(R.id.numberPicker);
                picker.setWrapSelectorWheel(false);
                picker.setMinValue(0);
                picker.setMaxValue(elements.length - 1);
                String tmp = MyTools.catToList(Library.TOUCH_BOOST_FREQS_PATH).get(position).trim();
                picker.setValue(list.indexOf(tmp));
                picker.setDisplayedValues(elements);
                picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                        try {
                            values.remove(position);
                        } catch (ArrayIndexOutOfBoundsException ignored) {
                        } finally {
                            values.add(position, elements[picker.getValue()]);
                        }
                    }
                });
            } catch (Exception ingored) {
            }

            return v;
        }
    }
}
