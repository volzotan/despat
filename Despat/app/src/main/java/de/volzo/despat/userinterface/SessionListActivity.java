package de.volzo.despat.userinterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import java.util.List;
import java.util.Map;

import de.volzo.despat.R;
import de.volzo.despat.RecordingSession;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;

public class SessionListActivity extends AppCompatActivity {

    public static final String TAG = SessionActivity.class.getSimpleName();

    Context context;

    Activity activity;
    ActionBar bar;

    List<Session> sessions;
    HashMap<Session, Integer> sessionsDeleteList = new HashMap<Session, Integer>();

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

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        sessions = sessionDao.getAll();

        SessionRecyclerViewAdapter adapter = new SessionRecyclerViewAdapter(activity, sessions, this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.session_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(this, sessionsDeleteList, adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void deleteSessions() {
        for (Map.Entry<Session, Integer> entry : sessionsDeleteList.entrySet()) {
            Session session = entry.getKey();
            Integer i = entry.getValue();

            if (sessions != null && sessions.contains(session)) {
                Log.w(TAG, "session " + session + " still in session list");
                sessions.remove(session);
            }

            RecordingSession.deleteSessionFromDatabase(context, session);
            Log.d(TAG, "deleted session: " + session);
        }

        sessionsDeleteList.clear();
    }

    public void onSelect(Session session) {
        Intent intent = new Intent(activity, SessionActivity.class);
        intent.putExtra(SessionActivity.ARG_SESSION_ID, session.getId());
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();

        deleteSessions();
    }

    public interface ItemTouchHelperAdapter {

        boolean onItemMove(int fromPosition, int toPosition);

        void onItemDismiss(int position);
    }

    public interface ItemTouchHelperViewHolder {

        void onItemSelected();

        void onItemClear();
    }

    class SessionRecyclerViewAdapter extends RecyclerView.Adapter<SessionRecyclerViewAdapter.ViewHolder> implements ItemTouchHelperAdapter {

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

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            return false;
        }

        @Override
        public void onItemDismiss(int position) {
            removeItem(position);
        }

        public void removeItem(int position) {
            sessions.remove(position);
            // notify the item removed by position
            // to perform recycler view delete animations
            // NOTE: don't call notifyDataSetChanged()
            notifyItemRemoved(position);
        }

        public void restoreItem(Session session, int position) {
            sessions.add(position, session);
            notifyItemInserted(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

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
            public void onItemSelected() {
                itemView.setBackgroundColor(Color.LTGRAY);
            }

            @Override
            public void onItemClear() {
                itemView.setBackgroundColor(0);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + name.getText() + "'";
            }
        }
    }

    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

        public static final float ALPHA_FULL = 1.0f;

        private final SessionListActivity activity;
        private final ItemTouchHelperAdapter adapter;

        HashMap<Session, Integer> sessionsDeleteList;

        public SimpleItemTouchHelperCallback(SessionListActivity activity, HashMap<Session, Integer> sessionsDeleteList, ItemTouchHelperAdapter adapter) {
            this.activity = activity;
            this.sessionsDeleteList = sessionsDeleteList;
            this.adapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                final int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            } else {
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                final int swipeFlags = ItemTouchHelper.START ; //ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }

            adapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
//            activity.onDelete(sessions.get(viewHolder.getAdapterPosition()));

            Session session = activity.getSessions().get(viewHolder.getAdapterPosition());

            if (session == null) {
                Log.e(TAG, "delete failed, no sessions present");
                Snackbar.make(findViewById(android.R.id.content), "delete failed, no sessions present", Snackbar.LENGTH_LONG).show();
            }

            if (sessionsDeleteList.containsKey(session)) {
                Log.w(TAG, "session already in delete list");
                return;
            }

            if (sessionsDeleteList.size() > 0) {
                activity.deleteSessions();
            }

            sessionsDeleteList.put(session, viewHolder.getAdapterPosition());

            String msg = null;

            if (sessionsDeleteList.size() == 1) {
                msg = "Deleted session";
            } else {
                msg = "Deleted " + sessionsDeleteList.size() + " sessions";
            }

            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (Map.Entry<Session, Integer> entry : sessionsDeleteList.entrySet()) {
                        Session s = entry.getKey();
                        Integer i = entry.getValue();
                        ((SessionRecyclerViewAdapter) adapter).restoreItem(s, i);
                    }
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

            adapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
//                viewHolder.itemView.setAlpha(alpha);
                ((SessionRecyclerViewAdapter.ViewHolder) viewHolder).viewForeground.setTranslationX(dX);
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder instanceof ItemTouchHelperViewHolder) {
                    ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                    itemViewHolder.onItemSelected();
                }
            }

            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            viewHolder.itemView.setAlpha(ALPHA_FULL);

            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                ItemTouchHelperViewHolder itemViewHolder = (SessionRecyclerViewAdapter.ViewHolder) viewHolder;
                itemViewHolder.onItemClear();
            }
        }
    }
}
