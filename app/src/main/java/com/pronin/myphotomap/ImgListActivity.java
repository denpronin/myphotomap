package com.pronin.myphotomap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.Intent;
import android.os.Bundle;
import com.pronin.myphotomap.model.Picture;
import java.util.ArrayList;

public class ImgListActivity extends AppCompatActivity {
    public static final String EXTRA_IMG_LIST = "ImgList";
    private ArrayList<Picture> imgList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_list);
        Intent intent = getIntent();
        imgList = intent.getParcelableArrayListExtra(EXTRA_IMG_LIST);
        initImgList();

    }

    private void initImgList() {
        RecyclerView recyclerView = findViewById(R.id.img_list);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        ImgListAdapter.OnImgClickListener onImgClick = new ImgListAdapter.OnImgClickListener() {
            @Override
            public void onImgClick(Picture picture) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(ImgListActivity.this, ImageActivity.class);
                    intent.putExtra(ImageActivity.EXTRA_IMG, picture.getPath());
                    startActivity(intent);
                });
            }
        };
        ImgListAdapter imgListAdapter = new ImgListAdapter(onImgClick);
        imgListAdapter.setItems(imgList);
        recyclerView.setAdapter(imgListAdapter);
    }
}