/*
 * Copyright (c) 2019 by Floating Market BV. Alle rechten voorbehouden.
 */

package com.plotprojects.facebookplaceswrapperlib.util;


import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.plotprojects.facebookplaceswrapperlib.Config;

public final class BasicLockFactory implements LockFactory {
    private static BasicLockFactory singleInstance;
    private final PowerManager pm;
    public static final long LOCK_TIMEOUT = 30;

    public static BasicLockFactory getInstance(Context context) {
        if (singleInstance == null) {
            singleInstance = new BasicLockFactory(context.getApplicationContext());
        }
        return singleInstance;
    }

    private BasicLockFactory(Context context) {
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public PowerManager.WakeLock createLock() {
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ":facebookplotlock");
    }

    @Override
    public void releaseLock(PowerManager.WakeLock lock) {
        try {
            lock.release();
        } catch (RuntimeException e) {
            Log.e(Config.LOG_TAG, "Failed to release lock", e);
        }

    }
}

