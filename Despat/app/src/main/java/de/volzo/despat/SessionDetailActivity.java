package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.ErrorEvent;
import de.volzo.despat.persistence.ErrorEventDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;

public class SessionDetailActivity extends AppCompatActivity {

    public static final String TAG = SessionDetailActivity.class.getSimpleName();
    public SimpleDateFormat dateFormat = new SimpleDateFormat("mm.dd hh:mm:ss");

    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        Bundle extras = getIntent().getExtras();
        long sessionId = extras.getLong("sessionId");

        if (sessionId <= 0) {
            Log.e(TAG, "invalid session ID for detail view: " + sessionId);
            return;
        }

        // TODO: move this to handler

        AppDatabase db = AppDatabase.getAppDatabase(this);
        SessionDao sessionDao = db.sessionDao();

        session = sessionDao.getById(sessionId);

        ImageView ivCompressedPreview = (ImageView) findViewById(R.id.compressedpreview);
        TextView tvName = (TextView) findViewById(R.id.name);
        TextView tvStart = (TextView) findViewById(R.id.start);
        TextView tvEnd = (TextView) findViewById(R.id.end);
        TextView tvDuration = (TextView) findViewById(R.id.duration);
        TextView tvNumberOfCaptures = (TextView) findViewById(R.id.numberOfCaptures);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.missing_img);
        ivCompressedPreview.setImageBitmap(bm);

        tvName.setText(session.getSessionName());
        tvStart.setText(dateFormat.format(session.getStart()));
        tvEnd.setText(dateFormat.format(session.getEnd()));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new ErrorEventListAdapter(this, recyclerView);
        recyclerView.setAdapter(adapter);
    }
}

class ErrorEventListAdapter extends RecyclerView.Adapter<ErrorEventListAdapter.ViewHolder> implements View.OnClickListener {

    public static final String TAG = ErrorEventListAdapter.class.getSimpleName();

    private Context context;
    private RecyclerView recyclerView;
    private List<ErrorEvent> errorEvents;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView preview;
        public TextView name;
//        public TextView start;
//        public TextView end;
//        public TextView duration;
//        public TextView numberOfCaptures;

        public ViewHolder(View v) {
            super(v);

            preview = (ImageView) v.findViewById(R.id.compressedpreview);
            name = (TextView) v.findViewById(R.id.name);
        }
    }

    public ErrorEventListAdapter(Context context, RecyclerView recyclerView) {

        this.context = context;
        this.recyclerView = recyclerView;

        AppDatabase db = AppDatabase.getAppDatabase(context);
        ErrorEventDao errorEventDao = db.errorEventDao();

        errorEvents = errorEventDao.getAll();
    }

    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);

//        Intent intent = new Intent(context, SessionDetailActivity.class);
//        intent.putExtra("sessionId", sessions.get(itemPosition).getId());
//        context.startActivity(intent);
    }

    @Override
    public ErrorEventListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleritem_session, parent, false);

        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ErrorEvent errorEvent = errorEvents.get(position);

        holder.name.setText(errorEvent.getType());
//        holder.start.setText(dateFormat.format(session.getStart()));
//        holder.end.setText(dateFormat.format(session.getEnd()));
    }

    @Override
    public int getItemCount() {
        return errorEvents.size();
    }
}

