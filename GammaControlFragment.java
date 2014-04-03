package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.File;
import java.util.ArrayList;

public class GammaControlFragment extends Fragment {

    public static final String setOnBootFileName = "90hellscore_color_settings";
    private final ArrayList<String> toRemove = new ArrayList<String>();
    private String redCal;
    private String greenCal;
    private String blueCal;
    private String redTemp;
    private String greenTemp;
    private String blueTemp;
    private int MODE;
    private TextView outMsg;
    private EditText redTempField;
    private EditText greenTempField;
    private EditText blueTempField;
    private EditText redCalField;
    private EditText greenCalField;
    private EditText blueCalField;
    private CheckBox setOnBoot;
    private File setOnBootAgent, setOnBootFile, scriptsDir;
    private ArrayList<String> profiles;
    private String toLoad = "~CUSTOM~";

    private View view;

    public GammaControlFragment() {

    }

    private static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), 0);
        } catch (Exception ignored) {
        }
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
                if (MODE != 1) {
                    Intent i1 = new Intent(getActivity(), BackgroudService.class);
                    getActivity().stopService(i1);
                    getActivity().startService(i1);
                }
                if (setOnBoot.isChecked())
                    prepareBootScript();
                else
                    removeBootFile();
                return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        RelativeLayout r = (RelativeLayout) view.findViewById(R.id.topBar);
        r.requestFocus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gamma_control, container, false);

        MODE = (new File(this.getString(R.string.kgamma_blue)).exists()) ? 1 : 2;

        if (MODE != 1) {
            (view.findViewById(R.id.gamma)).setVisibility(View.GONE);
            (view.findViewById(R.id.topBar)).setVisibility(View.GONE);
        }
        view.clearFocus();
        setHasOptionsMenu(true);
        profiles = MySQLiteAdapter.getAllProfiles(getActivity());

        showOutMsg(-1);

        String dataDir = MyTools.getDataDir(getActivity());
        scriptsDir = new File(dataDir + File.separator + "scripts");
        setOnBootFile = new File(scriptsDir + File.separator
                + setOnBootFileName);
        setOnBootAgent = new File(this.getString(R.string.setOnBootAgentFile));

        refreshAll();

        Button loadBtn = (Button) view.findViewById(R.id.loadBtn);
        loadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                String[] items = new String[profiles.size()];
                for (int i = 0; i < items.length; i++)
                    items[i] = profiles.get(i);
                builder.setSingleChoiceItems(items, profiles.indexOf(toLoad),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                toLoad = profiles.get(which);
                            }
                        }
                );

                builder.setPositiveButton("Load",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (profiles.indexOf(toLoad) > 0) {
                                    String[] values = MySQLiteAdapter.select(
                                            getActivity(),
                                            DBHelper.COLOR_PROFILES_TABLE,
                                            DBHelper.COLOR_PROFILES_TABLE_KEY,
                                            toLoad, new String[]{"red",
                                                    "green", "blue", "cal"}
                                    );
                                    redCalField.setText(values[0]);
                                    greenCalField.setText(values[1]);
                                    blueCalField.setText(values[2]);
                                    redTempField.setText(values[3].split(" ")[0]);
                                    greenTempField.setText(values[3].split(" ")[1]);
                                    blueTempField.setText(values[3].split(" ")[2]);
                                }

                                boolean b = (profiles.indexOf(toLoad) == 0);

                                lock(b);
                            }
                        }
                );

                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                builder.create().dismiss();
                            }
                        }
                );

                builder.show();
            }
        });

        Button addBtn = (Button) view.findViewById(R.id.addBtn);
        if (MODE != 1)
            addBtn.setText("  " + getString(R.string.button_importColors));
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(getActivity());
                d.setCanceledOnTouchOutside(true);
                d.setTitle(getString(R.string.title_profileName));
                d.setContentView(R.layout.add_profile__layout);
                d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        RelativeLayout r = (RelativeLayout) view
                                .findViewById(R.id.color_profiles);
                        r.requestFocus();
                    }
                });
                final EditText et = (EditText) d.findViewById(R.id.field);
                et.setMaxLines(1);
                final Button add = (Button) d.findViewById(R.id.addBtn0);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        et.setText(et.getText().toString().trim());
                        String[] values = {
                                et.getText().toString(),
                                redCalField.getText().toString(),
                                greenCalField.getText().toString(),
                                blueCalField.getText().toString(),
                                redTempField.getText().toString() + " "
                                        + greenTempField.getText().toString()
                                        + " "
                                        + blueTempField.getText().toString()
                                        + " "};
                        if (et.getText().toString().trim().length() != 0) {
                            if (MySQLiteAdapter.insert(getActivity(),
                                    DBHelper.COLOR_PROFILES_TABLE, values)) {
                                d.dismiss();
                                view.clearFocus();
                                MyTools.toast(getActivity(),
                                        getString(R.string.toast_colorProfileAdded));
                                profiles = MySQLiteAdapter
                                        .getAllProfiles(getActivity());
                                String[] items = new String[profiles.size()];
                                for (int i = 0; i < items.length; i++)
                                    items[i] = profiles.get(i);
                                RelativeLayout r = (RelativeLayout) view
                                        .findViewById(R.id.color_profiles);
                                r.requestFocus();
                            } else {
                                et.getEditableText().clear();
                                MyTools.longToast(getActivity(),
                                        getString(R.string.toast_colorProfile_alreadyExists));
                            }
                        } else {
                            et.getEditableText().clear();
                            MyTools.toast(getActivity(), getString(R.string.toast_colorProfile_invalidName));
                        }
                    }
                });

                d.show();
            }
        });

        Button removeBtn = (Button) view.findViewById(R.id.removeBtn);
        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRemove.clear();
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                builder.setTitle(getString(R.string.title_selectProfile_toRemove));
                String[] items = new String[profiles.size()];
                for (int i = 0; i < items.length; i++)
                    items[i] = profiles.get(i);
                builder.setMultiChoiceItems(items, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                if (isChecked)
                                    toRemove.add(profiles.get(which));
                                else
                                    toRemove.remove(toRemove.indexOf(profiles
                                            .get(which)));
                            }
                        }
                );

                builder.setPositiveButton(getString(R.string.button_removeSelected),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                for (String value : toRemove)
                                    MySQLiteAdapter.delete(getActivity(),
                                            DBHelper.COLOR_PROFILES_TABLE,
                                            DBHelper.COLOR_PROFILES_TABLE_KEY,
                                            value);

                                profiles = MySQLiteAdapter
                                        .getAllProfiles(getActivity());

                                if (toRemove.indexOf(toLoad) >= 0) {
                                    toLoad = "~" + getString(R.string.custom).toUpperCase() + "~";
                                    lock(true);
                                }
                            }
                        }
                );

                builder.setNegativeButton(getString(R.string.button_cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        }
                );

                builder.show();
            }
        });

        Button resetBtn = (Button) view.findViewById(R.id.resetBtn);
        resetBtn.setLongClickable(true);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTools.toast(getActivity(), getString(R.string.toast_longPress_toUse));
            }
        });

        resetBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.resetProfiles_confirmation))
                        .setTitle("<!>")
                        .setPositiveButton(getString(R.string.button_yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (MySQLiteAdapter.clearTable(getActivity(),
                                                DBHelper.COLOR_PROFILES_TABLE)) {
                                            MySQLiteAdapter
                                                    .createProfiles(getActivity());
                                            MyTools.toast(getActivity(),
                                                    getString(R.string.toast_done));
                                            profiles = MySQLiteAdapter
                                                    .getAllProfiles(getActivity());
                                        }

                                        builder.create().dismiss();
                                    }
                                }
                        )
                        .setNegativeButton(getString(R.string.button_no),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        builder.create().cancel();
                                    }
                                }
                        ).show();
                return true;
            }
        });

        return view;
    }

    private void saveAll() {

        if (!toLoad.equalsIgnoreCase("~CUSTOM~")) {
            EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
            easyTracker.send(MapBuilder
                            .createEvent("Color_Profile",
                                    "selected_color_profile",
                                    toLoad,
                                    null)
                            .build()
            );
        }

        MySQLiteAdapter.insertOrUpdate(getActivity(), DBHelper.SETTINGS_TABLE,
                new String[]{DBHelper.SETTINGS_TABLE_KEY,
                        DBHelper.SETTINGS_TABLE_COLUMN1,
                        DBHelper.SETTINGS_TABLE_COLUMN2},
                new String[]{this.getString(R.string.LOADED_COLOR_PROFILE),
                        toLoad, "null"}
        );

        hideSoftKeyboard(getActivity());
        getValues();
        MyTools.write(redCal, this.getString(R.string.kgamma_r));
        MyTools.write(greenCal, this.getString(R.string.kgamma_g));
        MyTools.write(blueCal, this.getString(R.string.kgamma_b));

        try {
            redTemp = redTempField.getText().toString().trim();
            blueTemp = blueTempField.getText().toString().trim();
            greenTemp = greenTempField.getText().toString().trim();

            if (Integer.parseInt(redTemp) > 255)
                redTemp = "255";
            if (Integer.parseInt(greenTemp) > 255)
                greenTemp = "255";
            if (Integer.parseInt(blueTemp) > 255)
                blueTemp = "255";
        } catch (Exception ingnored) {
        }

        String comTemp = redTemp + " " + greenTemp + " " + blueTemp;
        MyTools.write(comTemp, this.getString(R.string.kcal));

        if (MODE == 2)
            MyTools.write(1,
                    this.getString(R.string.kgamma_b).replace("_b", "_apply"));
        showOutMsg(0);
    }

    String cat(String file) {
        try {
            return MyTools.readFile(file);
        } catch (Exception e) {
            return "n/a";
        }
    }

    private void showOutMsg(int i) {
        outMsg = (TextView) view.findViewById(R.id.outMsg);
        if (i == 1)
            outMsg.setText(R.string.unsupported_gamma);
        else if (i == 0) {
            showOutMsg(-1);
            Toast.makeText(getActivity(), R.string.toast_screenOff_toApply,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshAll() {

        profiles = MySQLiteAdapter.getAllProfiles(getActivity());

        setOnBoot = (CheckBox) view.findViewById(R.id.setOnBoot);

        redTempField = (EditText) view.findViewById(R.id.redTempField);
        greenTempField = (EditText) view.findViewById(R.id.greenTempField);
        blueTempField = (EditText) view.findViewById(R.id.blueTempField);

        redCalField = (EditText) view.findViewById(R.id.redCalField);
        greenCalField = (EditText) view.findViewById(R.id.greenCalField);
        blueCalField = (EditText) view.findViewById(R.id.blueCalField);

        String[] Temp = cat(this.getString(R.string.kcal)).split(" ");

        if (!Temp[0].equals("n/a")) {
            redTempField.setText(Temp[0]);
            greenTempField.setText(Temp[1]);
            blueTempField.setText(Temp[2]);
        }
        switch (MODE) {
            case 1:
                redCalField.setText(cat(this.getString(R.string.kgamma_red)));
                greenCalField.setText(cat(this.getString(R.string.kgamma_green)));
                blueCalField.setText(cat(this.getString(R.string.kgamma_blue)));
                break;
            case 2:
                // redCalField.setText(cat(this.getString(R.string.kgamma_r)));
                // greenCalField.setText(cat(this.getString(R.string.kgamma_g)));
                // blueCalField.setText(cat(this.getString(R.string.kgamma_b)));
                break;
        }

        String loaded;
        try {
            loaded = (MySQLiteAdapter.select(getActivity(),
                    DBHelper.SETTINGS_TABLE, DBHelper.SETTINGS_TABLE_KEY,
                    this.getString(R.string.LOADED_COLOR_PROFILE),
                    new String[]{DBHelper.SETTINGS_TABLE_COLUMN1}))[0];
        } catch (IndexOutOfBoundsException e) {
            loaded = profiles.get(0);
        }
        switch (MODE) {
            case 1:
                if (!(profiles.indexOf(loaded) == 0)) {
                    String[] correctValues = MySQLiteAdapter.select(getActivity(),
                            DBHelper.COLOR_PROFILES_TABLE,
                            DBHelper.COLOR_PROFILES_TABLE_KEY, loaded,
                            new String[]{"red", "green", "blue"});

                    boolean a = false, b = false, c = false;

                    if (correctValues.length == 3) {
                        a = (redCalField.getText().toString()
                                .equals(correctValues[0]));
                        b = (greenCalField.getText().toString()
                                .equals(correctValues[1]));
                        c = (blueCalField.getText().toString()
                                .equals(correctValues[2]));
                    }

                    if (a && b && c) {
                        toLoad = loaded;
                        lock(false);
                    } else {
                        toLoad = profiles.get(0);
                        lock(true);
                    }
                } else {
                    lock(true);
                }
                break;

            default:
                toLoad = loaded;
        }

        setOnBoot.setChecked(
                setOnBootFile.exists() && setOnBootAgent.exists() &&
                        !setOnBootFile.isDirectory() && !setOnBootAgent.isDirectory()
        );
    }

    private void getValues() {
        redCal = redCalField.getText().toString();
        greenCal = greenCalField.getText().toString();
        blueCal = blueCalField.getText().toString();

        redTemp = redTempField.getText().toString();
        greenTemp = greenTempField.getText().toString();
        blueTemp = blueTempField.getText().toString();
    }

    private void prepareBootScript() {
        try {
            if (MODE == 2) return;

            getValues();
            if (!scriptsDir.exists())
                scriptsDir.mkdirs();

            String comTemp = redTemp + " " + greenTemp + " " + blueTemp;

            if (setOnBootFile.exists() && !setOnBootFile.isDirectory())
                setOnBootFile.delete();

            setOnBootFile.createNewFile();


            String[] values = {
                    redCal, greenCal, blueCal, comTemp
            };

            String[] destinations = {
                    this.getString(R.string.kgamma_r),
                    this.getString(R.string.kgamma_g),
                    this.getString(R.string.kgamma_b),
                    this.getString(R.string.kcal)
            };

            MyTools.fillScript(setOnBootFile, values, destinations, "");
            MyTools.createBootAgent(getActivity(), scriptsDir);

        } catch (Exception e) {
            File innerLog = new File(MyTools.getDataDir(getActivity()) + File.separator + "inner_log.log");
            MyTools.log(getActivity(), e.toString(), innerLog.toString());
            setOnBootFailed();
        }

    }

    private void lock(boolean b) {
        if (!b)
            outMsg.setText(toLoad + " (" + getString(R.string.TextView_locked) + ")");
        else
            outMsg.setText(getString(R.string.custom));

        redCalField.setFocusableInTouchMode(b);
        greenCalField.setFocusableInTouchMode(b);
        blueCalField.setFocusableInTouchMode(b);
        redTempField.setFocusableInTouchMode(b);
        greenTempField.setFocusableInTouchMode(b);
        blueTempField.setFocusableInTouchMode(b);

        view.clearFocus();
    }

    private void setOnBootFailed() {
        final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setMessage(getString(R.string.toast_failed_setOnBoot));
        b.setCancelable(false);
        b.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
