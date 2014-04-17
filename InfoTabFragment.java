package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.File;
import java.util.ArrayList;

public class InfoTabFragment extends Fragment {

    private static String dataDir;

    public InfoTabFragment() {
    }

    public static void postUpdates(final Activity activity) {

        File listFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator +
                activity.getString(R.string.hellscore_update_file));
        try {
            ArrayList<String> content = MyTools.catToList(listFile.toString());
            listFile.delete();
            ArrayList<String> StableNames = new ArrayList<String>();
            ArrayList<String> StableLinks = new ArrayList<String>();
            ArrayList<String> TestNames = new ArrayList<String>();
            ArrayList<String> TestLinks = new ArrayList<String>();

            for (String line : content) {
                if (line.contains("stable")) {
                    StableNames.add(activity.getString(R.string.stable) + ": " + (((line.split(";"))[1]).split(">>"))[0].trim());
                    StableLinks.add((((line.split(";"))[1]).split(">>"))[1].trim());
                } else {
                    TestNames.add(activity.getString(R.string.test) + ": " + (((line.split(";"))[1]).split(">>"))[0].trim());
                    TestLinks.add((((line.split(";"))[1]).split(">>"))[1].trim());
                }
            }

            final ArrayList<String> list = new ArrayList<String>();
            final ArrayList<String> links = new ArrayList<String>();
            if (TestNames.get(0).toLowerCase().contains("#")) {
                list.addAll(TestNames);
                list.addAll(StableNames);
                links.addAll(TestLinks);
                links.addAll(StableLinks);
            } else {
                list.addAll(StableNames);
                list.addAll(TestNames);
                links.addAll(StableLinks);
                links.addAll(TestLinks);
            }

            String[] items = new String[list.size()];
            items = list.toArray(items);

            final String[] finalItems = items;

            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setTitle(activity.getString(R.string.lastestVersions))
                    .setNeutralButton(activity.getString(R.string.button_dismiss), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(links.get(i))));
                            String filename = "";
                            try {
                                filename = (finalItems[i].split(":"))[1].replace("#", "").trim();
                            } catch (Exception ignored) {

                            }
                            downloadFile(activity, true, "mode2", links.get(i), filename);
                        }
                    })
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                new AlertDialog.Builder(activity)
                        .setCancelable(true)
                        .setTitle("Oops")
                        .setMessage(activity.getString(R.string.somethingWentWrong))
                        .setNeutralButton(activity.getString(R.string.button_dismiss), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            } catch (WindowManager.BadTokenException ee) {
                MyTools.toast(activity, R.string.somethingWentWrong);
            }
        }
    }

    public static void downloadFile(Activity activity, boolean force, String... args) {
        new fileDownloader(activity, force, args[0]).execute(args);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.info, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_diagnose:
                Intent intent = new Intent(getActivity(), Diagnose.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                final Dialog dialog = new Dialog(getActivity());
                dialog.setTitle(getString(R.string.title_about));
                dialog.setContentView(R.layout.about_dialog);
                TextView version = (TextView) dialog.findViewById(R.id.misc);
                try {
                    version.setText("v " + MainActivity.appVersion);
                } catch (Exception ignored) {
                }
                dialog.show();
                return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataDir = MyTools.getDataDir(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_tab, container, false);
        setHasOptionsMenu(true);
        TextView appVersion = (TextView) (view != null ? view.findViewById(R.id.title2) : null);
        assert appVersion != null;
        appVersion.setText(MainActivity.appVersion);
        assert view != null;
        TextView kernelVersion = (TextView) view.findViewById(R.id.textView3);
        kernelVersion.setText(MyTools.readFile("/proc/version"));
        final WebView browser = (WebView) view.findViewById(R.id.webView);
        browser.setVisibility(View.GONE);
        WebSettings webSettings = browser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        Button stable = (Button) view.findViewById(R.id.stable);
        Button test = (Button) view.findViewById(R.id.test);
        Button check = (Button) view.findViewById(R.id.check);
        stable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://d-h.st/users/hellsgod/?fld_id=28949";
                if (browser.getVisibility() == View.GONE) {
                    browser.loadUrl(url);
                    browser.setVisibility(View.VISIBLE);
                    browser.requestFocus();
                } else {
                    if (browser.getUrl().equals("http://d-h.st/users/hellsgod/?fld_id=28949")) {
                        browser.setVisibility(View.GONE);
                    } else {
                        browser.loadUrl(url);
                        browser.requestFocus();
                    }
                }
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://d-h.st/users/hellsgod/?fld_id=26855";
                if (browser.getVisibility() == View.GONE) {
                    browser.loadUrl(url);
                    browser.setVisibility(View.VISIBLE);
                    browser.requestFocus();
                } else {
                    if (browser.getUrl().equals("http://d-h.st/users/hellsgod/?fld_id=26855")) {
                        browser.setVisibility(View.GONE);
                    } else {
                        browser.loadUrl(url);
                        browser.requestFocus();
                    }
                }
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
                easyTracker.send(MapBuilder
                                .createEvent("Kernel_update_check",
                                        "button_press",
                                        "kernel_update",
                                        null)
                                .build()
                );
                downloadFile(getActivity(), false, "mode1");
            }
        });

        view.findViewById(R.id.donationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.showDonationDialog(getActivity());
            }
        });

        view.findViewById(R.id.kernelThread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.kernel_thread))));
            }
        });

        view.findViewById(R.id.appThread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_thread))));
            }
        });

        view.findViewById(R.id.gpCommunity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.googlePlus_community))));
            }
        });

        return view;
    }
}
