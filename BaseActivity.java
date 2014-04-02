package com.themike10452.hellscorekernelmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class BaseActivity extends Activity {

    private static BaseActivity instance;

    public BaseActivity() {
        instance = this;
    }

    public static BaseActivity getInstance() {
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);


        String kernel = MyTools.readFile("/proc/version");
        if (kernel.toLowerCase().contains("hells")) {
            Intent i = new Intent(this, MainActivity.class);
            new ProgressTask(this, i).execute();
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
                    InfoTabFragment.downloadFile(activity, true);
                }
            });
        }

    }

}
