package de.volzo.despat.userinterface;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.HomographyPointDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.preferences.Config;

public class HomographyPointListFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String TAG = HomographyPointListFragment.class.getSimpleName();

    private OnHomographyPointListSelectionListener selectionListener;
    private OnHomographyPointAddListener addListener;

    private Session session;
    private MapUtil mapUtil;
    private GoogleMap map;

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

        RecyclerView recyclerView = view.findViewById(R.id.rv_homographypoint_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new HomographyPointRecyclerViewAdapter(getActivity(), selectionListener));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        SupportMapFragment newFragment = new SupportMapFragment();
        transaction.replace(R.id.fragment_container_map, newFragment);
        transaction.commit();
        newFragment.getMapAsync(this);

        getView().findViewById(R.id.bt_addHomographyPoint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addListener != null) {
                    addListener.onHomographyPointAddListener(session);
                }
            }
        });

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

        if (context instanceof OnHomographyPointListSelectionListener) {
            selectionListener = (OnHomographyPointListSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement selection listener");
        }

        if (context instanceof OnHomographyPointAddListener) {
            addListener = (OnHomographyPointAddListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement add listener");
        }
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
        // map.setMapType(map.MAP_TYPE_HYBRID);

        mapUtil = new MapUtil(getContext(), map);

//        mapUtil.moveCamera(map);
//
////        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
//        placeMarkersOnMap(map, markers);

        if (session.getLocation() != null) {
            mapUtil.moveCamera(session.getLocation());
        }
        mapUtil.disableGestures(true);

        map.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Integer tag = (Integer) marker.getTag();

        return false;
    }

    // Fragment

    public interface OnHomographyPointListSelectionListener {
        void onHomographyPointListSelectionListener(HomographyPoint homographyPoint);
    }

    public interface OnHomographyPointAddListener {
        void onHomographyPointAddListener(Session session);
    }

    class HomographyPointRecyclerViewAdapter extends RecyclerView.Adapter<HomographyPointRecyclerViewAdapter.ViewHolder> {

        public final String TAG = HomographyPointRecyclerViewAdapter.class.getSimpleName();

        private Context context;
        private final OnHomographyPointListSelectionListener listener;
        private List<HomographyPoint> points;

        public HomographyPointRecyclerViewAdapter(Context context, OnHomographyPointListSelectionListener listener) {

            this.context = context;
            this.listener = listener;

            AppDatabase db = AppDatabase.getAppDatabase(context);
            HomographyPointDao homographyPointDao = db.homographyPointDao();

            if (session != null) {
                points = homographyPointDao.getAllBySession(session.getId());
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_homographypointlist_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            HomographyPoint point = points.get(position);
            holder.point = point;

            holder.idNumber.setText(Long.toString(point.getId()));

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
                        listener.onHomographyPointListSelectionListener(holder.point);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return points.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;

            public TextView idNumber;

            public HomographyPoint point;

            public ViewHolder(View view) {
                super(view);
                this.view = view;

                idNumber = (TextView) view.findViewById(R.id.idNumber);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + idNumber.getText() + "'";
            }
        }
    }
}
