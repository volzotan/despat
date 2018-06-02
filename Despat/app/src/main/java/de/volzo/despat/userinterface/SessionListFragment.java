package de.volzo.despat.userinterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;

public class SessionListFragment extends Fragment {

    private OnSessionListSelectionListener listener;

    public SessionListFragment() {}

//    public static SessionListFragment newInstance(int columnCount) {
//        SessionListFragment fragment = new SessionListFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sessionlist, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SessionRecyclerViewAdapter(getActivity(), listener));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnSessionListSelectionListener) {
            listener = (OnSessionListSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnSessionListSelectionListener {
        void onSessionListSelection(Session session);
    }

    class SessionRecyclerViewAdapter extends RecyclerView.Adapter<SessionRecyclerViewAdapter.ViewHolder> {

        public final String TAG = SessionRecyclerViewAdapter.class.getSimpleName();

        private Context context;
        private final SessionListFragment.OnSessionListSelectionListener listener;
        private List<Session> sessions;

        public SessionRecyclerViewAdapter(Context context, SessionListFragment.OnSessionListSelectionListener listener) {

            this.context = context;
            this.listener = listener;

            AppDatabase db = AppDatabase.getAppDatabase(context);
            SessionDao sessionDao = db.sessionDao();

            sessions = sessionDao.getAll();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_sessionlist_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Session session = sessions.get(position);
            holder.session = session;

            AppDatabase db = AppDatabase.getAppDatabase(context);
            SessionDao sessionDao = db.sessionDao();

            try {
                File f = session.getCompressedImage();
                if (f == null) throw new Exception("compressed image missing");
                Glide.with(context).load(f.getAbsoluteFile()).into(holder.preview);
            } catch (Exception e) {
                Log.w(TAG, "compressed preview for session " + session.toString() + "could not be loaded");
//                bm = BitmapFactory.decodeResource(getResources(), R.drawable.missing_img);

                Bitmap bm = null;
                bm = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.missing_img), 400, 300);
                holder.preview.setImageBitmap(bm);
            }

            holder.name.setText(session.getSessionName());
            holder.start.setText(Util.getDateFormat().format(session.getStart()));
            holder.end.setText(Util.getDateFormat().format(session.getEnd()));
            holder.duration.setText(Util.getHumanReadableTimediff(session.getStart(), session.getEnd(), true));
            holder.numberOfCaptures.setText(Integer.toString(sessionDao.getNumberOfCaptures(session.getId())));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onSessionListSelection(holder.session);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View mView;

            public ImageView preview;
            public TextView name;
            public TextView start;
            public TextView end;
            public TextView duration;
            public TextView numberOfCaptures;

            public Session session;

            public ViewHolder(View view) {
                super(view);
                mView = view;

                preview = (ImageView) view.findViewById(R.id.compressedpreview);
                name = (TextView) view.findViewById(R.id.name);
                start = (TextView) view.findViewById(R.id.start);
                end = (TextView) view.findViewById(R.id.end);
                duration = (TextView) view.findViewById(R.id.duration);
                numberOfCaptures = (TextView) view.findViewById(R.id.numberOfCaptures);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + name.getText() + "'";
            }
        }
    }
}

