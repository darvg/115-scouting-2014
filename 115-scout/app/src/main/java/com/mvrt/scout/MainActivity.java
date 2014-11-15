package com.mvrt.scout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

/**
 * Created by lee on 10/22/14.
 */
public class MainActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {
    public static final int NUM_PAGES = 2;
    public static final int NUM_MAIN = 0;
    public static final int NUM_AUTO = 1;

    private SmoothPager smoothPager;

    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);

            // Instantiate a ViewPager and a PagerAdapter.
            smoothPager = (SmoothPager) findViewById(R.id.pager);
            smoothPager.setOnPageChangeListener(this);
            pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            smoothPager.setAdapter(pagerAdapter);
        }

        @Override
        public void onBackPressed() {
            if (smoothPager.getCurrentItem() == 0) {
                // If the user is currently looking at the first step, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            smoothPager.setCurrentItem(smoothPager.getCurrentItem() - 1, true);
        }
    }

    public void startAuto(View view) {
        smoothPager.setCurrentItem(NUM_AUTO, true);
    }

    public String getTitleFromPosition(int i) {
        switch (i) {
            case 0:
                return "Scouter 2014";
            case 1:
                return "Autonomous Scouting";
            default:
                return "Scouter 2014";
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        setTitle(getTitleFromPosition(i));
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case NUM_MAIN:
                    return new MainFragment();
                case NUM_AUTO:
                    return new AutoFragment();
                default:
                    return new MainFragment();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
