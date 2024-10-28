package com.example.note.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.note.dao.NoteDao;
import com.example.note.entities.Notes;

@Database(entities = {Notes.class}, version = 1, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {
    // khai báo một biến database
    private static NotesDatabase notesDatabase;
    // khai báo một biến Room
    private static androidx.room.Room Room;
    // hàm khởi tạo database
    public static synchronized NotesDatabase getDatabase(Context context) {
        if (notesDatabase == null) {
            notesDatabase = androidx.room.Room.databaseBuilder(
                    context,
                    NotesDatabase.class,
                    "notes_db"
            ).build();
        }
        return notesDatabase;
    }
    // hàm trả về đối tượng NoteDao
    public abstract NoteDao noteDao();
}
