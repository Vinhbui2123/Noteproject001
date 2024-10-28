package com.example.note.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.note.R;
import com.example.note.adapters.NotesAdapter;
import com.example.note.database.NotesDatabase;
import com.example.note.entities.Notes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private List<Notes> noteList;
    private NotesAdapter notesAdapter;
    private ActivityResultLauncher<Intent> addNoteLauncher;
    // hàm khởi tạo activity và khởi tạo các biến cần thiết
    // cũng như gán sự kiện khi click vào nút tạo note mới
    // và gọi hàm lấy dữ liệu từ database
    // và hiển thị
    // và gán sự kiện khi nhận kết quả trả về từ activity tạo note mới
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNoteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Handle the result if needed
                        getNotes();
                    }
                }
        );

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList);
        notesRecyclerView.setAdapter(notesAdapter);


        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, createNoteActivity.class);
            addNoteLauncher.launch(intent);
        });
        getNotes();
    }
    // hàm lấy dữ liệu từ database và hiển thị
    private void getNotes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            final List<Notes> notes = NotesDatabase
                    .getDatabase(getApplicationContext())
                    .noteDao().getAllNotes();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void run() {
                    if(noteList.isEmpty()){
                        noteList.addAll(notes);
                        notesAdapter.notifyDataSetChanged();
                    }
                    else{
                        noteList.add(0, notes.get(0));
                        notesAdapter.notifyItemInserted(0);
                    }
                    notesRecyclerView.smoothScrollToPosition(0);
                }
            });
        });
    }
    // hàm nhận kết quả trả về từ activity tạo note mới và cập nhật dữ liệu
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK){
            getNotes();
        }
    }
}