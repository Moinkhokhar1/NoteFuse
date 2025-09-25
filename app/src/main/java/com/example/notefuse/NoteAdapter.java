package com.example.notefuse;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {

    private final List<Note> list;
    private final Context context;
    private final DBHelper db;

    private final Set<Note> selectedNotes = new HashSet<>();
    private boolean selectionMode = false;
    
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    private OnSelectionChangedListener selectionListener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    public NoteAdapter(List<Note> list, Context context, DBHelper db) {
        this.list = list;
        this.context = context;
        this.db = db;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        bind(holder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains("timer")) {
            Note n = list.get(position);
            updateTimer(holder, n);
        } else {
            bind(holder, position);
        }
    }

    private void bind(VH holder, int position) {
        Note n = list.get(position);
        holder.title.setText(n.getTitle().isEmpty() ? "(No Title)" : n.getTitle());

        String content = n.getContent();
        holder.content.setText(content.length() > 50 ? content.substring(0, 50) + "..." : content);

        holder.itemView.setBackgroundColor(selectedNotes.contains(n) ? 0x9934B5E4 : 0x00000000);

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(n);
            } else {
                Intent intent = new Intent(context, ViewNoteActivity.class);
                intent.putExtra("note_id", n.getId());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!selectionMode) {
                selectionMode = true;
                toggleSelection(n);
            }
            return true;
        });

        updateTimer(holder, n);
    }

    private void updateTimer(VH holder, Note n) {
        long expiry = n.getExpiryAt();
        if (expiry > 0) {
            long diff = expiry - System.currentTimeMillis();
            long secs = diff / 1000;
            long mins = secs / 60;
            holder.timer.setText(diff > 0 ? "Self-destructs in " + mins + "m " + (secs % 60) + "s" : "Expired");
        } else {
            holder.timer.setText("No timer");
        }
    }

    private void toggleSelection(Note note) {
        if (selectedNotes.contains(note)) {
            selectedNotes.remove(note);
        } else {
            selectedNotes.add(note);
        }
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedNotes.size());
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedNotes.clear();
        selectionMode = false;
        notifyDataSetChanged();
    }

    public List<Note> getSelectedNotes() {
        return new ArrayList<>(selectedNotes);
    }

    public void removeNotes(List<Note> notesToRemove) {
        for (Note n : notesToRemove) {
            db.deleteNoteById(n.getId());
            cancelAlarm(n.getId());
            list.remove(n);
        }
        clearSelection();
        notifyDataSetChanged();
    }

    private void cancelAlarm(long noteId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, DeleteAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                (int) noteId,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (am != null) am.cancel(pi);
    }

    public void removeExpiredNotes() {
        boolean removed = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            Note n = list.get(i);
            if (n.getExpiryAt() > 0 && n.getExpiryAt() <= System.currentTimeMillis()) {
                db.deleteNoteById(n.getId());
                cancelAlarm(n.getId());
                list.remove(i);
                notifyItemRemoved(i);
                removed = true;
            }
        }
        if (!removed) {
            notifyItemRangeChanged(0, list.size(), "timer");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, content, timer;

        public VH(View v) {
            super(v);
            title = v.findViewById(R.id.textNoteTitle);
            content = v.findViewById(R.id.textNoteContent);
            timer = v.findViewById(R.id.textNoteTimer);
        }
    }
}
