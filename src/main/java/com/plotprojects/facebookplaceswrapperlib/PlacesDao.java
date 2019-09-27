package com.plotprojects.facebookplaceswrapperlib;

import android.location.Location;
import android.os.PowerManager;

import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlacesFilterProperties;

import java.util.Set;

public interface PlacesDao {

    void retrieveCandidatePlaces(Set<PlotFacebookPlacesFilterProperties> filterProperties,
                                 Location location,
                                 PlacesAvailableCallback callback,
                                 PowerManager.WakeLock lock);
}
