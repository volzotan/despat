package de.volzo.despat.userinterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.ErrorEvent;
import de.volzo.despat.persistence.ErrorEventDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;

public class ErrorEventListFragment extends Fragment {

    private static final String TAG = ErrorEventListFragment.class.getSimpleName();

    private OnErrorEventListSelectionListener listener;
    private Session session;

    public ErrorEventListFragment() {

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

        View view = inflater.inflate(R.layout.fragment_erroreventlist, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new ErrorEventRecyclerViewAdapter(getActivity(), listener));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnErrorEventListSelectionListener) {
            listener = (OnErrorEventListSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnErrorEventListSelectionListener {
        void onErrorEventListSelection(ErrorEvent errorEvent);
    }

    class ErrorEventRecyclerViewAdapter extends RecyclerView.Adapter<ErrorEventRecyclerViewAdapter.ViewHolder> {

        public final String TAG = ErrorEventRecyclerViewAdapter.class.getSimpleName();

        private Context context;
        private final ErrorEventListFragment.OnErrorEventListSelectionListener listener;
        private List<ErrorEvent> errorEvents;

        public ErrorEventRecyclerViewAdapter(Context context, ErrorEventListFragment.OnErrorEventListSelectionListener listener) {

            this.context = context;
            this.listener = listener;

            AppDatabase db = AppDatabase.getAppDatabase(context);
            ErrorEventDao errorEventDao = db.errorEventDao();

            if (session != null) {
                errorEvents = errorEventDao.getAllBySession(session.getId());
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_erroreventlist_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            ErrorEvent errorEvent = errorEvents.get(position);

            holder.errorEvent = errorEvent;

            holder.timestamp.setText(Util.getDateFormat().format(errorEvent.getTimestamp()));
            holder.type.setText(errorEvent.getType());
            holder.description.setText(errorEvent.getDescription());
            holder.message.setText(errorEvent.getExceptionMessage());
            holder.stacktrace.setText(errorEvent.getStacktrace());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onErrorEventListSelection(holder.errorEvent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return errorEvents.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View mView;

            public TextView timestamp;
            public TextView type;
            public TextView description;
            public TextView message;
            public TextView stacktrace;

            public ErrorEvent errorEvent;

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

