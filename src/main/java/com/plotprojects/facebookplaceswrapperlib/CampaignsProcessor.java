package com.plotprojects.facebookplaceswrapperlib;

import android.content.Context;
import android.os.PowerManager;

import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlace;
import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlacesFilterProperties;
import com.plotprojects.retail.android.BaseTrigger;
import com.plotprojects.retail.android.Plot;
import com.plotprojects.retail.android.TriggerType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class CampaignsProcessor {
    private static final String NAME_KEY = "name";
    private static final String CATEGORY = "category";
    private static final String MIN_CONFIDENCE_KEY = "min_confidence";
    private static final int FB_PROPERTIES_COUNT = 3;
    private static final String PAYLOAD_CONFIDENCE = "confidence_level";
    private static final String PAYLOAD_NAME = "name";
    private static final String PAYLOAD_PLACE_ID = "id";
    private static final String PAYLOAD_LATITUDE = "latitude";
    private static final String PAYLOAD_LONGITUDE = "longitude";
    private static final String EXTERNAL = "external";

    private CampaignsProcessor() {
    }

    public static void process(List<BaseTrigger> notifications, Context context, PowerManager.WakeLock lock) {
        PlacesWrapper wrapperInstance = PlacesWrapper.getInstance(context);
        if (wrapperInstance.getCachedLocation() != null && validRun(wrapperInstance)) {
            Set<PlotFacebookPlacesFilterProperties> filterCriteria = new HashSet<>();
            List<BaseTrigger> facebookNotifications = new ArrayList<>();
            for (BaseTrigger notification : notifications) {
                if (isFacebookCandidateNotification(notification)) {
                    PlotFacebookPlacesFilterProperties properties = getFacebookNotificationFilters(notification);
                    facebookNotifications.add(notification);
                    filterCriteria.add(properties);
                }
            }
            if (!filterCriteria.isEmpty()) {
                BackgroundProcessingService.BackgroundServiceHandler backgroundServiceHandler = BackgroundProcessingService.showNotification(context);
                wrapperInstance.loadNearestPlaces(
                        filterCriteria,
                        wrapperInstance.getCachedLocation(),
                        getNearestPlacesCallback(facebookNotifications, backgroundServiceHandler),
                        lock);
            }
        }
        wrapperInstance.setLastRunInMillis(new Date().getTime());
    }

    private static boolean validRun(PlacesWrapper wrapperInstance) {
        Long lastRun = wrapperInstance.getLastRunInMillis();
        return lastRun == null || (new Date().getTime() - lastRun > Config.MIN_LAST_RUN_TIME_DIFF);
    }

    private static PlotFacebookPlacesFilterProperties getFacebookNotificationFilters(BaseTrigger notification) {
        Map<String, String> triggerProperties = notification.getTriggerProperties();
        return new PlotFacebookPlacesFilterProperties(triggerProperties.get(NAME_KEY),
                triggerProperties.get(CATEGORY),
                triggerProperties.get(MIN_CONFIDENCE_KEY));
    }

    private static boolean isFacebookCandidateNotification(BaseTrigger notification) {
        Map<String, String> triggerProperties = notification.getTriggerProperties();
        boolean containsKeys = triggerProperties.containsKey(NAME_KEY)
                && triggerProperties.containsKey(CATEGORY)
                && triggerProperties.containsKey(MIN_CONFIDENCE_KEY);
        return notification.getRegionType().equals(EXTERNAL)
                && triggerProperties.size() == FB_PROPERTIES_COUNT && containsKeys;
    }

    private static PlacesAvailableCallback getNearestPlacesCallback(final List<BaseTrigger> notifications,
                                                                    final BackgroundProcessingService.BackgroundServiceHandler handler) {
        return new PlacesAvailableCallback() {
            @Override
            public void onAvailable(Map<PlotFacebookPlacesFilterProperties, PlotFacebookPlace> placesResult) {
                Map<PlotFacebookPlacesFilterProperties, PlotFacebookPlace> places = new HashMap<>(placesResult);
                for (BaseTrigger notification : notifications) {
                    if (!isFacebookCandidateNotification(notification)) {
                        continue;
                    }

                    PlotFacebookPlacesFilterProperties filter = getFacebookNotificationFilters(notification);
                    PlotFacebookPlace place = places.get(filter);
                    if (place == null) {
                        continue;
                    }
                    places.remove(filter);

                    Map<String, String> payload = new HashMap<>();
                    payload.put(PAYLOAD_CONFIDENCE, place.getConfidenceLevel());
                    payload.put(PAYLOAD_NAME, place.getName());
                    payload.put(PAYLOAD_PLACE_ID, place.getId());
                    payload.put(PAYLOAD_LATITUDE, String.valueOf(place.getLocation().getLatitude()));
                    payload.put(PAYLOAD_LONGITUDE, String.valueOf(place.getLocation().getLongitude()));
                    for (Map.Entry<String, String> hour : place.getOpeningHours().entrySet()) {
                        payload.put(hour.getKey(), hour.getValue());
                    }

                    List<Map<String, String>> triggerProperties = new ArrayList<>();
                    triggerProperties.add(notification.getTriggerProperties());
                    Plot.externalRegionTrigger(triggerProperties, TriggerType.ENTER, payload);
                }
                handler.hideNotification();
            }
        };
    }
}
