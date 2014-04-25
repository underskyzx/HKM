package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.themike10452.hellscorekernelmanager.Blackbox.Blackbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import eu.chainfire.libsuperuser.Shell;

public class fileDownloader extends AsyncTask<String, Integer, Boolean> {

    private static Dialog dialog;
    private static HttpURLConnection urlConnection;
    private static boolean killedByMaster;
    private static String filename;
    private final Activity activity;
    private final boolean force;
    private String mode;
    private ProgressDialog progressDialog;

    fileDownloader(Activity activity, boolean arg, String mode) {
        killedByMaster = false;
        this.activity = activity;
        this.force = arg;
        this.mode = mode;
        filename = "";
    }

    private static void finish(final Activity activity, boolean install) {
        if (install) {
            String recoveryDir = "/cache/recovery";
            new AsyncTask<String, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(String... strings) {
                    try {
                        // a dirty way to check if /cache/recovery exists
                        Shell.SU.run("if [ -e " + strings[0] + " ];then echo iffoooo; fi").get(0);
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean doIt) {
                    if (doIt) {
                        doIt(activity);
                    } else {
                        new AlertDialog.Builder(activity)
                                .setMessage(activity.getString(R.string.kernelDownload_complete)
                                        .replace("###",
                                                Environment.getExternalStorageDirectory().getPath() +
                                                        File.separator +
                                                        activity.getString(R.string.kernel_download_location)
                                        ))
                                .setCancelable(true)
                                .setTitle(activity.getString(R.string.toast_done))
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        MyTools.longToast(activity, R.string.toast_openRecovery_notSupported);
                                    }
                                })
                                .show();
                    }
                }
            }.execute(recoveryDir);
        } else {
            new AlertDialog.Builder(activity)
                    .setMessage(activity.getString(R.string.kernelDownload_complete)
                            .replace("###",
                                    Environment.getExternalStorageDirectory().getPath() +
                                            File.separator +
                                            activity.getString(R.string.kernel_download_location)
                            ))
                    .setCancelable(true)
                    .setTitle(activity.getString(R.string.toast_done))
                    .show();
        }
    }

    private static void doIt(Activity activity) {
        File recoveryDir = new File("/cache/recovery");
        File openRecoveryScript = new File(recoveryDir.toString() + File.separator +
                "openrecoveryscript");

        File file = new File(Environment.getExternalStorageDirectory().getPath() +
                File.separator + activity.getString(R.string.kernel_download_location));
        if (file.isFile() && file.exists()) {
            Shell.SU.run(new String[]{
                    "mount -o remount rw /cache",
                    "echo install " + file.toString() + " > " +
                            openRecoveryScript.toString(),
                    "chmod 775 " + openRecoveryScript.toString()
            });
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.title_rebootToRecovery))
                    .setMessage(activity.getString(R.string.message_rebootToRecovery))
                    .setPositiveButton(activity.getString(R.string.button_rebootNow), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Shell.SU.run("reboot recovery");
                                }
                            }).start();
                        }
                    })
                    .setNegativeButton(activity.getString(R.string.button_later), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .show();
        } else {
            MyTools.longToast(activity, R.string.toast_failed);
        }
    }

    public boolean killAll(Activity act) {
        if (urlConnection == null)
            return false;
        killedByMaster = true;
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                urlConnection.disconnect();
            }
        }).start();*/
        return (new File(Environment.getExternalStorageDirectory().getPath() +
                File.separator + act.getString(R.string.kernel_download_location)))
                .delete();
    }

    @Override
    public void onPreExecute() {
        if (mode.equals("mode1")) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setTitle(activity.getString(R.string.pleaseWait));
            progressDialog.setMessage(activity.getString(R.string.downloadingList));
            progressDialog.show();
        } else if (mode.equals("mode2")) {
            dialog = new Dialog(activity);
            dialog.setTitle(activity.getString(R.string.title_downloading));
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.download_dialog);
            dialog.show();
        }
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        if (strings[0].equals("mode1"))
            try {

                if (!force && !MyTools.readFile("/proc/version").toLowerCase().contains("hellsgod") && !Blackbox.tool4(activity))
                    return false;

                URL url = new URL(activity.getString(R.string.kernel_list_url).trim());

                urlConnection = (HttpURLConnection) url.openConnection();

                //urlConnection.setRequestMethod("GET"); << useless
                //urlConnection.setDoOutput(true); << useless

                urlConnection.connect();

                File file = new File(Environment.getExternalStorageDirectory().getPath() +
                        File.separator + activity.getString(R.string.hellscore_update_file));

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
                    progressDialog.setProgress(downloadedSize); //display progress
                    if (killedByMaster) { //stop the download
                        inputStream.close();
                        fileOutput.close();
                        urlConnection.disconnect();
                    }
                }
                fileOutput.close();
                urlConnection.disconnect();
                return true;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        else if (strings[0].equals("mode2"))
            try {

                if (!force && !MyTools.readFile("/proc/version").toLowerCase().contains("hellsgod") && !Blackbox.tool4(activity))
                    return false;

                (dialog.findViewById(R.id.abortButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        killAll(activity);
                        dialog.cancel();
                    }
                });

                if (strings[2] != null)
                    filename = strings[2];

                Shell.SU.run("mount -o remount,rw /cache");

                URL url = new URL(strings[1]);

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.connect();

                File file = new File(Environment.getExternalStorageDirectory().getPath() +
                        File.separator + activity.getString(R.string.kernel_download_location));

                FileOutputStream fileOutput = new FileOutputStream(file);

                InputStream inputStream = urlConnection.getInputStream();

                int totalSize = urlConnection.getContentLength();
                int downloadedSize = 0;

                byte[] buffer = new byte[1024];
                int bufferLength;

                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    publishProgress(downloadedSize, totalSize); //display progress
                    if (killedByMaster) { //stop the download
                        inputStream.close();
                        fileOutput.close();
                        urlConnection.disconnect();
                    }
                }

                fileOutput.close();
                urlConnection.disconnect();
                return true;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        else
            return false;
    }

    @Override
    public void onPostExecute(Boolean successful) {
        if (mode.equals("mode1") && progressDialog != null) progressDialog.dismiss();
        if (successful) {
            if (mode.equals("mode1"))
                InfoTabFragment.postUpdates(activity);
            else if (mode.equals("mode2")) {
                finish(activity, ((CheckBox) dialog.findViewById(R.id.autoInstall)).isChecked());
                if (dialog != null) dialog.dismiss();
            }
        } else {
            if (!killedByMaster)
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

    @Override
    protected void onProgressUpdate(Integer... args) {
        ((ProgressBar) dialog.findViewById(R.id.progressBar))
                .setIndeterminate(false);
        ((TextView) dialog.findViewById(R.id.progressView))
                .setText(args[0] + "/" + args[1]);
        ((ProgressBar) dialog.findViewById(R.id.progressBar))
                .setMax(args[1]);
        ((ProgressBar) dialog.findViewById(R.id.progressBar))
                .setProgress(args[0]);
        //dialog.setCancelable(true);
        ((TextView) dialog.findViewById(R.id.filename)).setText(filename);
    }
}
