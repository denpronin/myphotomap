package com.pronin.myphotomap;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pronin.myphotomap.model.LatLong;
import com.pronin.myphotomap.util.MarkersWorker;
import com.pronin.myphotomap.model.Picture;
import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private MarkersWorker markersWorker;
    private HashMap<LatLong, ArrayList<Picture>> markersMap;
    private static final String PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String PERMISSION_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_MEDIA_LOCATION = Manifest.permission.ACCESS_MEDIA_LOCATION;
    private static final int PERMISSION_REQUEST_CODE = 856;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        markersWorker = new MarkersWorker();
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
            focusCamera();
        } else ActivityCompat.requestPermissions(this,
                new String[]{ PERMISSION_EXTERNAL_STORAGE, PERMISSION_MEDIA_LOCATION, PERMISSION_FINE_LOCATION },
                PERMISSION_REQUEST_CODE);
        mMap.setOnMarkerClickListener(this);

    }

    private void setMarkersMap() {
        markersWorker.getCameraImages(this, new MarkersWorker.OnMarkersMapCreatedListener() {
            @Override
            public void onDrawIcon(LatLong latLong, Bitmap icon) {
                runOnUiThread(() -> {
                    if (icon != null) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latLong.getLatitude(), latLong.getLongitude()))
                                .draggable(false).icon(BitmapDescriptorFactory.fromBitmap(icon))).setTag(latLong);
                        Log.d(TAG, "Added marker with icon " + latLong.toString());
                    } else {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latLong.getLatitude(), latLong.getLongitude())).draggable(false)).setTag(latLong);
                        Log.d(TAG, "Added marker without icon " + latLong.toString());
                    }
                });
            }

            @Override
            public void onMarkersMapCreated(HashMap<LatLong, ArrayList<Picture>> pictures) {
                runOnUiThread(() -> markersMap.putAll(pictures));
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
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 8);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLong latLong = (LatLong) marker.getTag();
        ArrayList<Picture> imgList = markersMap.get(latLong);
        if (imgList != null) {
            if (imgList.size() == 1) {
                Intent intent = new Intent(this, ImageActivity.class);
                intent.putExtra(ImageActivity.EXTRA_IMG, imgList.get(0).getPath());
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ImgListActivity.class);
                intent.putParcelableArrayListExtra(ImgListActivity.EXTRA_IMG_LIST, imgList);
                startActivity(intent);
            }
        }
        return false;
    }
}
