package de.volzo.despat.userinterface;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.preferences.Config;

public class PointActivity extends AppCompatActivity implements
        View.OnTouchListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener {

    private static final String TAG = PointActivity.class.getSimpleName();

    public static final String ARG_SESSION_ID = "ARG_SESSION_ID";

    Session session;

    ImageView ivPointSelector;
    MagnifierSurface magnifierSurface;
    Button btDone;

    GoogleMap map;
    MapUtil mapUtil;

    MapUtil.DespatMarker newMarker;
    List<MapUtil.DespatMarker> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);

        Bundle arguments = getIntent().getExtras();

        if (arguments == null) {
            Log.e(TAG, "arguments missing");
            return;
        }

        Long sessionId = arguments.getLong(ARG_SESSION_ID);

        if (sessionId == null || sessionId < 0) {
            Log.e(TAG, "missing or empty session id");
            return;
        }

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();

        session = sessionDao.getById(sessionId);

        if (session == null) {
            Log.e(TAG, "invalid session id: " + sessionId);
            return;
        }

        if (session.getCompressedImage() == null || !session.getCompressedImage().exists()) {
            Log.e(TAG, "compressed image missing from session: " + session);
            return;
        }

//        ivPointSelector = (ImageView) findViewById(R.id.iv_point_selector);
//        Glide.with(findViewById(android.R.id.content)).load(new File(Config.getImageFolder(this), "test.jpg")).into(ivPointSelector);
//        ivPointSelector.setOnTouchListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        SupportMapFragment newFragment = new SupportMapFragment();
        transaction.replace(R.id.fragment_container_markermap, newFragment);
        transaction.commit();
        newFragment.getMapAsync(this);

        magnifierSurface = (MagnifierSurface) findViewById(R.id.iv_magnifier);
        magnifierSurface.setImage(session.getCompressedImage());
        magnifierSurface.setOnTouchListener(this);

        btDone = findViewById(R.id.bt_done);
//        btDone.setBackgroundTintList(R.color.);
//        btDone.setBackgroundColor(Color.GRAY);
        btDone.setEnabled(false);
        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btn done");

                if (newMarker != null) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }

            case MotionEvent.ACTION_UP: {
                Log.d(TAG, "new position: " + magnifierSurface.getPosition().toString());
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                magnifierSurface.move(event.getX() / magnifierSurface.getWidth(), event.getY() / magnifierSurface.getHeight());
//                magnifierSurface.move(event.getX() / ivPointSelector.getWidth(), event.getY() / ivPointSelector.getHeight());
                break;
            }

            default: {
                break;
            }
        }

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(map.MAP_TYPE_SATELLITE);

        mapUtil = new MapUtil(this, map);

        if (session.getLocation() != null) {
            mapUtil.moveCamera(session.getLocation());

            markers.add(mapUtil.newMarker(MapUtil.DespatMarker.TYPE_CAMERA, session.getLocation(), "camera position"));
            mapUtil.placeMarkersOnMap(markers);
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "Location data missing, zoom in manually",
                    Snackbar.LENGTH_LONG).show();
        }

        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "marker click: " + marker);
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "on click");

        if (newMarker != null) {
            markers.remove(newMarker);
        }

        newMarker = mapUtil.newMarker(MapUtil.DespatMarker.TYPE_CORRESPONDING_POINT_NEW, latLng, "new point");
        markers.add(newMarker);

        mapUtil.clearAllMarkersOnMap();
        mapUtil.placeMarkersOnMap(markers);

        btDone.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btDone.setEnabled(true);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d(TAG, "on long click");
    }
}
