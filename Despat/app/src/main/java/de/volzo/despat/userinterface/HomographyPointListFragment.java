package de.volzo.despat.userinterface;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.HomographyPoint;
import de.volzo.despat.persistence.HomographyPointDao;

public class HomographyPointListFragment extends Fragment {

    private OnHomographyPointListSelectionListener listener;

    public HomographyPointListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: needs sessionId as argument
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_homographypointlist, container, false);

        Context context = view.getContext();
//        RecyclerView recyclerView = (RecyclerView) view;
//        recyclerView.setLayoutManager(new LinearLayoutManager(context));
//        recyclerView.setAdapter(new HomographyPointRecyclerViewAdapter(getActivity(), listener));

        RecyclerView recyclerView = view.findViewById(R.id.rv_homographypoint_list);
        recyclerView.setAdapter(new HomographyPointRecyclerViewAdapter(getActivity(), listener));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnHomographyPointListSelectionListener) {
            listener = (OnHomographyPointListSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnHomographyPointListSelectionListener {
        void onHomographyPointListSelectionListener(HomographyPoint homographyPoint);
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

            points = homographyPointDao.getAll();
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

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
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

            public final View mView;

            public TextView idNumber;

            public HomographyPoint point;

            public ViewHolder(View view) {
                super(view);
                mView = view;

                idNumber = (TextView) view.findViewById(R.id.idNumber);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + idNumber.getText() + "'";
            }
        }
    }
}
