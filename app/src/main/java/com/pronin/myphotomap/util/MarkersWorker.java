package com.pronin.myphotomap.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.util.Log;
import com.pronin.myphotomap.model.LatLong;
import com.pronin.myphotomap.model.Picture;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MarkersWorker {
    public static final String CAMERA_IMAGE_BUCKET_NAME = "Camera";
    private static final String TAG = "MarkersWorker";

    public void getCameraImages(Context context, OnMarkersMapCreatedListener callback) {
        Thread getting  = new Thread(() -> {
            HashMap<LatLong, ArrayList<Picture>> markersMap = loadPicturesForMarkers(context);
            for (LatLong latLong : markersMap.keySet()) {
                String path = markersMap.get(latLong).get(0).getPath();
                Bitmap icon = getBitmap(path);
                callback.onDrawIcon(latLong, icon);
            }
            callback.onMarkersMapCreated(markersMap);
        });
        getting.start();

    }

    public interface OnMarkersMapCreatedListener {
        void onMarkersMapCreated(HashMap<LatLong, ArrayList<Picture>> pictures);
        void onDrawIcon(LatLong latLong, Bitmap icon);
    }

    private HashMap<LatLong, ArrayList<Picture>> createMarkersMap(ArrayList<Picture> pictureArrayList) {
        HashMap<LatLong, ArrayList<Picture>> mapMarkers = new HashMap<>();
        ArrayList<Picture> tempImgList;
        for (Picture picture : pictureArrayList) {
            LatLong latLong = picture.getLatLong();
            if (!mapMarkers.containsKey(latLong)) {
                tempImgList = new ArrayList<>();
            } else {
                tempImgList = mapMarkers.get(latLong);
            }
            assert tempImgList != null;
            tempImgList.add(picture);
            mapMarkers.put(latLong, tempImgList);
            Log.d(TAG, "Added " + picture.getPath() + " date " + picture.getDate() + " to key: " + latLong.toString());
        }

        return mapMarkers;
    }

    private Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        File f = new File(path);
        try (FileInputStream fis = new FileInputStream(f)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 30;
            options.outMimeType = "image/png";
            bitmap = BitmapFactory.decodeStream(fis, null, options);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (bitmap != null) bitmap = getRoundedBitmap(bitmap);
        return bitmap;
    }

    private HashMap<LatLong, ArrayList<Picture>> loadPicturesForMarkers(Context context) {
        final String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED };
        final String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";// + " AND " + MediaStore.Images.Media.LONGITUDE + " != ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_NAME };//, "null" };
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        ArrayList<Picture> imgList = new ArrayList<>(cursor.getCount());
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
                        Picture picture = new Picture(data, imgLatLong[0], imgLatLong[1], cursor.getString(dateColumn));
                        Log.d(TAG, picture.toString());
                        imgList.add(picture);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } while (cursor.moveToNext());
        } else {
            Log.w(TAG, "No images found!");
        }
        cursor.close();
        Log.d(TAG, Integer.toString(imgList.size()));
        HashMap<LatLong, ArrayList<Picture>> markersMap = new HashMap<>(createMarkersMap(imgList));
        return markersMap;
    }

    private Bitmap getRoundedBitmap(Bitmap bitmap)
    {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),  bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, Math.min(bitmap.getHeight(), bitmap.getWidth()), Math.min(bitmap.getHeight(), bitmap.getWidth()));
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
