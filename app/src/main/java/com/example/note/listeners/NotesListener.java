package com.example.note.listeners;

import com.example.note.entities.Notes;

public interface NotesListener {

    void onNoteClicked(Notes note, int position);

}
