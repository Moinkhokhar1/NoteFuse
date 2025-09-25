//package com.example.notefuse;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import java.util.List;
//
//public class MainActivity extends AppCompatActivity {
//
//    RecyclerView rv;
//    NoteAdapter adapter;
//    DBHelper db;
//    FloatingActionButton fab;
//
//    private final Handler handler = new Handler();
//    private Runnable runnable;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        rv = findViewById(R.id.recyclerViewNotes);
//        fab = findViewById(R.id.fabAddNote);
//        db = new DBHelper(this);
//
//        rv.setLayoutManager(new LinearLayoutManager(this));
//
//        fab.setOnClickListener(v -> {
//            startActivityForResult(new Intent(this, AddNoteActivity.class), 101);
//        });
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        loadNotes();
//
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                if (adapter != null) {
//                    adapter.removeExpiredNotes();
//                    adapter.notifyDataSetChanged();
//                }
//                handler.postDelayed(this, 1000); // repeat every 1 second
//            }
//        };
//        handler.post(runnable);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        handler.removeCallbacks(runnable);
//    }
//
//    private void loadNotes() {
//        List<Note> notes = db.getAllNotes();
//        adapter = new NoteAdapter(notes, this, db);
//        rv.setAdapter(adapter);
//    }
//}
package com.example.notefuse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;
    NoteAdapter adapter;
    DBHelper db;
    FloatingActionButton fab;
    private final Handler handler = new Handler();
    private Runnable runnable;

    private ActionMode actionMode;

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

        adapter.setOnSelectionChangedListener(count -> {
            if (count > 0) {
                if (actionMode == null) {
                    actionMode = startActionMode(actionModeCallback);
                }
                actionMode.setTitle(count + " selected");
            } else if (actionMode != null) {
                actionMode.finish();
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.removeExpiredNotes();
                    handler.postDelayed(this, 1000);
                }
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

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.delete_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_delete) {
                List<Note> selected = adapter.getSelectedNotes();
                adapter.removeNotes(selected);
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            if (adapter != null) {
                adapter.clearSelection();
            }
        }
    };
}
