package com.tf.pomodorotimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;


public class NotificationsHelper {
    private final String FG_CHANNEL_ID = "com.touchforce.pomodorotimer.pomodoro_fg_channel";
    private final String FG_CHANNEL_NAME = "Pomodoro timer foreground channel";
    private final String DEFAULT_CHANNEL_ID = "com.touchforce.pomodorotimer.pomodoro_default_channel";
    private final String DEFAULT_CHANNEL_NAME = "Pomodoro timer default channel";
    private final int NOTIFICATIONS_ICON = R.drawable.ic_timer;
    private NotificationManager mNotificationManager;
    private Context context;
    private Intent activityIntent;
    private PendingIntent notificationActivityIntent;
    public NotificationsHelper(Context context, NotificationManager notificationManager){
        this.context = context;
        this.activityIntent = new Intent(context, MainActivity.class);
        notificationActivityIntent = PendingIntent
                .getActivity(context, 0, activityIntent, 0);
        mNotificationManager = notificationManager;
        createForegroundNotificationChannel();
        createDefaultNotificationChannel();
    }

    public Notification.Builder createWorkNotificationBuilder(){
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(NOTIFICATIONS_ICON)
                .setContentIntent(notificationActivityIntent)
                .setSound(null)
                .setContentTitle(context.getString(R.string.work_time_notification_title));
        if(isO()){
            builder.setChannelId(FG_CHANNEL_ID);
        }
        return builder;
    }

    public Notification.Builder createRestNotificationBuilder(){
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(NOTIFICATIONS_ICON)
                .setContentIntent(notificationActivityIntent)
                .setSound(null)
                .setContentTitle(context.getString(R.string.rest_time_notification_title));
        if(isO()){
            builder.setChannelId(FG_CHANNEL_ID);
        }
        return builder;
    }

    public void sendStopWorkNotification(){
        Notification.Builder builder = getStopActivityNotificationBuilder(R.string.stop_work);
        Notification notification = builder.build();
        mNotificationManager.notify(0, notification);
    }

    public void sendStopRestNotification(){
        Notification.Builder builder = getStopActivityNotificationBuilder(R.string.stop_rest);
        Notification notification = builder.build();
        mNotificationManager.notify(0, notification);
    }

    private void createForegroundNotificationChannel() {
        if (isO()) {
            int importance = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel channel = new NotificationChannel(FG_CHANNEL_ID, FG_CHANNEL_NAME, importance);
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);
        }
    }
    private void createDefaultNotificationChannel() {
        if (isO()) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, importance);
//            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private Notification.Builder getStopActivityNotificationBuilder(int text) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(context.getString(R.string.change_of_activity));
        builder.setContentText(context.getString(text));
        builder.setContentIntent(notificationActivityIntent);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setSmallIcon(NOTIFICATIONS_ICON);
        if(isO()){
            builder.setChannelId(DEFAULT_CHANNEL_ID);
        }
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        return builder;
    }

    private boolean isO(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
