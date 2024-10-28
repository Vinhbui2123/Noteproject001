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
import com.example.note.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NotesListener {

    private RecyclerView notesRecyclerView;
    private List<Notes> noteList;
    private NotesAdapter notesAdapter;
    private ActivityResultLauncher<Intent> addNoteLauncher;
    public int noteClickedPosition = -1;
    public final static int REQUEST_CODE_SHOW_NOTE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNoteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean isNoteUpdated = result.getData().getBooleanExtra("isNoteUpdated", false);
                        if (isNoteUpdated) {
                            getNotes();
                        }
                    }
                }
        );

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, createNoteActivity.class);
            addNoteLauncher.launch(intent);
        });
        getNotes();
    }

    @Override
    public void onNoteClicked(Notes note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(MainActivity.this, createNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        addNoteLauncher.launch(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getNotes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            final List<Notes> notes = NotesDatabase
                    .getDatabase(getApplicationContext())
                    .noteDao().getAllNotes();
            new Handler(Looper.getMainLooper()).post(() -> {
                noteList.clear();
                noteList.addAll(notes);
                notesAdapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            getNotes();
        }
    }
}