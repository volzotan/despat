package de.volzo.despat.userinterface;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.EventDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;

public class EventListFragment extends Fragment {

    private static final String TAG = EventListFragment.class.getSimpleName();

    private OnEventListSelectionListener listener;
    private Session session;

    public EventListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        final long sessionId = args.getLong(SessionActivity.ARG_SESSION_ID);

        if (sessionId <= 0) {
            Log.e(TAG, "invalid session ID for error list view: " + sessionId);
        }

        AppDatabase db = AppDatabase.getAppDatabase(getContext());
        SessionDao sessionDao = db.sessionDao();

        session = sessionDao.getById(sessionId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_eventlist, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new EventRecyclerViewAdapter(getActivity(), listener));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnEventListSelectionListener) {
            listener = (OnEventListSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnEventListSelectionListener {
        void onEventListSelection(Event event);
    }

    class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {

        public final String TAG = EventRecyclerViewAdapter.class.getSimpleName();

        private Context context;
        private final EventListFragment.OnEventListSelectionListener listener;
        private List<Event> events;

        public EventRecyclerViewAdapter(Context context, EventListFragment.OnEventListSelectionListener listener) {

            this.context = context;
            this.listener = listener;

            AppDatabase db = AppDatabase.getAppDatabase(context);
            EventDao eventDao = db.eventDao();

            if (session != null) {
                events = eventDao.getAllBetween(session.getStart(), session.getEnd());
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_eventlist_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Event event = events.get(position);

            holder.event = event;

            holder.timestamp.setText(Util.getDateFormat().format(event.getTimestamp()));
            holder.type.setText(event.getTypeAsString());
            holder.description.setText(event.getPayload());

//            holder.description.setText(event.getDescription());
//            holder.message.setText(event.getExceptionMessage());
//            holder.stacktrace.setText(event.getStacktrace());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onEventListSelection(holder.event);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View mView;

            public TextView timestamp;
            public TextView type;
            public TextView description;
            public TextView message;
            public TextView stacktrace;

            public Event event;

            public ViewHolder(View view) {
                super(view);
                mView = view;

                timestamp = (TextView) view.findViewById(R.id.timestamp);
                type = (TextView) view.findViewById(R.id.type);
                description = (TextView) view.findViewById(R.id.description);
                message = (TextView) view.findViewById(R.id.message);
                stacktrace = (TextView) view.findViewById(R.id.stacktrace);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }
    }
}

