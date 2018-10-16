package de.volzo.despat.userinterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.volzo.despat.R;
import de.volzo.despat.SessionManager;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;

public class SessionListActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String TAG = SessionListActivity.class.getSimpleName();

    Context context;

    Activity activity;
    ActionBar bar;

    List<Session> sessions;
    HashMap<Session, Integer> sessionsDeleteList = new HashMap<Session, Integer>();
    Session deletedSession;
    Integer deletedSessionIndex;

    SessionRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessionlist);

        this.activity = this;
        this.context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((AppCompatActivity) this).setSupportActionBar(toolbar);
        bar = ((AppCompatActivity) this).getSupportActionBar();
        bar.setTitle("Datasets");
        bar.setDisplayHomeAsUpEnabled(true);

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        sessions = sessionDao.getAll();

        List<Session> dummyData = new LinkedList<>();
        SessionManager sessionManager = SessionManager.getInstance(context);
        dummyData.add(sessionManager.createDummyData());
        dummyData.add(sessionManager.createDummyData());
        dummyData.add(sessionManager.createDummyData());
//        sessions = dummyData;


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.session_list);

        if (sessions == null || sessions.size() == 0) {
            LinearLayout ll_empty = findViewById(R.id.empty_view);
            recyclerView.setVisibility(View.GONE);
            ll_empty.setVisibility(View.VISIBLE);
            return;
        }

        adapter = new SessionRecyclerViewAdapter(activity, sessions, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

//        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(this, sessionsDeleteList, adapter);
//        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
//        touchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (deletedSession != null) deleteSession();
    }

    public void onSelect(Session session) {
        if (deletedSession != null) deleteSession();

        Intent intent = new Intent(activity, SessionActivity.class);
        intent.putExtra(SessionActivity.ARG_SESSION_ID, session.getId());
        startActivity(intent);
    }

    public void deleteSession() {
        if (deletedSession == null || deletedSessionIndex == null) {
            Log.w(TAG, "nothing to delete");
            return;
        }

        if (sessions != null && sessions.contains(deletedSession)) {
            Log.w(TAG, "session " + deletedSession + " still in session list");
            sessions.remove(deletedSession);
        }

        SessionManager.deleteSessionFromDatabase(context, deletedSession);

        deletedSession = null;
        deletedSessionIndex = null;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof SessionRecyclerViewAdapter.ViewHolder) {

            Session session = sessions.get(viewHolder.getAdapterPosition());

            if (deletedSession != null) {
                deleteSession();
            }

            if (session == null) {
                Log.e(TAG, "delete failed, no sessions present");
                Snackbar.make(findViewById(android.R.id.content), "delete failed, no sessions present", Snackbar.LENGTH_LONG).show();
                return;
            }

            // backup of removed item for undo purpose
            deletedSession = sessions.get(viewHolder.getAdapterPosition());
            deletedSessionIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), session.getSessionName() + " deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem(deletedSession, deletedSessionIndex);
                    deletedSession = null;
                    deletedSessionIndex = null;
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    class SessionRecyclerViewAdapter extends RecyclerView.Adapter<SessionRecyclerViewAdapter.ViewHolder> {

        public final String TAG = SessionRecyclerViewAdapter.class.getSimpleName();

        private Context context;
        private final SessionListActivity activity;
        private List<Session> sessions;

        public SessionRecyclerViewAdapter(Context context, List<Session> sessions, SessionListActivity activity) {

            this.context = context;
            this.sessions = sessions;
            this.activity = activity;
        }

        @Override
        public SessionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_sessionlist_item, parent, false);
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
                Bitmap bm = null;
                bm = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.missing_img), 400, 300);
                holder.preview.setImageBitmap(bm);
            }

            holder.name.setText(session.getSessionName());
            holder.start.setText(Util.getDateFormat().format(session.getStart()));

            if (session.getEnd() != null) {
                holder.end.setText(Util.getDateFormat().format(session.getEnd()));
                holder.duration.setText(Util.getHumanReadableTimediff(session.getStart(), session.getEnd(), true));
            } else {
                Log.w(TAG, "session " + session + "is missing end");
                holder.end.setText("---");
                holder.duration.setText("---");
            }
            holder.numberOfCaptures.setText(Integer.toString(sessionDao.getNumberOfCaptures(session.getId())));

            holder.view.setOnClickListener(new View.OnClickListener() {
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

        public void removeItem(int position) {
            this.sessions.remove(position);
            // notify the item removed by position
            // to perform recycler view delete animations
            // NOTE: don't call notifyDataSetChanged()
            notifyItemRemoved(position);
        }

        public void restoreItem(Session session, int position) {
            this.sessions.add(position, session);
            // notify item added by position
            notifyItemInserted(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;

            public LinearLayout viewForeground;

            public ImageView preview;
            public TextView name;
            public TextView start;
            public TextView end;
            public TextView duration;
            public TextView numberOfCaptures;

            public Session session;

            public ViewHolder(View view) {
                super(view);
                this.view = view;

                viewForeground = view.findViewById(R.id.view_foreground);

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

class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
        private RecyclerItemTouchHelperListener listener;

        public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
            super(dragDirs, swipeDirs);
            this.listener = listener;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (viewHolder != null) {
                final View foregroundView = ((SessionListActivity.SessionRecyclerViewAdapter.ViewHolder) viewHolder).viewForeground;

                getDefaultUIUtil().onSelected(foregroundView);
            }
        }

        @Override
        public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
            final View foregroundView = ((SessionListActivity.SessionRecyclerViewAdapter.ViewHolder) viewHolder).viewForeground;
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final View foregroundView = ((SessionListActivity.SessionRecyclerViewAdapter.ViewHolder) viewHolder).viewForeground;
            getDefaultUIUtil().clearView(foregroundView);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            final View foregroundView = ((SessionListActivity.SessionRecyclerViewAdapter.ViewHolder) viewHolder).viewForeground;

            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
        }

        @Override
        public int convertToAbsoluteDirection(int flags, int layoutDirection) {
            return super.convertToAbsoluteDirection(flags, layoutDirection);
        }

        public interface RecyclerItemTouchHelperListener {
            void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
        }
}

