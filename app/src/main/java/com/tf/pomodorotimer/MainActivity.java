package com.tf.pomodorotimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TimerService.TimerUpdatesListener {
    private ImageButton playButton;
    private ImageButton stopButton;
    private ImageButton pauseButton;
    private ImageButton skipButton;
    private ProgressBar mProgressBar;
    private TextView typeOfActivityTextView;
    private AppCompatTextView timeLeftTextView;
    private TimerService mTimerService;
    private boolean mBound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playButton = findViewById(R.id.play_timer_image_button);
        pauseButton = findViewById(R.id.pause_timer_image_button);
        stopButton = findViewById(R.id.stop_timer_image_button);
        skipButton = findViewById(R.id.skip_activity_image_button);
        typeOfActivityTextView = findViewById(R.id.type_of_activity_text_view);
        playButton.setOnClickListener(playButtonClickListener);
        pauseButton.setOnClickListener(pauseButtonClickListener);
        stopButton.setOnClickListener(stopButtonClickListener);
        skipButton.setOnClickListener(skipButtonClickListener);
        timeLeftTextView = findViewById(R.id.time_left_text_view);
        mProgressBar = findViewById(R.id.ProgressBar);
        if(!mBound){
            resetTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.about_options_menu_button:
                Intent aboutActivity = new Intent(this, AboutActivity.class);
                startActivity(aboutActivity);
                break;
            case R.id.settings_options_menu_button:
                Intent settingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivity);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetTimer(){
        timeLeftTextView.setText(DateFormatter.getDate(TimerService.workLength*1000));
        typeOfActivityTextView.setText("");
        mProgressBar.setProgress(100);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private View.OnClickListener playButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mBound){
                mTimerService.start();
            }
        }
    };
    private View.OnClickListener pauseButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mBound){
                mTimerService.pause();
                typeOfActivityTextView.setText(R.string.activity_type_pause);
            }
        }
    };
    private View.OnClickListener stopButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mBound){
                mTimerService.stop();
                resetTimer();
            }
        }
    };

    private View.OnClickListener skipButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mBound){
                mTimerService.skip();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            TimerService.TimerServiceBinder binder = (TimerService.TimerServiceBinder) service;
            mTimerService = binder.getService();
            mBound = true;
            mTimerService.setTimerUpdatesListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onTimerUpdated(long millisUntilFinished, long activityLength) {
        if(millisUntilFinished<=0){
            mProgressBar.setProgress(0);
        }
        timeLeftTextView.setText(DateFormatter.getDate(millisUntilFinished));
        mProgressBar.setProgress(Math.round(100*millisUntilFinished/(activityLength*1000)));
        if(mTimerService.isUserActivityWork()){
            typeOfActivityTextView.setText(R.string.work_time_notification_title);
        }else{
            typeOfActivityTextView.setText(R.string.rest_time_notification_title);
        }
    }

    static class DateFormatter{
        static String getDate(long time){
            Date date = new Date(time);
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.US);
            return dateFormat.format(date);
        }
    }
}
