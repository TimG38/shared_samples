package com.example.foretickornot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Timer;
import java.util.TimerTask;

public class MyServiceTicker extends Service {
    private final IBinder binder = new LocalBinder();
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    private static final String TAG = "MyServiceTicker";
    private static final int ONGOING_NOTIFICATION_ID = 111;
    private static final String CHANNEL_ID = "Channel 9";
    private Timer timer = null;
    private int count = 0;

    public MyServiceTicker() {
        Log.i(TAG, "MyServiceTicker");
    }

    public class LocalBinder extends Binder {
        MyServiceTicker getService() {
            return MyServiceTicker.this;
        }
    }

    public void destroyMe() {
        Log.i(TAG, "destroyMe");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        buildNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        startTimer();
        return START_NOT_STICKY;
    }

    // not guaranteed, but can couple with android:stopWithTask="true"
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        destroyMe();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return binder;
    }

    private void buildNotification() {
        Intent activityIntent = new Intent(MyServiceTicker.this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, activityIntent, 0);

        CharSequence name = getString(R.string.app_name);
        String description = getString(R.string.app_name);
        notificationManager = NotificationManagerCompat.from(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.enableLights(false);
            notificationManager.createNotificationChannel(channel);
        }

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle(name)
                .setContentText("...")
                .setContentIntent(pendingIntent)
                .setNotificationSilent()
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notification = notificationBuilder.build();
    }

    private void startTimer() {
        if (timer == null) {
            Log.i(TAG, "startTimer");
            count = 0;

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    count++;
                    notificationBuilder.setContentText(""+count);
                    notificationManager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build());
                    Log.i(TAG, ""+count);
                }
            };

            timer = new Timer();
            timer.scheduleAtFixedRate(timerTask, 0, 1000);
        }
    }
}
