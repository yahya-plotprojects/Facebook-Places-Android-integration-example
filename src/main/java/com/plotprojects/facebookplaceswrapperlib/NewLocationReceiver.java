package com.plotprojects.facebookplaceswrapperlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.plotprojects.facebookplaceswrapperlib.util.BasicLockFactory;
import com.plotprojects.facebookplaceswrapperlib.util.LockFactory;
import com.plotprojects.retail.android.BaseTrigger;
import com.plotprojects.retail.android.Geotrigger;
import com.plotprojects.retail.android.NotificationTrigger;
import com.plotprojects.retail.android.Plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NewLocationReceiver extends BroadcastReceiver {
    private static final String LOCATION_KEY = "location";

    @Override
    public void onReceive(Context context, Intent intent) {
        FacebookSdk.setClientToken(context.getString(R.string.fb_client_token));

        PlacesWrapper wrapperInstance = PlacesWrapper.getInstance(context);
        Location location = intent.getParcelableExtra(LOCATION_KEY);
        wrapperInstance.setCachedLocation(location);
        BasicLockFactory lockFactory = BasicLockFactory.getInstance(context);
        PowerManager.WakeLock lock = lockFactory.createLock();
        lock.acquire(BasicLockFactory.LOCK_TIMEOUT);
        processFacebookCampaignsTask(context.getApplicationContext(), lockFactory, lock).execute();
    }

    private AsyncTask<Void, Void, Void> processFacebookCampaignsTask(final Context context,
                                                                     final LockFactory lockFactory,
                                                                     final PowerManager.WakeLock lock) {
        return new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    Collection<NotificationTrigger> loadedNotifications = Plot.getLoadedNotifications();
                    Collection<Geotrigger> loadedGeotriggers = Plot.getLoadedGeotriggers();
                    List<BaseTrigger> allCampaigns = new ArrayList<>();
                    allCampaigns.addAll(loadedNotifications);
                    allCampaigns.addAll(loadedGeotriggers);

                    CampaignsProcessor.process(allCampaigns, context, lock);
                } catch (Exception e) {
                    Log.e(Config.LOG_TAG, "Failed to load Facebook Places locations", e);
                } finally {
                    lockFactory.releaseLock(lock);
                }
                return null;
            }
        };
    }
}
