package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.themike10452.hellscorekernelmanager.Blackbox.Blackbox;

public class BaseActivity extends Activity {

    private static BaseActivity instance;

    public BaseActivity() {
        instance = this;
    }

    public static BaseActivity getInstance() {
        if (instance != null)
            return instance;
        else
            return new BaseActivity();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        String kernel;
        try {
            kernel = MyTools.readFile("/proc/version");
        } catch (Exception ignored) {
            kernel = "n/a";
        }
        if (kernel.toLowerCase().contains("hells") || Blackbox.tool4(getApplicationContext())) {
            final SharedPreferences preferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
            boolean b = preferences.getBoolean("ShowRatingDisclaimer", true);
            if (b) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setPositiveButton("Take me to PlayStore", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=com.themike10452.hellscorekernelmanager"));
                        startActivity(intent);
                        preferences.edit().putBoolean("ShowRatingDisclaimer", false).commit();
                    }
                })
                        .setNeutralButton("My rating is fine", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                preferences.edit().putBoolean("ShowRatingDisclaimer", false).commit();
                                load();
                            }
                        })
                        .setCancelable(false)
                        .setTitle("PlayStore Ratings")
                        .setMessage("This is a direct message to all who gave a bad app rating long ago and forgot to update their ratings after updating and improving the app. Please go ahead and fix your ratings.\n\nThe same goes to all of you who gave bad ratings simply because you miss one small feature, this is a discouragement and unappreciation to my work and efforts to provide you this free app, so go ahead and rethink your ratings.\n\nThe App will be pulled from PS and back to XDA because of these unjustified bad ratings.\n\nNote: I am not the kernel developer, I just created the support app.")
                        .show();
            } else {
                load();
            }
        } else {
            findViewById(R.id.progressBar).setVisibility(TextView.GONE);
            findViewById(R.id.button).setVisibility(View.VISIBLE);
            TextView msg = (TextView) findViewById(R.id.messageView);
            msg.setTextColor(getResources().getColor(R.color.hellorange));
            msg.setText(getString(R.string.sorry_not_supported));
            final Button getHellscore = (Button) findViewById(R.id.button);
            final Activity activity = this;
            getHellscore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EasyTracker easyTracker = EasyTracker.getInstance(activity);
                    easyTracker.send(MapBuilder
                                    .createEvent("get_hellscore_kernel",
                                            "move_to_kernel",
                                            "get_hellscore_now",
                                            null)
                                    .build()
                    );
                    InfoTabFragment.downloadFile(activity, true, "mode1");
                }
            });
        }

    }

    private void load() {
        Intent i = new Intent(this, MainActivity.class);
        new ProgressTask(this, i).execute();
    }

}
