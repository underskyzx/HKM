package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.themike10452.hellscorekernelmanager.Blackbox.Library;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.chainfire.libsuperuser.Shell;

public class MyTools {

    private static Shell.Interactive rootSession;
    private static String dataDir;

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

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

    public static String[] listElements(String fp, String delim) throws FileNotFoundException, IOException {
        return readFile(fp).split(delim);
    }

    public static String readFile(String fp) throws IOException {
        File f = new File(fp);
        if (f.exists() && f.isFile()) {
            if (f.canRead()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                return bufferedReader.readLine();
            } else {
                throw new IOException();
            }
        } else {
            throw new FileNotFoundException();
        }
    }

    public static String readFile(String fp, String errorCode) {
        try {
            return readFile(fp);
        } catch (Exception e) {
            return errorCode;
        }
    }

    public static int catInt(String file, int errorCode) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String value = bufferedReader.readLine();
            bufferedReader.close();
            return Integer.parseInt(value);
        } catch (Exception e) {
            return errorCode;
        }
    }

    public static ArrayList<String> readToList(String file) {
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

    public static List<String> catToList(String file) {
        List<String> list = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("cat " + file).getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
            return list;
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFormattedKernelVersion() {
        String procVersionStr;

        try {
            procVersionStr = new BufferedReader(new FileReader(new File("/proc/version"))).readLine();

            final String PROC_VERSION_REGEX =
                    "Linux version (\\S+) " +
                            "\\((\\S+?)\\) " +
                            "(?:\\(gcc.+? \\)) " +
                            "(#\\d+) " +
                            "(?:.*?)?" +
                            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)";

            Pattern p = Pattern.compile(PROC_VERSION_REGEX);
            Matcher m = p.matcher(procVersionStr);

            if (!m.matches()) {
                return "Unavailable";
            } else if (m.groupCount() < 4) {
                return "Unavailable";
            } else {
                return (new StringBuilder(m.group(1)).append("\n").append(
                        m.group(2)).append(" ").append(m.group(3)).append("\n")
                        .append(m.group(4))).toString();
            }
        } catch (IOException e) {
            return "Unavailable";
        }
    }

    public static String getDataDir(Context context) {
        if (dataDir == null) {
            PackageManager m = context.getPackageManager();
            String xdataDir = context.getPackageName();
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

    public static void fillScript(File file, String[] values, String[] destinations, String flags) throws Exception {
        if (file.exists() && !file.isDirectory())
            file.delete();

        if (!file.exists() || file.isDirectory()) {
            file.createNewFile();
        }

        PrintWriter p = new PrintWriter(new FileWriter(file));
        p.println("#!/system/bin/sh");
        p.println("echo `date +%T` -- " + file.getName() + " called >> /sdcard/HKM.log");
        if (flags.contains(":delay:")) {
            p.println("if [ \"`ps | grep -m 1 [a]ndroid`\" ]; then");
            p.println("if [ \"$1\" != \"nodelay\" ]; then");
            p.println("sleep 3");
            p.println("fi");
        } else if (flags.contains(":delay45:")) { //Exit, to be launched on boot by OnBootService
            p.println("if [ \"$1\" != \"nodelay\" ]; then");
            p.println("echo `date +%T` -- " + file.getName() + " delayed >> /sdcard/HKM.log");
            p.println("exit 0");
            p.println("fi");
        }
        p.println("echo `date +%T` -- " + file.getName() + " executed >> /sdcard/HKM.log");
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && destinations[i] != null)
                p.println("echo " + values[i] + " > " + destinations[i]);
        }

        if (flags.contains(":delay:")) {
            p.println("else");
            p.println("if [ \"$1\" != \"nodelay\" ]; then");
            p.println("sleep 5");
            p.println("fi");
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

    public static void completeScriptWith(File file, String[] lines) throws Exception {
        if (!file.exists() || file.isDirectory())
            file.createNewFile();

        PrintWriter p = new PrintWriter(new FileWriter(file, true));
        p.println("");

        for (String line : lines)
            p.println(line);

        p.flush();
        p.close();
        MyTools.execTerminalCommand(new String[]{
                "chmod 775 " + file.toString()
        });
    }

    public static void createBootAgent(final Context context, final File scriptsDir) throws Exception {

        new AsyncTask<Integer, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Integer... integers) {
                File filesDir = context.getFilesDir();
                if (!filesDir.exists() || !filesDir.isDirectory())
                    filesDir.mkdir();

                File setOnBootAgentTmpFile = new File(filesDir.toString() + File.separator + "tmpScript");
                File setOnBootAgentFile = new File(Library.setOnBootAgentFile);
                PrintWriter pw;

                if (setOnBootAgentTmpFile.exists() && !setOnBootAgentTmpFile.isDirectory())
                    setOnBootAgentTmpFile.delete();

                try {
                    setOnBootAgentTmpFile.createNewFile();
                } catch (IOException e) {
                    return false;
                }

                try {
                    pw = new PrintWriter(new FileWriter(setOnBootAgentTmpFile, true));
                } catch (IOException e) {
                    return false;
                }

                pw.println("#!/system/bin/sh");
                pw.println("if [[ \"`cat /proc/version | grep -i hells`\" || -e \"/data/force_hkm\" ]];");
                pw.println("then");
                pw.println("echo `date +%T` -- boot_time > /sdcard/HKM.log");
                pw.println("else");
                pw.println("exit 99");
                pw.println("fi");
                pw.println("if [ -d " + scriptsDir + " ];");
                pw.println("then");
                pw.println("busybox run-parts " + scriptsDir + ";");
                pw.println("fi;");
                pw.flush();
                pw.close();

                Shell.SU.run("mount -o remount,rw /system");
                Shell.SU.run("chmod 775 " + setOnBootAgentTmpFile.toString());
                Shell.SU.run("rm /system/etc/init.d/99hellscore_*");
                Shell.SU.run("cat " + setOnBootAgentTmpFile.toString() + " > " + setOnBootAgentFile.toString());
                Shell.SU.run(new String[]{
                        "chmod 775 " + setOnBootAgentFile.toString(),
                        "rm " + setOnBootAgentTmpFile.toString(),
                        "mount -o remount,ro /system"
                });
                return true;
            }
        }.execute();

    }

    public static void removeFile(File f) {
        if (f.exists() && f.isFile())
            MyTools.execTerminalCommand(new String[]{
                    "mount -o remount,rw /system",
                    "rm " + f.toString(),
                    "mount -o remount,ro /system"});
    }

    public static String parseIntFromBoolean(boolean b) {
        if (b)
            return "1";
        else
            return "0";
    }

    public static Boolean parseBoolFromInteger(int i) {
        return (i != 0);
    }

    public static Boolean parseBoolFromString(String s) {
        return (!s.trim().equals("0"));
    }

    public static String[] addToArray(String[] array, String string, int index) {
        String[] newArray = new String[array.length + 1];
        for (int i = 0; i < newArray.length; i++) {
            if (i < index)
                newArray[i] = array[i];
            else if (i == index)
                newArray[i] = string;
            else
                newArray[i] = array[i - 1];
        }
        return newArray;
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
            } catch (Exception ignored) {
            }
        }
        MyTools.execTerminalCommand(new String[]{"chmod 775 " + innerLog.toString()});
        longToast(activity, log);
        write(log, file);
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
