/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * Shows a listing of all the badges the user has earned. Right not it shows only
 * the earned badges, we can add an additional display flag to also display badges
 * the user has yet to unlock as well. This will show them what they're missing
 * which would be fun to see.
 * 
 * @date March 10, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class BadgesActivity extends Activity {
    private static final String TAG = "BadgesActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_BADGE_ARRAY_LIST_PARCEL = Foursquared.PACKAGE_NAME
        + ".BadgesActivity.EXTRA_BADGE_ARRAY_LIST_PARCEL";

    private GridView mBadgesGrid;
    private BadgeWithIconListAdapter mListAdapter;
    
    private StateHolder mStateHolder;
    

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.badges_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
        } else {
            mStateHolder = new StateHolder();
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(
                    EXTRA_BADGE_ARRAY_LIST_PARCEL)) {
                
                // Can't jump from ArrayList to Group, argh.
                ArrayList<Badge> badges = getIntent().getExtras().getParcelableArrayList(
                        EXTRA_BADGE_ARRAY_LIST_PARCEL);
                Group<Badge> group = new Group<Badge>();
                for (Badge it : badges) {
                    group.add(it);
                }
                mStateHolder.setBadges(group);
            } else {
                Log.e(TAG, "BadgesActivity requires a badge ArrayList pareclable in its intent extras.");
                finish();
                return;
            }
        }
        
        ensureUi();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (isFinishing()) {
            mListAdapter.removeObserver();
            unregisterReceiver(mLoggedOutReceiver);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mStateHolder;
    }

    private void ensureUi() {
        mBadgesGrid = (GridView)findViewById(R.id.badgesGrid);
        mListAdapter = new BadgeWithIconListAdapter(this,
                ((Foursquared)getApplication()).getRemoteResourceManager());
        mListAdapter.setGroup(mStateHolder.getBadges());
        mBadgesGrid.setAdapter(mListAdapter);
    }
    
    private static class StateHolder {
        private Group<Badge> mBadges;
        
        public StateHolder() {
            mBadges = new Group<Badge>();
        }
        
        public void setBadges(Group<Badge> badges) { 
            mBadges = badges;
        }
        
        public Group<Badge> getBadges() {
            return mBadges;
        }
    }
}
