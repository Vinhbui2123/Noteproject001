package com.example.note.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
    private AlertDialog dialogDeleteNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNoteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean isNoteUpdated = result.getData().getBooleanExtra("isNoteUpdated", false);
                        boolean isNoteDeleted = result.getData().getBooleanExtra("isNoteDeleted", false);
                        if (isNoteUpdated || isNoteDeleted) {
                            getNotes(isNoteDeleted);
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
        getNotes(false);
        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no need to implement
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });
    }

    @Override
    public void onNoteClicked(Notes note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(MainActivity.this, createNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        addNoteLauncher.launch(intent);
    }

    public void onNoteLongClicked(Notes note, int position) {
        showDeleteNoteDialog(note, position);
    }

    private void showDeleteNoteDialog(Notes note, int position) {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(v -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNotes(note);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        noteList.remove(position);
                        notesAdapter.notifyItemRemoved(position);
                        dialogDeleteNote.dismiss();
                    });
                });
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogDeleteNote.dismiss());
        }
        dialogDeleteNote.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getNotes(final boolean isNoteDeleted) {
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
}