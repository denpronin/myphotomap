package com.pronin.myphotomap;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pronin.myphotomap.model.LatLong;
import com.pronin.myphotomap.model.PhotoArrayFormation;
import com.pronin.myphotomap.model.Picture;
import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private PhotoArrayFormation photoArrayFormation;
    private HashMap<LatLong, ArrayList<Picture>> markersMap;
    private static final String PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String PERMISSION_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_MEDIA_LOCATION = Manifest.permission.ACCESS_MEDIA_LOCATION;
    private static final int PERMISSION_REQUEST_CODE = 856;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        photoArrayFormation = new PhotoArrayFormation();
        markersMap = new HashMap<>();
        if (ContextCompat.checkSelfPermission(this, PERMISSION_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, PERMISSION_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, PERMISSION_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setMarkersMap();
        } else ActivityCompat.requestPermissions(this,
                new String[]{ PERMISSION_EXTERNAL_STORAGE, PERMISSION_MEDIA_LOCATION, PERMISSION_FINE_LOCATION },
                PERMISSION_REQUEST_CODE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        focusCamera();
    }

    private void setMarkersMap() {
        photoArrayFormation.getCameraImages(this, new PhotoArrayFormation.OnMarkersMapCreatedListener() {
            @Override
            public void onMarkersMapCreated(HashMap<LatLong, ArrayList<Picture>> pictures) {
                runOnUiThread(() -> {
                    markersMap.putAll(pictures);
                    for (LatLong latLong : markersMap.keySet()) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latLong.getLatitude(), latLong.getLongitude())).draggable(false));
                        Log.d(TAG, "Added marker " + latLong.toString());
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
