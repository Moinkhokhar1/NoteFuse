// NoteAdapter.java
package com.example.notefuse;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {
    List<Note> list;
    Context ctx;
    DBHelper db;

    public NoteAdapter(List<Note> list, Context ctx, DBHelper db) {
        this.list = list;
        this.ctx = ctx;
        this.db = db;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Note n = list.get(position);
        holder.title.setText(n.getTitle().isEmpty() ? "(No Title)" : n.getTitle());

        if(n.getExpiryAt() > 0){
            long diff = n.getExpiryAt() - System.currentTimeMillis();
            if(diff <= 0){
                holder.content.setText("Expired...");
            } else {
                long mins = TimeUnit.MILLISECONDS.toMinutes(diff);
                long secs = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
                holder.content.setText(n.getContent() + " — self destructs in " + mins + "m " + secs + "s");
            }
        } else {
            holder.content.setText(n.getContent());
        }

        holder.itemView.setOnLongClickListener(v -> {
            db.deleteNoteById(n.getId());
            cancelAlarm(n.getId());
            list.remove(position);
            notifyItemRemoved(position);
            return true;
        });
    }


    private void cancelAlarm(long noteId){
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctx, DeleteAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, (int)noteId, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if(am != null) am.cancel(pi);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public void removeExpiredNotes() {
        for (int i = list.size() - 1; i >= 0; i--) { // iterate backwards
            Note n = list.get(i);
            if (n.getExpiryAt() > 0 && n.getExpiryAt() <= System.currentTimeMillis()) {
                db.deleteNoteById(n.getId());
                cancelAlarm(n.getId());
                list.remove(i);
                notifyItemRemoved(i);
            }
        }
    }


    static class VH extends RecyclerView.ViewHolder {
        TextView title, content;
        VH(View v){ super(v);
            title = v.findViewById(android.R.id.text1);
            content = v.findViewById(android.R.id.text2);
        }
    }
}
