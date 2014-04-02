package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import eu.chainfire.libsuperuser.Shell;

public class ProgressTask extends AsyncTask<Integer, Void, Boolean> {

    private final ProgressDialog p;
    private final Activity activity;
    private final Intent intent;

    public ProgressTask(Activity activity, Intent i) {
        intent = i;
        this.activity = activity;
        p = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        p.setTitle(activity.getString(R.string.pleaseWait));
        p.setMessage(activity.getString(R.string.waitingSuperSU));
        p.setCancelable(false);
        p.setIndeterminate(true);
        p.show();

    }

    protected void onPostExecute(Boolean arg0) {
        activity.startActivity(intent);
        p.dismiss();
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        Shell.SU.run("SU");
        return true;
    }
}
