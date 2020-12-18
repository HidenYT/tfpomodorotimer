package com.tf.pomodorotimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;


public class TimerService extends Service {
    private NotificationManager mNotificationManager;
    private PowerManager.WakeLock mWakeLock;
    private final String WAKELOCK_TAG = "TFPMDRTMR::WakeLockKeepCPUOn";
    private final int permanentNotificationID = 8949468;
    private final int shortBreakDefaultLength = 5;
    private final int longBreakDefaultLength = 15;
    public static final int workLength = 25*60;
    private int restLength = 5*60;

    private int longBreakLength = 5*60;
    public static final int updateFrequency = 1000;
    private final String TAG = "TAG";
    private CountDownTimerPausable currentTimer;
    private IBinder mIBinder = new TimerServiceBinder();
    private NotificationsHelper mNotificationsHelper;
    private Notification.Builder mRestNotificationBuilder;
    private Notification.Builder mWorkNotificationBuilder;
    private TimerUpdatesListener mTimerUpdatesListener;
    private SharedPreferences mSharedPreferences;
    private int startedTimes = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    class TimerServiceBinder extends Binder {
        TimerService getService(TimerUpdatesListener listener) {
            mTimerUpdatesListener = listener;
            return TimerService.this;
        }
    }

    private class WorkTimer extends CountDownTimerPausable{
        WorkTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval, workLength);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if(mTimerUpdatesListener!=null){
                mTimerUpdatesListener.onTimerUpdated(millisUntilFinished, workLength);
            }
            mWorkNotificationBuilder.setContentText(MainActivity.DateFormatter.getDate(millisUntilFinished));
            mNotificationManager.notify(permanentNotificationID, mWorkNotificationBuilder.build());
        }

        @Override
        public void onFinish() {
            mNotificationsHelper.sendStopWorkNotification();
            onWorkFinished();
        }
    }

    private class RestTimer extends CountDownTimerPausable{
        RestTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval, restLength);

        }

        @Override
        public void onTick(long millisUntilFinished) {
            if(mTimerUpdatesListener!=null){
                mTimerUpdatesListener.onTimerUpdated(millisUntilFinished, restLength);
            }
            mRestNotificationBuilder.setContentText(MainActivity.DateFormatter.getDate(millisUntilFinished));
            mNotificationManager.notify(permanentNotificationID,mRestNotificationBuilder.build());
        }

        @Override
        public void onFinish() {
            mNotificationsHelper.sendStopRestNotification();
            currentTimer = createWorkTimer();
            currentTimer.start();
        }
    }

    private WorkTimer createWorkTimer(){
        return new WorkTimer(workLength*1000, updateFrequency);
    }

    private RestTimer createRestTimer(long length){
        return new RestTimer(length*1000, updateFrequency);
    }

    public void start(){
        if(currentTimer==null){
            currentTimer = createWorkTimer();
            notifyListenerFirstTime();
            startForeground(permanentNotificationID, mWorkNotificationBuilder.build());
        }
        currentTimer.start();
    }

    public void pause(){
        if(currentTimer!=null){
            try {
                currentTimer.pause();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        if(currentTimer!=null){
            currentTimer.cancel();
            currentTimer = null;
            stopForeground(true);
        }
    }

    public void skip(){
        if(currentTimer!=null){
            if(isUserActivityWork()){
                currentTimer.cancel();
                onWorkFinished();
                notifyListenerFirstTime();
            }else{
                currentTimer.cancel();
                currentTimer = createWorkTimer();
                currentTimer.start();
                notifyListenerFirstTime();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        restLength = 60 * mSharedPreferences
                .getInt(getString(R.string.preference_short_break_length_key), shortBreakDefaultLength);
        longBreakLength = 60* mSharedPreferences
                .getInt(getString(R.string.preference_long_break_length_key), longBreakDefaultLength);
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotificationsHelper = new NotificationsHelper(this, mNotificationManager);
        mRestNotificationBuilder = mNotificationsHelper.createRestNotificationBuilder();
        mWorkNotificationBuilder = mNotificationsHelper.createWorkNotificationBuilder();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) {
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
            mWakeLock.acquire();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mWakeLock!=null)mWakeLock.release();
    }

    public boolean isUserActivityWork(){
        return currentTimer instanceof WorkTimer;
    }

    public interface TimerUpdatesListener{
        void onTimerUpdated(long millisUntilFinished, long activityLength);
    }

    private void onWorkFinished(){
        startedTimes+=1;
        if(startedTimes == 5){
            startedTimes = 0;
            currentTimer = createRestTimer(longBreakLength);
        }else{
            currentTimer = createRestTimer(restLength);
        }
        currentTimer.start();
    }

    private void notifyListenerFirstTime(){
        if(mTimerUpdatesListener!=null){
            mTimerUpdatesListener.onTimerUpdated(currentTimer.getMillisRemaining(),
                    currentTimer.getActivityLength());
        }
    }
}
