package com.themike10452.hellscorekernelmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter {

    private Context mContext;
    private int id;
    private ArrayList<String> names;
    private ArrayList<String> values;


    public CustomListAdapter(Context context, int textViewId,
                             ArrayList<String> xnames, ArrayList<String> xvalues) {
        super(context, textViewId, xnames);
        mContext = context;
        id = textViewId;
        names = xnames;
        values = xvalues;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View mView = v;
        if (mView == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(id, null);
        }

        TextView text = null;
        if (mView != null) {
            text = (TextView) mView.findViewById(R.id.textViewz);
        }
        text.setText(names.get(position));

        TextView edit = (TextView) mView.findViewById(R.id.editText);
        edit.setText(values.get(position));

        return mView;
    }

}

