package com.example.notefuse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView rv;
    NoteAdapter adapter;
    DBHelper db;
    FloatingActionButton fab;

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
    }

    private void loadNotes(){
        List<Note> notes = db.getAllNotes();
        adapter = new NoteAdapter(notes, this, db);
        rv.setAdapter(adapter);
    }
}
