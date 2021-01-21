package com.pronin.myphotomap;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageActivity extends AppCompatActivity {
    public static final String EXTRA_IMG = "img";
    private float dY = 0.0f;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Intent intent = getIntent();
        String img_path = intent.getStringExtra(EXTRA_IMG);
        ImageView single_img = findViewById(R.id.single_image);
        Picasso.get()
                .load("file://" + img_path)
                .error(R.drawable.ic_launcher_background)
                .into(single_img);
        single_img.performClick();
        single_img.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dY = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.animate()
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        break;
                    case MotionEvent.ACTION_UP:
                        finish();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }
}