package de.volzo.despat.userinterface;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import de.volzo.despat.R;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;

public class DrawSurface extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = DrawSurface.class.getSimpleName();

    private DrawSurfaceCallback onReadyCallback;
    private boolean interactive = false;

    public Paint paintBlack = null;
    public Paint paintMain = null;
    public Paint paintRed = null;
    private SurfaceHolder holder = null;
    private final Context context;

    List<RectF> rectanglesBlack = new ArrayList<RectF>();
    List<RectF> rectanglesMain = new ArrayList<RectF>();
    List<RectF> rectanglesRed = new ArrayList<RectF>();

    private float startX = 0;
    private float startY = 0;
    private float stopX = 0;
    private float stopY = 0;

    public DrawSurface(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public DrawSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public DrawSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {

        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);

        this.setZOrderOnTop(true);
        this.setWillNotDraw(false);

        paintBlack = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBlack.setColor(Color.BLACK);
        paintBlack.setStyle(Paint.Style.STROKE);
        paintBlack.setStrokeWidth(1.0f);

        paintMain = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMain.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        paintMain.setStyle(Paint.Style.STROKE);
        paintMain.setAlpha(255/10);
        paintMain.setStrokeWidth(2.0f);

        paintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRed.setColor(Color.RED);
        paintRed.setStyle(Paint.Style.STROKE);
        paintRed.setStrokeWidth(2.0f);
    }

    public void setCallback(DrawSurfaceCallback onReadyCallback) {
        this.onReadyCallback = onReadyCallback;

        if (holder.getSurface().isValid()) onReadyCallback.onSurfaceReady(this);
    }

    public void setInteractive(boolean interative) {
        this.interactive = interative;
    }

    public void clearCanvas() throws Exception {
        if (!holder.getSurface().isValid()) {
            throw new Exception("surface not valid");
        }

            Canvas canvas = holder.lockCanvas();

        if (canvas == null) {
            throw new Exception("canvas not valid");
        }

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        holder.unlockCanvasAndPost(canvas);
    }

    public void redraw() {
        invalidate();
    }

    /**
     * Beware: modifies rectangles coords in place
     */
    public void addBoxes(Size referenceFrame, List<RectF> rectangles, Paint paint) throws Exception {

        if (!holder.getSurface().isValid()) {
            throw new Exception("surface not valid");
        }

        Canvas canvas = holder.lockCanvas();

        if (canvas == null) {
            throw new Exception("canvas not valid");
        }

        if (referenceFrame == null || referenceFrame.getHeight() == 0 || referenceFrame.getWidth() == 0) {
            throw new Exception("reference frame not valid (either 0 or null");
        }

        Matrix mat = new Matrix();
        mat.setScale((float) canvas.getWidth() / (float) referenceFrame.getWidth(), (float) canvas.getHeight() / (float) referenceFrame.getHeight());

        holder.unlockCanvasAndPost(canvas);

        for (RectF r : rectangles) {
            mat.mapRect(r);
        }

        if (paint == paintBlack) {
            rectanglesBlack.addAll(rectangles);
        } else if (paint == paintMain) {
            rectanglesMain.addAll(rectangles);
        } else if (paint == paintRed) {
            rectanglesRed.addAll(rectangles);
        } else {
            // defaultColor
            rectanglesMain.addAll(rectangles);
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (RectF rect : rectanglesBlack) {
            canvas.drawRect(rect, paintBlack);
        }

        for (RectF rect : rectanglesMain) {
            canvas.drawRect(rect, paintMain);
        }

        for (RectF rect : rectanglesRed) {
            canvas.drawRect(rect, paintRed);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!interactive) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            invalidate();
            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    startX = event.getX();
                    startY = event.getY();

                    canvas.drawCircle(event.getX(), event.getY(), 100, paintMain);
                    holder.unlockCanvasAndPost(canvas);
                    return true;
                }
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    stopX = event.getX();
                    stopY = event.getY();

                    canvas.drawCircle(event.getX(), event.getY(), 100, paintRed);
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    // reset canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    Rect r = new Rect((int) startX, (int) startY, (int) event.getX(), (int) event.getY());
                    canvas.drawRect(r, paintMain);

                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (onReadyCallback != null) {
            onReadyCallback.onSurfaceReady(this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public static class DrawSurfaceCallback {

        public DrawSurfaceCallback() {
        }

        public void onSurfaceReady(DrawSurface surface) {

        }
    }
}