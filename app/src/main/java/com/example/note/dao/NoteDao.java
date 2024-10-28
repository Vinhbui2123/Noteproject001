package com.example.note.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.note.entities.Notes;

import java.util.List;

@Dao
public interface NoteDao {

    // hàm lấy tất cả các note từ database
    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Notes> getAllNotes();
    // hàm lấy note theo id từ database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotes(Notes notes);
    // hàm xóa note từ database
    @Delete
    void deleteNotes(Notes notes);


}
