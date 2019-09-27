package com.plotprojects.facebookplaceswrapperlib;

import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlace;
import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlacesFilterProperties;

import java.util.Map;

interface UnmarshallAndFilterCompleteCallback {
    void onComplete(Map<PlotFacebookPlacesFilterProperties, PlotFacebookPlace> places);
}
