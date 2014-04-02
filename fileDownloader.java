package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class fileDownloader extends AsyncTask<Integer, Void, Boolean> {

    private final Activity activity;
    private final boolean force;
    private ProgressDialog progressDialog;

    fileDownloader(Activity activity, boolean arg) {
        this.activity = activity;
        this.force = arg;
    }

    @Override
    public void onPreExecute() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(activity.getString(R.string.pleaseWait));
        progressDialog.setMessage(activity.getString(R.string.downloadingList));
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        try {
            Log.d("TAG", "force = " + force);
            if (!force && !(MyTools.readFile("/proc/version").toLowerCase().contains("hellsgod")))
                return false;

            URL url = new URL("https://dl.dropboxusercontent.com/s/8ro7mw2jfn75d2j/hellscoreKernelList.txt");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            urlConnection.connect();

            File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "hellscore_update.txt");

            FileOutputStream fileOutput = new FileOutputStream(file);

            InputStream inputStream = urlConnection.getInputStream();

            int totalSize = urlConnection.getContentLength();
            progressDialog.setMax(totalSize);
            int downloadedSize = 0;

            byte[] buffer = new byte[1024];
            int bufferLength;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                progressDialog.setProgress(downloadedSize);

            }
            fileOutput.close();
            return true;

        } catch (MalformedURLException e) {
            Log.d("TAG", e.toString());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onPostExecute(Boolean successful) {
        progressDialog.dismiss();
        if (successful) {
            InfoTabFragment.postUpdates(activity);
        } else {
            new AlertDialog.Builder(activity)
                    .setCancelable(true)
                    .setTitle(":(")
                    .setMessage(activity.getString(R.string.service_not_available))
                    .setNeutralButton("", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
        }
    }
}
