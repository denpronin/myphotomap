package com.pronin.myphotomap;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pronin.myphotomap.model.LatLong;
import com.pronin.myphotomap.model.PhotoArrayFormation;
import com.pronin.myphotomap.model.Picture;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private PhotoArrayFormation photoArrayFormation;
    private HashMap<LatLong, ArrayList<Picture>> markersMap;
    private static final String PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String PERMISSION_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_MEDIA_LOCATION = Manifest.permission.ACCESS_MEDIA_LOCATION;
    private static final int PERMISSION_REQUEST_CODE = 856;
    private static final int DEFAULT_THREAD_POOL_SIZE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        photoArrayFormation = new PhotoArrayFormation();
        markersMap = new HashMap<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, PERMISSION_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, PERMISSION_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, PERMISSION_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setMarkersMap();
        } else ActivityCompat.requestPermissions(this,
                new String[]{ PERMISSION_EXTERNAL_STORAGE, PERMISSION_MEDIA_LOCATION, PERMISSION_FINE_LOCATION },
                PERMISSION_REQUEST_CODE);
        focusCamera();
    }

    private Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        try {
            File f = new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 30;
            options.outMimeType = "image/png";
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            bitmap = getRoundedBitmap(bitmap);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap)
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

    private void setMarkersMap() {
        photoArrayFormation.getCameraImages(this, new PhotoArrayFormation.OnMarkersMapCreatedListener() {
            @Override
            public void onMarkersMapCreated(HashMap<LatLong, ArrayList<Picture>> pictures) {
                runOnUiThread(() -> {
                    markersMap.putAll(pictures);
                    ExecutorService executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
                    Bitmap img;
                    for (LatLong latLong : markersMap.keySet()) {
                        final String path = markersMap.get(latLong).get(0).getPath();
                        img = null;
                        Future<Bitmap> future = executorService.submit(() -> getBitmap(path));
                        try {
                            img = future.get();
                        } catch (ExecutionException | InterruptedException ex) {
                            ex.printStackTrace();
                            Log.w(TAG, "Failed to convert image to bitmap");
                        }
                        if (img != null) {
                            mMap.addMarker(new MarkerOptions().position(new LatLng(latLong.getLatitude(), latLong.getLongitude())).draggable(false).icon(BitmapDescriptorFactory.fromBitmap(img)));
                            Log.d(TAG, "Added marker with icon " + latLong.toString());
                        } else {
                            mMap.addMarker(new MarkerOptions().position(new LatLng(latLong.getLatitude(), latLong.getLongitude())).draggable(false));
                            Log.d(TAG, "Added marker without icon " + latLong.toString());
                        }
                    }
                });
            }
        });
    }


    private void focusCamera() {
        if (ContextCompat.checkSelfPermission(this, PERMISSION_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager mng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = mng.getLastKnownLocation(mng.getBestProvider(new Criteria(), false));
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 10);
                mMap.animateCamera(cameraUpdate);
            } else Toast.makeText(this, getString(R.string.no_location), Toast.LENGTH_LONG).show();
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    setMarkersMap();
                    focusCamera();
                } else {
                    Toast.makeText(this, getString(R.string.no_permissions_msg), Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{ PERMISSION_EXTERNAL_STORAGE, PERMISSION_MEDIA_LOCATION, PERMISSION_FINE_LOCATION },
                            PERMISSION_REQUEST_CODE);
                }
            }
        }
    }
}
