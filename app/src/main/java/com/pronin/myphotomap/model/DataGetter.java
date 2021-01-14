package com.pronin.myphotomap.model;

import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataGetter {
    public static final String CAMERA_IMAGE_BUCKET_NAME = "Camera";
    private static final String TAG = "DataGetter";

    public void getCameraImages(Context context, OnGettingDataDoneListener callback) {
        Thread getting  = new Thread(() -> {
                final String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};
                final String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";// + " AND " + MediaStore.Images.Media.LONGITUDE + " != ?";
                final String[] selectionArgs = {CAMERA_IMAGE_BUCKET_NAME};//, "null" };
                final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null);

                ArrayList<Pictures> result = new ArrayList<>(cursor.getCount());
                if (cursor.moveToFirst()) {
                    final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    final int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);

                    do {
                        try {
                            final String data = cursor.getString(dataColumn);
                            ExifInterface exif = new ExifInterface(data);
                            float[] imgLatLong = new float[2];
                            boolean hasLatLong = exif.getLatLong(imgLatLong);
                            if (hasLatLong) {
                                Pictures pictures = new Pictures(data, imgLatLong[0], imgLatLong[1], cursor.getString(dateColumn));
                                Log.d(TAG, pictures.toString());
                                result.add(pictures);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } while (cursor.moveToNext());
                } else {
                    Log.d(TAG, "No images found!");
                }
                cursor.close();
                Log.d(TAG, Integer.toString(result.size()));
                callback.onGettingDataDone(result);
        });
        getting.start();

    }

    public interface OnGettingDataDoneListener {
        void onGettingDataDone(List<Pictures> pictures);
    }

}
