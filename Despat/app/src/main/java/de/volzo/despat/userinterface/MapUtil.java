package de.volzo.despat.userinterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.R;

public class MapUtil {

    private static final String TAG = MapUtil.class.getSimpleName();

    Context context;

    GoogleMap map;

    LatLng position;
    float zoom = 19;

    public MapUtil(Context context) {
        this.context = context;
    }

    public MapUtil(Context context, GoogleMap map) {
        this.context = context;
        this.map = map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public List<DespatMarker> parseArguments(Bundle args) {

        List<DespatMarker> markers = new ArrayList<DespatMarker>();

        try {
            position = parseLatLng(args.getString("MAP_POSITION"));
            ArrayList<String> markerPositions = args.getStringArrayList("MAP_MARKER_POSITION");
            ArrayList<String> markerDescriptions = args.getStringArrayList("MAP_MARKER_DESCRIPTION");
            ArrayList<Integer> markerTypes = args.getIntegerArrayList("MAP_MARKER_TYPE");

            for (int i=0; i<markerPositions.size(); i++) {
                markers.add(new DespatMarker(markerTypes.get(i), parseLatLng(markerPositions.get(i)), markerDescriptions.get(i)));
            }

        } catch (Exception e) {
            Log.w(TAG, "maps started without information: ", e);
        }

        return markers;
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

    public void moveCamera(LatLng latLng) {
        moveCamera(latLng, this.zoom);
    }

    public void moveCamera(Location loc) {
        moveCamera(loc, this.zoom);
    }

    public void moveCamera(Location loc, float z) {
        this.zoom = z;
        moveCamera(new LatLng(loc.getLatitude(), loc.getLongitude()), this.zoom);
    }

    public void moveCamera(LatLng latLng, float z) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, z));
    }

    public void disableGestures(boolean action) {
        map.getUiSettings().setAllGesturesEnabled(!action);
    }

    public void clearAllMarkersOnMap() {
        map.clear();
    }

    public void placeMarkersOnMap(List<DespatMarker> markers) {
        for (DespatMarker m : markers) {

            MarkerOptions marker = new MarkerOptions();
            marker.position(m.pos);
            marker.title(m.description);

            switch (m.type) {
                case DespatMarker.TYPE_CORRESPONDING_POINT: {
                    marker.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(context, R.drawable.ic_marker_plus)));
                    break;
                }

                case DespatMarker.TYPE_CORRESPONDING_POINT_NEW: {
                    marker.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(context, R.drawable.ic_marker_plus_new)));
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

    public DespatMarker createMarker(int type, Location pos, String description) {
        return new DespatMarker(type, new LatLng(pos.getLatitude(), pos.getLongitude()), description);
    }

    public DespatMarker createMarker(int type, LatLng pos, String description) {
        return new DespatMarker(type, pos, description);
    }

    public class DespatMarker {

        public static final int TYPE_CAMERA                     = 0x10;
        public static final int TYPE_CORRESPONDING_POINT        = 0x20;
        public static final int TYPE_CORRESPONDING_POINT_NEW    = 0x30;

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
