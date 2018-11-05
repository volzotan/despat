package de.volzo.despat.userinterface;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.HomographyPointDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.support.Util;

public class HomographyPointListFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private static final String TAG = HomographyPointListFragment.class.getSimpleName();

    private Session session;
    private List<HomographyPoint> points;

    private RecyclerView recyclerView;

    private MapUtil mapUtil;
    private GoogleMap map;
    private List<MapUtil.DespatMarker> markerList = new ArrayList<MapUtil.DespatMarker>();

    public HomographyPointListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        final long sessionId = args.getLong(SessionActivity.ARG_SESSION_ID);

        if (sessionId <= 0) {
            Log.e(TAG, "invalid session ID for homography point list view: " + sessionId);
        }

        AppDatabase db = AppDatabase.getAppDatabase(getContext());
        SessionDao sessionDao = db.sessionDao();

        session = sessionDao.getById(sessionId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_homographypointlist, container, false);

        TextView tooltip_homography = view.findViewById(R.id.tooltip_homography);
        if (!Config.getShowTooltips(getContext())) tooltip_homography.setVisibility(View.INVISIBLE);

        AppDatabase db = AppDatabase.getAppDatabase(getContext());
        HomographyPointDao homographyPointDao = db.homographyPointDao();

        if (session != null) {
            points = homographyPointDao.getAllBySession(session.getId());
        }

        recyclerView = view.findViewById(R.id.rv_homographypoint_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new HomographyPointRecyclerViewAdapter(getActivity(), points, this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        SupportMapFragment newFragment = new SupportMapFragment();
        transaction.replace(R.id.fragment_container_map, newFragment);
        transaction.commit();
        newFragment.getMapAsync(this);

        view.findViewById(R.id.bt_addHomographyPoint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHomographyPointAddListener(session);
            }
        });

        recyclerView.getAdapter().notifyDataSetChanged();

        return view;
    }

//    private void loadMapFragment() {
//
//        ArrayList<String> mpos = new ArrayList<String>();
//        ArrayList<String> mdesc = new ArrayList<String>();
//        ArrayList<Integer> mtype = new ArrayList<Integer>();
//
//        mpos.add("50.971296, 11.037630"); mdesc.add("1"); mtype.add(GmapsFragment.DespatMarker.TYPE_CORRESPONDING_POINT);
//        mpos.add("50.971173, 11.037914"); mdesc.add("2"); mtype.add(GmapsFragment.DespatMarker.TYPE_CORRESPONDING_POINT);
//        mpos.add("50.971456, 11.037915"); mdesc.add("3"); mtype.add(GmapsFragment.DespatMarker.TYPE_CORRESPONDING_POINT);
//        mpos.add("50.971705, 11.037711"); mdesc.add("4"); mtype.add(GmapsFragment.DespatMarker.TYPE_CORRESPONDING_POINT);
//        mpos.add("50.971402, 11.037796"); mdesc.add("5"); mtype.add(GmapsFragment.DespatMarker.TYPE_CORRESPONDING_POINT);
//        mpos.add("50.971636, 11.037486"); mdesc.add("6"); mtype.add(GmapsFragment.DespatMarker.TYPE_CORRESPONDING_POINT);
//        mpos.add("50.971040, 11.038093"); mdesc.add("Camera"); mtype.add(GmapsFragment.DespatMarker.TYPE_CAMERA);
//
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        GmapsFragment newFragment = new GmapsFragment();
//
//        Bundle args = new Bundle();
//        args.putCharSequence("MAP_POSITION", "50.971402, 11.037796");
//        args.putStringArrayList("MAP_MARKER_POSITION", mpos);
//        args.putStringArrayList("MAP_MARKER_DESCRIPTION", mdesc);
//        args.putIntegerArrayList("MAP_MARKER_TYPE", mtype);
//        newFragment.setArguments(args);
//
//        transaction.replace(R.id.fragment_container_gmaps, newFragment);
//        transaction.addToBackStack(null);
//
//        transaction.commit();
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

