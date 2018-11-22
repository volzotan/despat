package de.volzo.despat.userinterface;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.PowerManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.security.Permission;

import de.volzo.despat.R;
import de.volzo.despat.MainActivity;
import de.volzo.despat.SessionManager;
import de.volzo.despat.preferences.Config;

public class TourActivity extends AppCompatActivity {

    private static final String TAG = TourActivity.class.getSimpleName();

    private Context context;
    private Activity activity;

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnPrev, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = this;
        this.activity = this;

        Log.d(TAG, "TourActivity init");

        // Making notification bar transparent
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_tour);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnPrev = (Button) findViewById(R.id.btn_prev);
        btnNext = (Button) findViewById(R.id.btn_next);

        layouts = new int[]{
                R.layout.tour1,
                R.layout.tour2,
                R.layout.tour3,
                R.layout.tour4,
                R.layout.tour5,
                R.layout.tour6,
                R.layout.tour7,
        };

        addBottomDots(0);
        changeStatusBarColor();

        viewPagerAdapter = new ViewPagerAdapter();
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        viewPager.setAdapter(viewPagerAdapter);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(-1);
                if (current < 0) {
                    launchHomeScreen();
                } else {
                    // move to prev screen
                    viewPager.setCurrentItem(current);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });

        MainActivity.runInitializationTasks(context);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "TourActivity pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "TourActivity resume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "TourActivity stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TourActivity destroy");
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        Config.setFirstTimeLaunch(context, false);

        startActivity(new Intent(context, MainActivity.class));
        finish();
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // adjust button captions for first and last page
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
//                btnPrev.setVisibility(View.GONE);
            } else if (position == 0) {
                btnPrev.setText(getString(R.string.skip));
//                btnNext.setVisibility(View.VISIBLE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnPrev.setText(getString(R.string.prev));
//                btnPrev.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void changeStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MainActivity.PERMISSION_REQUEST_CODE: {
                boolean success = true;

                for (int i=0; i<grantResults.length; i++) {
                    // even though only still image permissions are required, video/audio is part of the
                    // permission package. But since android never displays the audio request, it is denied.
                    if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                        continue;
                    }

                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        success = false;
                        break;
                    }
                }

                if (success) {
                    Log.w(TAG, "permissions are granted by user");
                    setButtonStates(true, null);
                } else {
                    Log.w(TAG, "permissions denied by user");
                    setButtonStates(false, null);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.DOZE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setButtonStates(null, true);
            } else {
                setButtonStates(null, false);
            }
        } else {
            Log.wtf(TAG, "unknown request code: " + requestCode);
        }
    }

    public void setButtonStates(Boolean permissions, Boolean doze) {
        Button btPermissions = findViewById(R.id.btPermissions);
        Button btDoze = findViewById(R.id.btDoze);

        if (btPermissions != null) {
            int color = R.color.darkGrey;
            int text = R.string.grantPermission;

            if (permissions == null) {
                if (!MainActivity.checkPermissionsAreGiven(activity)) {
                    color = R.color.darkGrey;
                    text = R.string.grantPermission;
                } else {
                    color = R.color.success;
                    text = R.string.permissionGranted;
                }
            } else if (permissions == true) {
                color = R.color.success;
                text = R.string.permissionGranted;
            } else {
                color = R.color.error;
                text = R.string.notGranted;
            }

//          btPermissions.setBackgroundTintList(getResources().getColor(R.color.darkGrey, null));
            ViewCompat.setBackgroundTintList(btPermissions, ColorStateList.valueOf(getResources().getColor(color, null)));
            btPermissions.setText(text);
        }

        if (btDoze != null) {
            int color = R.color.darkGrey;
            int text = R.string.grantPermission;

            if (doze == null) {
                if (!MainActivity.checkWhitelistingForDoze(activity)) {
                    color = R.color.darkGrey;
                    text = R.string.grantException;
                } else {
                    color = R.color.success;
                    text = R.string.exceptionGranted;
                }
            } else if (doze == true) {
                color = R.color.success;
                text = R.string.exceptionGranted;
            } else {
                color = R.color.error;
                text = R.string.notGranted;
            }

            ViewCompat.setBackgroundTintList(btDoze, ColorStateList.valueOf(getResources().getColor(color, null)));
            btDoze.setText(text);
        }
    }

    public class ViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public ViewPagerAdapter() {}

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            switch (position) {
                case 0: {
                    ImageView imageView = findViewById(R.id.iv_tour1_bg);
                    Glide.with(context).load(R.drawable.hamburg).into(imageView);
                    break;
                }
                case 5: {
                    ImageView imageView = findViewById(R.id.iv_tour5);
                    Glide.with(context).load(R.drawable.tour5).into(imageView);
                }
            }

            setButtonStates(null, null);

            Button btPermissions = findViewById(R.id.btPermissions);
            Button btDoze = findViewById(R.id.btDoze);

            if (btPermissions != null) {
                btPermissions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!MainActivity.checkPermissionsAreGiven(activity)) {
                            MainActivity.requestPermissions(activity);
                        }
                    }
                });
            }

            if (btDoze != null) {
                btDoze.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!MainActivity.checkWhitelistingForDoze(activity)) {
                            MainActivity.whitelistAppForDoze(activity);
                        }
                    }
                });
            }

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}