package de.volzo.despat.support;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

public class AspectRatioImageView extends AppCompatImageView {

    private static final String TAG = AspectRatioImageView.class.getSimpleName();

    private Integer ratio_x = null;
    private Integer ratio_y = null;

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, int ratio_x, int ratio_y) {
        super(context);

        this.ratio_x = ratio_x;
        this.ratio_y = ratio_y;
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(Integer ratio_x, Integer ratio_y) {
        this.ratio_x = ratio_x;
        this.ratio_y = ratio_y;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (ratio_x == null || ratio_y == null) {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }

        if (this.ratio_x == 0 || this.ratio_y == 0) {
            Log.w(TAG, "initialized with default ratio 4:3");
            this.ratio_x = 4;
            this.ratio_y = 3;
        }

        int width = getMeasuredWidth();
        int height = (width * this.ratio_y) / this.ratio_x;
        setMeasuredDimension(width, height);
    }
}