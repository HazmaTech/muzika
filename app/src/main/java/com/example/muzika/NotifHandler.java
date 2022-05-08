package com.example.muzika;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotifHandler {
    private static final String ID = "Muzika";
    private final int NOTIF_ID = 10;
    private Context context;
    private NotificationManager manager;
    public NotifHandler(Context context){
        this.context = context;
        this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        newChannel();
    }
    public void send(String message){
        Intent intent = new Intent(context, Feed.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIF_ID, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(context, ID)
                .setContentTitle("Muzika needs you")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_DEFAULT);
        Log.i("notif", "Sending new notificationrn");
        manager.notify(NOTIF_ID, builder.build());
    }

    private void newChannel(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(ID, "Muzika Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            channel.setDescription("Muzika Notifications, brought to you by Muzika"); //And on this episode, we'll see just how convinient it is to sometimes copy and paste the perfectly working solutions ;)
            manager.createNotificationChannel(channel);
        }
    }
    public void cancel() {
        manager.cancel(NOTIF_ID);
    }
}
