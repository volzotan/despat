package de.volzo.despat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    public static final String TAG = MapsActivity.class.getSimpleName();
    Context context;

    private GoogleMap map;

    LatLng position;
    float zoom = 19;

    ArrayList<DespatMarker> markers = new ArrayList<DespatMarker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        this.context = this;

        Bundle extras = getIntent().getExtras();
        try {
            position = parseLatLng(extras.getString("MAP_POSITION"));
            ArrayList<String> markerPositions = extras.getStringArrayList("MAP_MARKER_POSITION");
            ArrayList<String> markerDescriptions = extras.getStringArrayList("MAP_MARKER_DESCRIPTION");
            ArrayList<Integer> markerTypes = extras.getIntegerArrayList("MAP_MARKER_TYPE");

            for (int i=0; i<markerPositions.size(); i++) {
                markers.add(new DespatMarker(markerTypes.get(i), parseLatLng(markerPositions.get(i)), markerDescriptions.get(i)));
            }

        } catch (Exception e) {
            Log.w(TAG, "maps started without information: ", e);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private LatLng parseLatLng(String data) throws Exception {
        String[] split = data.split(", ");

        if (split == null || split.length != 2) {
            throw new Exception("parsing failed. raw data: " + data);
        }

        Double lat = Double.parseDouble(split[0]);
        Double lon = Double.parseDouble(split[1]);
        return new LatLng(lat, lon);
    }

    private Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    private void placeMarkersOnMap(GoogleMap map, List<DespatMarker> markers) {
        for (DespatMarker m : markers) {

            MarkerOptions marker = new MarkerOptions();
            marker.position(m.pos);
            marker.title(m.description);

            switch (m.type) {
                case DespatMarker.TYPE_CORRESPONDING_POINT: {
                    marker.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(context, R.drawable.ic_marker_plus)));
                    marker.draggable(true);
                    break;
                }

                case DespatMarker.TYPE_CAMERA: {
                    marker.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(context, R.drawable.ic_marker_camera)));
                    break;
                }

                default: {
                    Log.w(TAG, "marker " + m.description + " at position " + m.pos + "has unknown type: " + m.type);
                    marker.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(context, R.drawable.ic_marker_unknown)));
                }
            }

            Marker mapMarker = map.addMarker(marker);
            mapMarker.setTag(0);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(map.MAP_TYPE_SATELLITE);
        // map.setMapType(map.MAP_TYPE_HYBRID);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
        placeMarkersOnMap(map, markers);

        map.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Integer tag = (Integer) marker.getTag();

        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    class DespatMarker {

        public static final int TYPE_CAMERA                 = 0x10;
        public static final int TYPE_CORRESPONDING_POINT    = 0x20;

        public int type;
        public LatLng pos;
        public String description;

        public DespatMarker(int type, LatLng pos, String description) {
            this.type = type;
            this.pos = pos;
            this.description = description;
        }
    }
}
