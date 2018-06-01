package de.volzo.despat.userinterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.CaptureDao;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.support.Util;


public class SessionFragment extends Fragment {

    public static final String TAG = SessionFragment.class.getSimpleName();

    public static final String ARG_SESSION_ID       = "ARG_SESSION_ID";
    public static final String ACTION_HOMOGRAPHY    = "ACTION_HOMOGRAPHY";
    public static final String ACTION_ERRORS        = "ACTION_ERRORS";
    public static final String ACTION_DELETE        = "ACTION_DELETE";

    Context context;
    OnSessionActionSelectionListener listener;

    public SessionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_session, container, false);

        Bundle args = getArguments();
        final long sessionId = args.getLong(ARG_SESSION_ID);

        if (sessionId <= 0) {
            Log.e(TAG, "invalid session ID for detail view: " + sessionId);
            return null;
        }

        // TODO: move this to handler

        AppDatabase db = AppDatabase.getAppDatabase(context);
        SessionDao sessionDao = db.sessionDao();
        CaptureDao captureDao = db.captureDao();

        final Session session = sessionDao.getById(sessionId);

        ImageView ivCompressedPreview = (ImageView) view.findViewById(R.id.compressedpreview);
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvStart = (TextView) view.findViewById(R.id.start);
        TextView tvEnd = (TextView) view.findViewById(R.id.end);
        TextView tvDuration = (TextView) view.findViewById(R.id.duration);
        TextView tvNumberOfCaptures = (TextView) view.findViewById(R.id.numberOfCaptures);

        Bitmap bm = null;
        try {
            File f = session.getCompressedImage();
            if (f == null) throw new Exception("compressed image missing");
//            bm = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(f.getAbsolutePath()), 1200, 900);
            Glide.with(context).load(f.getAbsoluteFile()).into(ivCompressedPreview);
        } catch (Exception e) {
            Log.w(TAG, "compressed preview for session " + session.toString() + "could not be loaded");
            bm = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.missing_img), 1200, 900);
            Glide.with(context).load(R.drawable.missing_img).into(ivCompressedPreview);
        }
//        ivCompressedPreview.setImageBitmap(bm);

        tvName.setText(session.getSessionName());
        tvStart.setText(Util.getDateFormat().format(session.getStart()));
        tvEnd.setText(Util.getDateFormat().format(session.getEnd()));
        tvDuration.setText(Util.getHumanReadableTimediff(session.getStart(), session.getEnd(), true));

        view.findViewById(R.id.bt_homography).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSessionActionSelection(sessionId, ACTION_HOMOGRAPHY);
            }
        });

        view.findViewById(R.id.bt_errors).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSessionActionSelection(sessionId, ACTION_ERRORS);
            }
        });

        view.findViewById(R.id.bt_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSessionActionSelection(sessionId, ACTION_DELETE);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnSessionActionSelectionListener) {
            listener = (OnSessionActionSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnSessionActionSelectionListener {
        void onSessionActionSelection(long sessionId, String action);
    }
}
