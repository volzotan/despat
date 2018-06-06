package de.volzo.despat.userinterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;

public class SessionListActivity extends AppCompatActivity {

    public static final String TAG = SessionActivity.class.getSimpleName();

    Activity activity;
    ActionBar bar;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessionlist);

        this.activity = this;
        this.context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((AppCompatActivity) this).setSupportActionBar(toolbar);
        bar = ((AppCompatActivity) this).getSupportActionBar();
        bar.setTitle("Sessions");
        bar.setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.session_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SessionRecyclerViewAdapter(activity, this));
    }

    public void onSelect(Session session) {
        Intent intent = new Intent(activity, SessionActivity.class);
        intent.putExtra(SessionActivity.ARG_SESSION_ID, session.getId());
        startActivity(intent);
    }

    class SessionRecyclerViewAdapter extends RecyclerView.Adapter<SessionRecyclerViewAdapter.ViewHolder> {

        public final String TAG = SessionRecyclerViewAdapter.class.getSimpleName();

        private Context context;
        private final SessionListActivity activity;
        private List<Session> sessions;

        public SessionRecyclerViewAdapter(Context context, SessionListActivity activity) {

            this.context = context;
            this.activity = activity;

            AppDatabase db = AppDatabase.getAppDatabase(context);
            SessionDao sessionDao = db.sessionDao();

            sessions = sessionDao.getAll();
        }

        @Override
        public SessionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_sessionlist_item, parent, false);
            return new SessionRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SessionRecyclerViewAdapter.ViewHolder holder, int position) {
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
                    if (activity != null) {
                        activity.onSelect(holder.session);
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
