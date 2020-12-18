package com.tf.pomodorotimer;

import android.os.CountDownTimer;

public abstract class CountDownTimerPausable {
    private long millisInFuture;
    private long countDownInterval;
    private long millisRemaining;
    private long activityLength;

    public long getActivityLength(){return activityLength;}

    public long getMillisRemaining(){return millisRemaining;}

    private CountDownTimer countDownTimer = null;

    private boolean isPaused = true;

    public CountDownTimerPausable(long millisInFuture, long countDownInterval, long activityLength) {
        super();
        this.activityLength = activityLength;
        this.millisInFuture = millisInFuture;
        this.countDownInterval = countDownInterval;
        this.millisRemaining = this.millisInFuture;
    }
    private void createCountDownTimer(){
        countDownTimer = new CountDownTimer(millisRemaining,countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished;
                CountDownTimerPausable.this.onTick(millisUntilFinished);

            }

            @Override
            public void onFinish() {
                CountDownTimerPausable.this.onFinish();

            }
        };
    }

    public abstract void onTick(long millisUntilFinished);

    public abstract void onFinish();

    public final void cancel(){
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
        this.millisRemaining = 0;
    }

    public synchronized final CountDownTimerPausable start(){
        if(isPaused){
            createCountDownTimer();
            countDownTimer.start();
            isPaused = false;
        }
        return this;
    }

    public void pause()throws IllegalStateException{
        if(!isPaused){
            countDownTimer.cancel();
        } else{
            throw new IllegalStateException("CountDownTimerPausable is already in pause state, start counter before pausing it.");
        }
        isPaused = true;
    }
    public boolean isPaused() {
        return isPaused;
    }
}