/*
 * Copyright (c) 2019 by Floating Market BV. Alle rechten voorbehouden.
 */

package com.plotprojects.facebookplaceswrapperlib.util;

import android.os.PowerManager;

public interface LockFactory {
    PowerManager.WakeLock createLock();

    void releaseLock(PowerManager.WakeLock lock);
}
