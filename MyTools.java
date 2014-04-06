package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class MyTools {

    private static Shell.Interactive rootSession;
    private static String dataDir;

    public static void initiateSUShell(final Activity activity) {

        if (rootSession == null) {
            rootSession = new Shell.Builder()
                    .setMinimalLogging(true).
                            useSU().
                            open(new Shell.OnCommandResultListener() {
                                @Override
                                public void onCommandResult(int commandCode, final int exitCode, List<String> output) {
                                    if (exitCode != Shell.OnCommandResultListener.SHELL_RUNNING) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                        builder.setMessage("Failed to get Root Access. Please Check your SuperSU.")
                                                .setCancelable(false)
                                                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        System.exit(exitCode);
                                                    }
                                                }).show();
                                    }
                                }
                            });
        }
    }

    public static void SUhardWrite(final String data, final String file) {
        //can not be used on main thread!
        Shell.SU.run(String.format("echo \"%s\" > \"%s\"", data, file));
    }

    public static void write(final String data, final String file) {
        if (rootSession != null) {
            rootSession.addCommand(String.format("echo \"%s\" > \"%s\"", data, file));
        } else {
            initiateSUShell(null);
            rootSession.addCommand(String.format("echo \"%s\" > \"%s\"", data, file));
        }
    }

    public static void write(final int data, final String file) {
        if (rootSession != null) {
            rootSession.addCommand(String.format("echo \"%s\" > \"%s\"", data, file));
        } else {
            initiateSUShell(null);
            rootSession.addCommand(String.format("echo \"%s\" > \"%s\"", data, file));
        }
    }

    public static void execTerminalCommand(final String[] cmd) {
        if (rootSession != null) {
            for (String c : cmd)
                rootSession.addCommand(c);
        } else {
            initiateSUShell(null);
            for (String c : cmd)
                rootSession.addCommand(c);
        }
    }

    public static String readFile(String fp) {
        File f = new File(fp);
        if (f.exists() && f.isFile()) {
            if (f.canRead()) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                    return bufferedReader.readLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                File filesDir = new File(dataDir + "Files");
                if (!filesDir.exists() && !filesDir.isDirectory()) {
                    filesDir.mkdir();
                }
                File dump = new File(filesDir.toString() + "dump.bin");
                if (!dump.exists()) {
                    try {
                        dump.createNewFile();
                    } catch (IOException ignored) {
                    }
                }
                Shell.SU.run("cat " + f.toString() + " > " + dump.toString());
                Shell.SU.run("chmod 666 " + f.toString());
                if (dump.exists() && dump.isFile())
                    return readDump(dump.toString());
                else
                    return "n/a";
            }
        }
        return "n/a";
    }

    public static int catInt(String file, int errorCode) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String value = bufferedReader.readLine();
            return Integer.parseInt(value);
        } catch (Exception e) {
            Log.e("TAG1", "cat exception: " + e);
            return errorCode;
        }
    }

    public static ArrayList<String> catToList(String file) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                list.add(str);
            }
        } catch (Exception e) {
            list.clear();
            list.add("ladyGaga");
            return list;
        }
        return list;
    }

    public static String getDataDir(Activity activity) {
        if (dataDir == null) {
            PackageManager m = activity.getPackageManager();
            String xdataDir = activity.getPackageName();
            try {
                PackageInfo p = m.getPackageInfo(xdataDir, 0);
                dataDir = p.applicationInfo.dataDir;
                return dataDir;
            } catch (Exception e) {
                return "ladyGaga";
            }
        } else {
            return dataDir;
        }
    }

    public static String getApplicationLabel(Activity activity, String info) {
        PackageManager pm = activity.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(info, 0);
            return ai.publicSourceDir;
        } catch (Exception e) {
            return "ladyGaga";
        }
    }

    public static void fillScript(File file, String[] values, String[] destinations, String flags) throws Exception {
        if (file.exists() && !file.isDirectory())
            file.delete();

        if (!file.exists() || file.isDirectory()) {
            file.createNewFile();
        }

        PrintWriter p = new PrintWriter(new FileWriter(file));
        p.println("#!/system/bin/sh");
        p.println("echo " + file.getName() + ": called -- `date` >> /sdcard/HKM.log");
        if (flags.contains(":delay:")) {
            p.println("if [ \"`ps | grep -m 1 [a]ndroid`\" ]; then");
            p.println("sleep 3");
        } else if (flags.contains(":delay45:")) {
            p.println("if [ \"`ps | grep -m 1 [a]ndroid`\" ]; then");
            p.println("sleep 45");
        }
        p.println("echo " + file.getName() + ": executed -- `date` >> /sdcard/HKM.log");
        for (int i = 0; i < values.length; i++)
            p.println("echo " + values[i] + " > " + destinations[i]);

        if (flags.contains(":delay")) {
            p.println("else");
            p.println("sleep 5");
            p.println("$0");
            p.println("fi");
        }
        p.println();
        p.flush();
        p.close();
        execTerminalCommand(new String[]{
                "chmod 775 " + file.toString()
        });
    }

    public static void completeScriptWith(File file, ArrayList<String> values, String[] destinations) throws Exception {
        if (!file.exists() || file.isDirectory())
            file.createNewFile();

        PrintWriter p = new PrintWriter(new FileWriter(file, true));
        p.println("");
        switch (destinations.length) {
            case 1:
                for (String value : values) {
                    p.println("echo " + value + " > " + destinations[0]);
                }
                break;
            default:
                for (int i = 0; i < values.size(); i++) {
                    p.println("echo " + values.get(i) + " > " + destinations[i]);
                }
                break;
        }
        p.println();
        p.flush();
        p.close();
        MyTools.execTerminalCommand(new String[]{
                "chmod 775 " + file.toString()
        });
    }

    public static void completeScriptWith(File file, String[] values, String[] destinations) throws Exception {
        if (!file.exists() || file.isDirectory())
            file.createNewFile();

        PrintWriter p = new PrintWriter(new FileWriter(file, true));
        p.println("");
        switch (destinations.length) {
            case 1:
                for (String value : values) {
                    p.println("echo " + value + " > " + destinations[0]);
                }
                break;
            default:
                for (int i = 0; i < values.length; i++) {
                    p.println("echo " + values[i] + " > " + destinations[i]);
                }
                break;
        }
        p.flush();
        p.close();
        MyTools.execTerminalCommand(new String[]{
                "chmod 775 " + file.toString()
        });
    }

    public static void createBootAgent(final Activity a, final File scriptsDir) throws Exception {

        new AsyncTask<Integer, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Integer... integers) {
                File filesDir = new File(getDataDir(a) + File.separator + "files");
                if (!filesDir.exists() || !filesDir.isDirectory())
                    filesDir.mkdir();

                File setOnBootAgentTmpFile = new File(filesDir.toString() + File.separator + "tmpScript");
                File setOnBootAgentFile = new File(a.getString(R.string.setOnBootAgentFile));
                PrintWriter pw;

                if (setOnBootAgentTmpFile.exists() && !setOnBootAgentTmpFile.isDirectory())
                    setOnBootAgentTmpFile.delete();

                try {
                    setOnBootAgentTmpFile.createNewFile();
                } catch (IOException e) {
                    Log.e("TAG", e.toString());
                    return false;
                }

                try {
                    pw = new PrintWriter(new FileWriter(setOnBootAgentTmpFile, true));
                } catch (IOException e) {
                    Log.e("TAG", e.toString());
                    return false;
                }

                pw.println("#!/system/bin/sh");
                pw.println("if [ \"`cat /proc/version | grep -i hells`\" ];");
                pw.println("then");
                pw.println("echo hellsCore Manager: bootime -- `date` > /sdcard/HKM.log");
                pw.println("else");
                pw.println("exit 99");
                pw.println("fi");
                pw.println("if [ -d " + scriptsDir + " ];");
                pw.println("then");
                pw.println("busybox run-parts " + scriptsDir + ";");
                pw.println("fi;");
                pw.flush();
                pw.close();

                Shell.SU.run("mount -o remount rw /system");
                Shell.SU.run("chmod 775 " + setOnBootAgentTmpFile.toString());
                Shell.SU.run("rm /system/etc/init.d/99hellscore_*");
                Shell.SU.run("cat " + setOnBootAgentTmpFile.toString() + " > " + setOnBootAgentFile.toString());
                Shell.SU.run(new String[]{
                        "chmod 775 " + setOnBootAgentFile.toString(),
                        "rm " + setOnBootAgentTmpFile.toString(),
                        "mount -o remount ro /system"
                });
                return true;
            }
        }.execute();

    }

    public static void removeFile(File f) {
        if (f.exists() && !f.isDirectory())
            MyTools.execTerminalCommand(new String[]{
                    "mount -o remount rw /system",
                    "rm " + f.toString(),
                    "mount -o remount rw /system"});
    }

    public static String parseIntFromBoolean(boolean b) {
        if (b)
            return "1";
        else
            return "0";
    }

    public static void toast(Context c, int id) {
        Toast.makeText(c, id, Toast.LENGTH_SHORT).show();
    }

    public static void longToast(Context c, int id) {
        Toast.makeText(c, id, Toast.LENGTH_LONG).show();
    }

    public static void toast(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    public static void longToast(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
    }

    public static void log(Activity activity, String log, String file) {
        File innerLog = new File(file);
        if (!innerLog.exists()) {
            try {
                innerLog.createNewFile();
            } catch (Exception e) {
                Log.e("TAG", "error creating inner log file: " + e);
                return;
            }
        }
        MyTools.execTerminalCommand(new String[]{"chmod 775 " + innerLog.toString()});
        longToast(activity, log);
        write(log, file);
    }

    public static String readFirstLine(String file) throws Exception {
        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(new FileReader(new File(file)));
        return bufferedReader.readLine();
    }

    private static String readDump(String fp) {
        File f = new File(fp);
        if (f.exists() && f.isFile() && f.canRead()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                return bufferedReader.readLine();
            } catch (Exception e) {
                e.printStackTrace();
                return "n/a";
            }
        } else {
            return "n/a";
        }
    }

}
