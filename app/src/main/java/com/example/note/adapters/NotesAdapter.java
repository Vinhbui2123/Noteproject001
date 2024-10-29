package com.example.note.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.R;
import com.example.note.entities.Notes;
import com.example.note.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    // khai báo một list chứa các note
    private List<Notes> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Notes> notesSource;

    // hàm khởi tạo adapter với tham số là list các note
    public NotesAdapter(List<Notes> notes , NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        notesSource = notes;
    }

    @NonNull
    @Override
    // hàm tạo view holder cho mỗi item trong list note
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    // hàm gán dữ liệu cho mỗi item trong list note
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position) , position);
            }
        });
    }

    @Override
    // hàm trả về số lượng item trong list note
    public int getItemCount() {
        return notes.size();
    }

    @Override
    // hàm trả về loại item trong list note
    public int getItemViewType(int position) {
        return position;
    }
    // class view holder cho mỗi item trong list note
    static class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView textTitle , textSubtitle , textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        // hàm khởi tạo view holder với tham số là view của item
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubTitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
        }
        // hàm gán dữ liệu cho mỗi item trong list note
        void setNote(Notes note)
        {
            textTitle.setText(note.getTitle());
            if(note.getSubtitle().trim().isEmpty())
            {
                textSubtitle.setVisibility(View.GONE);
            }
            else
            {
                textSubtitle.setText(note.getSubtitle());
            }
            textDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if(note.getColor() != null)
            {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            else
            {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if(note.getImagePath() != null)
            {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }else {
                imageNote.setVisibility(View.GONE);
            }
        }
    }

    public void searchNotes(final String searchKeyWord)
    {
        timer = new Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                if(searchKeyWord.trim().isEmpty())
                {
                    notes = notesSource;
                }
                else
                {
                    ArrayList<Notes> temp = new ArrayList<>();
                    for(Notes note : notesSource)
                    {
                        if(note.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                note.getSubtitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                note.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase()))
                        {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        } , 500);
    }

    public void cancelTimer()
    {
        if(timer != null)
        {
            timer.cancel();
        }
    }
}
