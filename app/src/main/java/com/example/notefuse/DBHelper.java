package com.example.notefuse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "notefuse.db";
    private static final int DB_VER = 1;

    public static final String TABLE = "notes";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_CREATED = "created_at";
    public static final String COL_EXPIRY = "expiry_at";

    public DBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_CONTENT + " TEXT, " +
                COL_CREATED + " INTEGER, " +
                COL_EXPIRY + " INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldv, int newv) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insertNote(String title, String content, long createdAt, long expiryAt){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_CONTENT, content);
        cv.put(COL_CREATED, createdAt);
        cv.put(COL_EXPIRY, expiryAt);
        long id = db.insert(TABLE, null, cv);
        db.close();
        return id;
    }

    public void deleteNoteById(long id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Note> getAllNotes(){
        List<Note> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, null, null, null, null, COL_CREATED + " DESC");
        while(c.moveToNext()){
            long id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
            String t = c.getString(c.getColumnIndexOrThrow(COL_TITLE));
            String cont = c.getString(c.getColumnIndexOrThrow(COL_CONTENT));
            long created = c.getLong(c.getColumnIndexOrThrow(COL_CREATED));
            long expiry = c.getLong(c.getColumnIndexOrThrow(COL_EXPIRY));
            list.add(new Note(id,t,cont,created,expiry));
        }
        c.close();
        db.close();
        return list;
    }
}
