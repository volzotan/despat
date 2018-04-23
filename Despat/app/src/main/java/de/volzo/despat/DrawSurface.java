package de.volzo.despat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class DrawSurface extends SurfaceView {

    public static final String TAG = SurfaceView.class.getSimpleName();

    private Paint paint1              = null;
    private Paint paint2              = null;
    private SurfaceHolder holder      = null;
    private final Context context;

    private float startX  = 0;
    private float startY  = 0;
    private float stopX   = 0;
    private float stopY   = 0;

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
        holder.setFormat(PixelFormat.TRANSPARENT);

        this.setZOrderOnTop(true);

        paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setColor(Color.GREEN);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(4.0f);
        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setColor(Color.RED);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(4.0f);
    }

    public void clearCanvas() {

    }

    public void drawBoxes(Size referenceFrame, List<RectF> rectangles, boolean clearBeforeDraw) throws Exception {

        if (!holder.getSurface().isValid()) {
            throw new Exception("surface not valid");
        }

        Canvas canvas = holder.lockCanvas();

        Log.i(TAG, "canvas size: " + canvas.getWidth() + " x " + canvas.getHeight());

        Matrix mat = new Matrix();
        mat.setScale((float) canvas.getWidth() / (float) referenceFrame.getWidth(), (float) canvas.getHeight() / (float) referenceFrame.getHeight());

        if (canvas == null) {
            throw new Exception("canvas not valid");
        }

        if (clearBeforeDraw) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        for (RectF rect : rectangles) {
            mat.mapRect(rect);
            canvas.drawRect(rect, paint1);
        }

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            invalidate();
            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    startX = event.getX();
                    startY = event.getY();

                    canvas.drawCircle(event.getX(), event.getY(), 100, paint1);
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

                    canvas.drawCircle(event.getX(), event.getY(), 100, paint2);
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
                    canvas.drawRect(r, paint1);

                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        return false;
    }
}
