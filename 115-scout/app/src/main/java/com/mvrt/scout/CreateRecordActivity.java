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
public class CreateRecordActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {

    public static final int NUM_PAGES = 2;
    public static final int NUM_PREGAME = 0;
    public static final int NUM_AUTO = 1;

    private DataCollectionFragment[] fragmentList;
    private int currentFragment;

    private SmoothPager smoothPager;

    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);

        fragmentList = new DataCollectionFragment[NUM_PAGES];
        fragmentList[NUM_PREGAME] = new PregameFragment();
        fragmentList[NUM_AUTO] = new AutoFragment();

        // Instantiate a ViewPager and a PagerAdapter.
        smoothPager = (SmoothPager) findViewById(R.id.pager);
        smoothPager.setOnPageChangeListener(this);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        smoothPager.setAdapter(pagerAdapter);

        setTitle(getTitleFromPosition(NUM_PREGAME));
    }


    public void startAuto(View view) {
        smoothPager.setCurrentItem(NUM_AUTO, true);
    }

    public String getTitleFromPosition(int i) {
        switch (i) {
            case NUM_PREGAME:
                return "Match " + ((ScoutBase)getApplication()).getScheduleManager().getCurrentMatchNo();
            case NUM_AUTO:
                return "Autonomous Scouting";
            default:
                return "Scouter 2014";
        }
    }

    @Override
    public void onPageSelected(int i) {

        fragmentList[currentFragment].getDataFromUI(); //sync data
        setTitle(getTitleFromPosition(i));
        currentFragment = i;

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

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position >= fragmentList.length)position = fragmentList.length - 1;
            currentFragment = position;
            return fragmentList[position];
        }

        @Override
        public int getCount() {
            return fragmentList.length;
        }
    }

}
