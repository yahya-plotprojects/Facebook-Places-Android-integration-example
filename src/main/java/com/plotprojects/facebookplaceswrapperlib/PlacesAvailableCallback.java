package com.plotprojects.facebookplaceswrapperlib;

import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlace;
import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlacesFilterProperties;

import java.util.Map;

public interface PlacesAvailableCallback {
    void onAvailable(Map<PlotFacebookPlacesFilterProperties, PlotFacebookPlace> places);
}
