package com.example.note.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.note.R;
import com.example.note.database.NotesDatabase;
import com.example.note.entities.Notes;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.View;
import android.content.res.ColorStateList;

public class createNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime;
    private View viewSubtitleIndicator;

    private String selectedNoteColor;
    private ImageView imageNote;
    private String selectedImagePath;

    private TextView textWebUrl;
    private LinearLayout layoutWebURL;

    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;

    private AlertDialog dialogAddURL;


    // hàm onCreate() được gọi khi activity được tạo ra dùng để khởi tạo activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener((view -> onBackPressed()));
        // khai báo các biến và ánh xạ các view
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textWebUrl = findViewById(R.id.textWebURL);
        layoutWebURL = findViewById(R.id.layoutWebURL);

        // hàm setText() dùng để hiển thị thời gian hiện tại
        textDateTime.setText(
                new java.text.SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date(System.currentTimeMillis() + 7 * 60 * 60 * 1000))
        );

        ImageView imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener(view -> saveNote());

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        TextView textView = findViewById(R.id.textMiscellaneous);
        // hàm setBackground() dùng để thiết lập hình nền cho view
        // Hiệu ứng Ripple được hỗ trợ từ Android 5.0 (API 21) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
            shapeDrawable.getPaint().setColor(getResources().getColor(R.color.your_color, null));

            RippleDrawable rippleDrawable = new RippleDrawable(
                    ColorStateList.valueOf(getResources().getColor(R.color.ripple_color, null)),
                    shapeDrawable,
                    null
            );

            textView.setBackground(rippleDrawable);
        }

        initMiscellaneous();
        setSubtitleIndicatorColor();

    }
    // hàm saveNote() dùng để lưu ghi chú
    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputNoteSubtitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Notes note = new Notes();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);
        // hàm setWebLink() dùng để thiết lập đường dẫn web
        if(layoutWebURL.getVisibility() == View.VISIBLE) {
            note.setWebLink(textWebUrl.getText().toString());
        }

        // hàm execute() dùng để thực thi một tác vụ
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNotes(note);
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                });
            } catch (Exception e) {
                Log.e("InsertNoteTask", "Error inserting note", e);
            }
        });
    }
    // hàm initMiscellaneous() dùng để khởi tạo các mục khác
    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        // khai báo các biến và ánh xạ các view
        final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);
        // hàm setOnClickListener() dùng để xử lý sự kiện khi người dùng click vào một view
        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(view -> {
            selectedNoteColor = "#333333";
            imageColor1.setImageResource(R.drawable.ic_done);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(view -> {
            selectedNoteColor = "#FDBE3B";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_done);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(view -> {
            selectedNoteColor = "#FF4842";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_done);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(view -> {
            selectedNoteColor = "#3A52Fc";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(R.drawable.ic_done);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(view -> {
            selectedNoteColor = "#000000";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(R.drawable.ic_done);
            setSubtitleIndicatorColor();
        });
        // hàm setOnClickListener() dùng để xử lý sự kiện khi người dùng click vào một view và chọn ảnh từ thư viện ảnh
        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            getApplicationContext(), Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                createNoteActivity.this,
                                new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                REQUEST_CODE_STORAGE_PERMISSION
                        );
                    } else {
                        selectImage();
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(
                            getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                createNoteActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_STORAGE_PERMISSION
                        );
                    } else {
                        selectImage();
                    }
                }
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });
    }
    // hàm setSubtitleIndicatorColor() dùng để thiết lập màu của tiêu đề phụ
    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }
    // hàm selectImage() dùng để chọn ảnh từ thư viện ảnh
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
        }
    }
    // hàm onRequestPermissionsResult() dùng để xử lý kết quả của việc yêu cầu quyền từ người dùng
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // hàm onActivityResult() dùng để xử lý kết quả trả về từ các activity con mà nó gọi ra
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        @SuppressLint("Recycle") InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    // hàm getPathFromUri() dùng để lấy đường dẫn từ Uri của ảnh đã chọn từ thư viện ảnh
    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    // hàm showAddURLDialog() dùng để hiển thị dialog thêm đường dẫn web
    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(createNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            // kiểm tra dialogAddURL có tồn tại hay không
            if(dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            // khai báo các biến và ánh xạ các view
            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                // hàm setOnClickListener() dùng để xử lý sự kiện khi người dùng click vào một view và kiểm tra đường dẫn web
                public void onClick(View view) {
                    if(inputURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText(createNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                        Toast.makeText(createNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        textWebUrl.setText(inputURL.getText().toString());
                        layoutWebURL.setVisibility(View.VISIBLE);
                        dialogAddURL.dismiss();
                    }
                }
            });
            // Hàm setOnClickListener() dùng để xử lý sự kiện khi người dùng click vào một view và hủy bỏ việc thêm đường dẫn web
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogAddURL.dismiss();
                }
            });
        }
        dialogAddURL.show();
    }
}