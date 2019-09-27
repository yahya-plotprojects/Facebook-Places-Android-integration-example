package com.plotprojects.facebookplaceswrapperlib;

import android.content.Context;
import android.location.Location;
import android.os.PowerManager;

import com.plotprojects.facebookplaceswrapperlib.categories.CategoriesDao;
import com.plotprojects.facebookplaceswrapperlib.categories.FileCategoriesDao;
import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlacesFilterProperties;
import com.plotprojects.facebookplaceswrapperlib.util.BasicLockFactory;
import com.plotprojects.facebookplaceswrapperlib.util.LockFactory;

import java.util.Set;

class PlacesWrapper {
    private static PlacesWrapper singleInstance = null;

    private PlacesDao placesDao;
    private Location cachedLocation;
    private Long lastRunInMillis;

    private PlacesWrapper(Context context) {
        CategoriesDao categoriesDao = new FileCategoriesDao();
        LockFactory lockFactory = BasicLockFactory.getInstance(context);
        placesDao = new GraphQLFacebookPlacesDao(lockFactory, categoriesDao);
    }

    public static PlacesWrapper getInstance(Context context) {
        if (singleInstance == null) {
            singleInstance = new PlacesWrapper(context);
        }
        return singleInstance;
    }

    public Location getCachedLocation() {
        return cachedLocation;
    }

    public void setCachedLocation(Location cachedLocation) {
        this.cachedLocation = cachedLocation;
    }

    public Long getLastRunInMillis() {
        return lastRunInMillis;
    }

    public void setLastRunInMillis(Long lastRunInMillis) {
        this.lastRunInMillis = lastRunInMillis;
    }

    public void loadNearestPlaces(Set<PlotFacebookPlacesFilterProperties> filterProperties, Location location, PlacesAvailableCallback callback, PowerManager.WakeLock lock) {
        placesDao.retrieveCandidatePlaces(filterProperties, location, callback, lock);
    }
}
