package de.volzo.despat.userinterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.preferences.Config;

public class MagnifierSurface extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = MagnifierSurface.class.getSimpleName();

    private SurfaceHolder holder    = null;
    private final Context context;

    private Bitmap image;

    private List<Point> prevMarker;

    private Paint bitmapPaint;
    private Paint linePaint;
    private Paint linePaint2;
    private Paint markerPaint;

    private float posX = 0.5f;
    private float posY = 0.5f;

    public MagnifierSurface(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MagnifierSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MagnifierSurface(Context context, AttributeSet attrs, int defStyle) {
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

        bitmapPaint = new Paint();
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(4.0f);
        linePaint2 = new Paint();
        linePaint2.setColor(Color.WHITE);
        linePaint2.setStrokeWidth(2.0f);
        markerPaint = new Paint();
        markerPaint.setColor(Color.RED);
        markerPaint.setStrokeWidth(3.0f);

        prevMarker = new ArrayList<>();
        prevMarker.add(new Point(1000, 1000));
        prevMarker.add(new Point(2000, 1500));
    }

    public void setImage(File path) {
        image = BitmapFactory.decodeFile(path.getAbsolutePath());
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

    public void move(float x, float y) {
//        Log.d(TAG, "" + Float.toString(x) + " : " + Float.toString(y));

        posX = x;
        posY = y;

        invalidate();
    }

    public Point getPosition() {
        return new Point((int) (image.getWidth()*posX), (int) (image.getHeight()*posY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (image == null) return;

        canvas.drawARGB(255, 0, 0, 0);

        canvas.drawBitmap(image, (-1 * posX * image.getWidth()) + canvas.getWidth()/2, (-1 * posY * image.getHeight()) + canvas.getHeight()/2, bitmapPaint);

        canvas.drawLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight(), linePaint);
        canvas.drawLine(canvas.getWidth() / 2 +1, 0, canvas.getWidth() / 2 +1, canvas.getHeight(), linePaint2);
        canvas.drawLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2, linePaint);
        canvas.drawLine(0, canvas.getHeight() / 2 +1, canvas.getWidth(), canvas.getHeight() / 2 +1, linePaint2);

        // TODO: draw marker
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        invalidate();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
