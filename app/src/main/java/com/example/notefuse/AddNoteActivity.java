package com.example.notefuse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;

import java.util.List;

public class AddNoteActivity extends AppCompatActivity {
    EditText etTitle, etContent;
    Spinner spinnerTimer;
    Button btnSave;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        etTitle = findViewById(R.id.editNoteTitle);
        etContent = findViewById(R.id.editNoteContent);
        spinnerTimer = findViewById(R.id.spinnerTimer);
        btnSave = findViewById(R.id.btnSaveNote);
        db = new DBHelper(this);

        String[] timers = {"No Timer", "1 minute", "5 minutes", "30 minutes", "1 hour", "6 hours",
                "1 day", "3 day", "5 day", "10 day", "15 day", "30 day"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimer.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveNote());

        // Create notification channel
        createNotificationChannel();

//        destroyNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "notefuse_channel",
                    "NoteFuse Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for saved and destroyed notes");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Write something!", Toast.LENGTH_SHORT).show();
            return;
        }

        long created = System.currentTimeMillis();
        long expiry = calculateExpiry(created, spinnerTimer.getSelectedItemPosition());

        long noteId = db.insertNote(title, content, created, expiry);
        
        showNotification("Note Saved", "Your note has been saved successfully.", noteId);

        Intent intent = new Intent(AddNoteActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private long calculateExpiry(long createdTime, int spinnerPos) {
        long minute = 60 * 1000L;
        long hour = 60 * minute;
        long day = 24 * hour;

        switch (spinnerPos) {
            case 1: return createdTime + 1 * minute;
            case 2: return createdTime + 5 * minute;
            case 3: return createdTime + 30 * minute;
            case 4: return createdTime + 1 * hour;
            case 5: return createdTime + 6 * hour;
            case 6: return createdTime + 24 * hour;
            case 7: return createdTime + 3 * day;
            case 8: return createdTime + 5 * day;
            case 9: return createdTime + 10 * day;
            case 10: return createdTime + 15 * day;
            case 11: return createdTime + 30 * day;
            default: return 0; // No expiry
        }
    }
//    private void destroyExpiredNotes() {
//        List<Note> allNotes = db.getAllNotes();
//        for (Note note : allNotes) {
//            long diff = note.getExpiryAt() - System.currentTimeMillis();
//            if (diff <= 0) {
//                db.deleteNoteById(note.getId());
//                showNotification("Note Destroyed",
//                        "Your note \"" + note.getTitle() + "\" has self-destructed.",
//                        note.getId());
//            }
//        }
//    }

    private void showNotification(String title, String message, long noteId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notefuse_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) noteId, builder.build());
        }
    }
}
