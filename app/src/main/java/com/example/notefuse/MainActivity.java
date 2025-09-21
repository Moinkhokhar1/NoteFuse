package com.example.notefuse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;
    NoteAdapter adapter;
    DBHelper db;
    FloatingActionButton fab;

    private final Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = findViewById(R.id.recyclerViewNotes);
        fab = findViewById(R.id.fabAddNote);
        db = new DBHelper(this);

        rv.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddNoteActivity.class), 101);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();

        // Start live countdown handler
        runnable = new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.removeExpiredNotes(); 
                    adapter.notifyDataSetChanged(); 
                }
                handler.postDelayed(this, 1000); 
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    private void loadNotes() {
        List<Note> notes = db.getAllNotes();
        adapter = new NoteAdapter(notes, this, db);
        rv.setAdapter(adapter);
    }
}