//        if (context instanceof OnHomographyPointListSelectionListener) {
//            selectionListener = (OnHomographyPointListSelectionListener) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement selection listener");
//        }
//
//        if (context instanceof OnHomographyPointAddListener) {
//            addListener = (OnHomographyPointAddListener) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement add listener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // Map

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(map.MAP_TYPE_SATELLITE);
        mapUtil = new MapUtil(getContext(), map);

        AppDatabase db = AppDatabase.getAppDatabase(getContext());
        SessionDao sessionDao = db.sessionDao();
        HomographyPointDao homographyPointDao = db.homographyPointDao();

        if (session.getLocation() != null) {
            mapUtil.moveCamera(session.getLocation(), 18);

            markerList.add(mapUtil.createMarker(MapUtil.DespatMarker.TYPE_CAMERA, session.getLocation(), "camera position"));
            List<HomographyPoint> homographyPoints = homographyPointDao.getAllBySession(session.getId());
            for (HomographyPoint point : homographyPoints) {
                markerList.add(mapUtil.createMarker(MapUtil.DespatMarker.TYPE_CORRESPONDING_POINT, point.getLocation(), "corresponding point " + point.getId()));
            }
        }
        mapUtil.placeMarkersOnMap(markerList);
        mapUtil.disableGestures(true);

        map.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer tag = (Integer) marker.getTag();
        return false;
    }

    // Fragment

    public void onHomographyPointListSelectionListener(HomographyPoint homographyPoint, int position) {
        AppDatabase database = AppDatabase.getAppDatabase(getContext());
        HomographyPointDao homographyPointDao = database.homographyPointDao();

        homographyPointDao.delete(homographyPoint);

        ((HomographyPointRecyclerViewAdapter) recyclerView.getAdapter()).data.remove(homographyPoint);
        recyclerView.getAdapter().notifyItemRemoved(position);
//        recyclerView.getAdapter().notifyDataSetChanged();
//        recyclerView.getAdapter().notifyItemRangeChanged(position, position+1);
    }

    public void onHomographyPointAddListener(Session session) {
        Log.d(TAG, "homography point add: " + session);

        Intent pointIntent = new Intent(getContext(), PointActivity.class);
        pointIntent.putExtra(PointActivity.ARG_SESSION_ID, session.getId());
        startActivityForResult(pointIntent, PointActivity.REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != getActivity().RESULT_OK) {
            Log.e(TAG, "onActivityResult non OK result");
            return;
        }

        if (requestCode != PointActivity.REQUEST_CODE) {
            Log.wtf(TAG, "unknown onActivityResult");
            return;
        }

        Log.d(TAG, "PointActivity successful");

        if (session == null) {
            Log.e(TAG, "session information missing");
            return;
        }

        double[] imageCoordinates = data.getDoubleArrayExtra(PointActivity.DATA_IMAGE_COORDINATES);
        double[] mapCoordinates = data.getDoubleArrayExtra(PointActivity.DATA_MAP_COORDINATES);

        AppDatabase db = AppDatabase.getAppDatabase(getContext());
        HomographyPointDao homographyPointDao = db.homographyPointDao();

        HomographyPoint newPoint = new HomographyPoint();
        newPoint.setSessionId(session.getId());
        newPoint.setX(imageCoordinates[0]);
        newPoint.setY(imageCoordinates[1]);
        newPoint.setLatitude(mapCoordinates[0]);
        newPoint.setLongitude(mapCoordinates[1]);
        newPoint.setModificationTime(Calendar.getInstance().getTime());

        List<Long> newPointIds = homographyPointDao.insert(newPoint);

        newPoint.setId(newPointIds.get(0));
        points.add(newPoint);

        recyclerView.getAdapter().notifyDataSetChanged();

        markerList.add(mapUtil.createMarker(MapUtil.DespatMarker.TYPE_CORRESPONDING_POINT, newPoint.getLocation(), "corresponding point " + newPoint.getId()));
        mapUtil.clearAllMarkersOnMap();
        mapUtil.placeMarkersOnMap(markerList);

        Integer count = homographyPointDao.getCountBySession(session.getId());
        if (count != null && count >= 4) {
            Orchestrator.runHomographyService(this.getActivity(), session.getId());
        }
    }

    class HomographyPointRecyclerViewAdapter extends RecyclerView.Adapter<HomographyPointRecyclerViewAdapter.ViewHolder> {

        public final String TAG = HomographyPointRecyclerViewAdapter.class.getSimpleName();

        private Context context;
        private final HomographyPointListFragment listener;
        public List<HomographyPoint> data;

        public HomographyPointRecyclerViewAdapter(Context context, List<HomographyPoint> data, HomographyPointListFragment listener) {
            this.context = context;
            this.listener = listener;
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_homographypointlist_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            HomographyPoint point = data.get(position);
            holder.point = point;

            if (session.getLatitude() != null && session.getLongitude() != null) {
                float dist = Util.distanceBetweenCoordinates(session.getLatitude(), session.getLongitude(), point.getLatitude(), point.getLongitude());
                holder.desc.setText(String.format(Config.LOCALE, "distance to camera: %-4.2fm", dist));
            } else {
                Log.w(TAG, "session is missing location information");
            }

//            holder.view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (null != listener) {
//                        listener.onHomographyPointListSelectionListener(holder.point);
//                    }
//                }
//            });
            holder.view.findViewById(R.id.bt_homography_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onHomographyPointListSelectionListener(holder.point, holder.getAdapterPosition());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;

            public TextView desc;

            public HomographyPoint point;

            public ViewHolder(View view) {
                super(view);
                this.view = view;

                desc = (TextView) view.findViewById(R.id.idNumber);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + desc.getText() + "'";
            }
        }
    }
}
