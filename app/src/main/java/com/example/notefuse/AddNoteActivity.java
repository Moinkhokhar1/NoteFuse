package com.example.notefuse;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.Calendar;

public class AddNoteActivity extends  AppCompatActivity {
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

        String[] timers = {"No Timer", "1 minute", "5 minutes", "30 minutes", "1 hour", "6 hours"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimer.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveNote());
    }

    @SuppressLint("ScheduleExactAlarm")
    private void saveNote(){
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        if(title.isEmpty() && content.isEmpty()){
            Toast.makeText(this, "Write something!", Toast.LENGTH_SHORT).show();
            return;
        }
        long created = System.currentTimeMillis();
        long expiry = 0;
        int pos = spinnerTimer.getSelectedItemPosition();
        switch(pos){
            case 1: expiry = created + 60 * 1000L; break;
            case 2: expiry = created + 5 * 60 * 1000L; break;
            case 3: expiry = created + 30 * 60 * 1000L; break;
            case 4: expiry = created + 60 * 60 * 1000L; break;
            case 5: expiry = created + 24 * 60 * 60 * 1000L; break;
            case 6: expiry = created + 3 * 24 * 60 * 60 * 1000L; break;
            case 7: expiry = created + 5 * 24 * 60 * 60 * 1000L; break;
            case 8: expiry = created + 10 * 24 * 60 * 60 * 1000L; break;
            case 9: expiry = created + 15 * 24 * 60 * 60 * 1000L; break;
            case 10: expiry = created + 30 * 24 * 60 * 60 * 1000L; break;
            default: expiry = 0; break;
        }

        long id = db.insertNote(title, content, created, expiry);

        if(expiry > 0){
            scheduleDeleteAlarm(id, expiry);
        }

        setResult(RESULT_OK);
        finish();
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void scheduleDeleteAlarm(long noteId, long expiryAt){
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, DeleteAlarmReceiver.class);
        i.putExtra(DeleteAlarmReceiver.EXTRA_NOTE_ID, noteId);
        PendingIntent pi = PendingIntent.getBroadcast(this, (int)noteId, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, expiryAt, pi);
        }
    }
}
