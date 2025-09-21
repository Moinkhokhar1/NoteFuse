package com.example.notefuse;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class DeleteAlarmReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTE_ID = "note_id";
    private static final String CHANNEL_ID = "notefuse_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        long noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1);
        if(noteId == -1) return;

        DBHelper db = new DBHelper(context);
        db.deleteNoteById(noteId);

        createNotificationChannel(context);

        Intent openApp = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, (int)noteId, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("NoteFuse — Note destroyed")
                .setContentText("A note has self-destructed and been deleted.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int)noteId, nb.build());
    }

    private void createNotificationChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "NoteFuse Alerts";
            String desc = "Notifications for destroyed notes";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(desc);
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
}
