package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;

public class SessionActivity extends AppCompatActivity {

    public static final String TAG = SessionActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SessionListAdapter(this, recyclerView);
        recyclerView.setAdapter(adapter);

    }

}

class SessionListAdapter extends RecyclerView.Adapter<SessionListAdapter.ViewHolder> implements View.OnClickListener {

    public static final String TAG = SessionListAdapter.class.getSimpleName();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("mm.dd hh:mm:ss");

    private Context context;
    private RecyclerView recyclerView;
    private List<Session> sessions;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView preview;
        public TextView name;
        public TextView start;
        public TextView end;
        public TextView duration;
        public TextView numberOfCaptures;

        public ViewHolder(View v) {
            super(v);

            preview = (ImageView) v.findViewById(R.id.compressedpreview);
            name = (TextView) v.findViewById(R.id.name);
            start = (TextView) v.findViewById(R.id.start);
            end = (TextView) v.findViewById(R.id.end);
            duration = (TextView) v.findViewById(R.id.duration);
            numberOfCaptures = (TextView) v.findViewById(R.id.numberOfCaptures);
        }
    }

    public SessionListAdapter(Context context, RecyclerView recyclerView) {

        this.context = context;
        this.recyclerView = recyclerView;

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();

        sessions = sessionDao.getAll();
    }

    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);

        Intent intent = new Intent(context, SessionDetailActivity.class);
        intent.putExtra("sessionId", sessions.get(itemPosition).getId());
        context.startActivity(intent);
    }

    @Override
    public SessionListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleritem_session, parent, false);

        v.setOnClickListener(this);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Session session = sessions.get(position);

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.missing_img);
        holder.preview.setImageBitmap(bm);

        holder.name.setText(session.getSessionName());
        holder.start.setText(dateFormat.format(session.getStart()));
        holder.end.setText(dateFormat.format(session.getEnd()));
//        holder.duration.setText();
//        holder.numberOfCaptures.setText();
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }
}

