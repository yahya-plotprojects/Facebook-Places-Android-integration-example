/*
 * Copyright (c) 2019 by Floating Market BV. Alle rechten voorbehouden.
 */

package com.plotprojects.facebookplaceswrapperlib.model;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlotFacebookPlace {
    private static final String NAME_KEY = "name";
    private static final String ID_KEY = "id";
    private static final String LOCATION_KEY = "location";
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";
    private static final String CONFIDENCE_KEY = "confidence_level";
    private static final String OPENING_HOURS_KEY = "hours";
    private static final String DAY_KEY = "key";
    private static final String HOUR_KEY = "value";
    private static final String LOCATION_PROVIDER = "FacebookPlaces";
    private static final String CATEGORY_LIST_KEY = "category_list";

    private String name;
    private String id;
    private String confidenceLevel;
    private Location location;
    private Map<String, String> openingHours;
    private Set<String> categoryIds;

    private PlotFacebookPlace(String name,
                              String id,
                              String confidenceLevel,
                              Location location,
                              Map<String, String> openingHours,
                              Set<String> categoryIds) {
        this.name = name;
        this.id = id;
        this.confidenceLevel = confidenceLevel;
        this.location = location;
        this.openingHours = openingHours;
        this.categoryIds = categoryIds;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getConfidenceLevel() {
        return confidenceLevel.toLowerCase();
    }

    public Location getLocation() {
        return location;
    }

    public Map<String, String> getOpeningHours() {
        return openingHours;
    }

    public Set<String> getCategoryIds() {
        return categoryIds;
    }

    public static PlotFacebookPlace fromJsonObject(JSONObject json) {
        String name = json.optString(NAME_KEY);
        String id = json.optString(ID_KEY);
        JSONObject locationJson = json.optJSONObject(LOCATION_KEY);
        Location location = new Location(LOCATION_PROVIDER);
        if (locationJson != null) {
            location.setLatitude(locationJson.optDouble(LATITUDE_KEY));
            location.setLongitude(locationJson.optDouble(LONGITUDE_KEY));
        }
        String confidence = json.optString(CONFIDENCE_KEY);
        Map<String, String> openingHours = new HashMap<>();
        JSONArray hours = json.optJSONArray(OPENING_HOURS_KEY);
        if (hours != null) {
            for (int i = 0; i < hours.length(); i++) {
                JSONObject hour = hours.optJSONObject(i);
                String day = hour.optString(DAY_KEY);
                if (!day.isEmpty()) {
                    String time = hour.optString(HOUR_KEY);
                    openingHours.put(day, time);
                }
            }
        }
        Set<String> categoryIds = new HashSet<>();
        JSONArray categories = json.optJSONArray(CATEGORY_LIST_KEY);
        if (categories != null) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject cat = categories.optJSONObject(i);
                categoryIds.add(cat.optString("id"));
            }
        }

        return new PlotFacebookPlace(name, id, confidence, location, openingHours, categoryIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlotFacebookPlace that = (PlotFacebookPlace) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (confidenceLevel != null ? !confidenceLevel.equals(that.confidenceLevel) : that.confidenceLevel != null)
            return false;
        if (location != null ? !location.equals(that.location) : that.location != null)
            return false;
        if (openingHours != null ? !openingHours.equals(that.openingHours) : that.openingHours != null)
            return false;
        return categoryIds != null ? categoryIds.equals(that.categoryIds) : that.categoryIds == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (confidenceLevel != null ? confidenceLevel.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (openingHours != null ? openingHours.hashCode() : 0);
        result = 31 * result + (categoryIds != null ? categoryIds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlotFacebookPlace{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", confidenceLevel='" + confidenceLevel + '\'' +
                ", location=" + location +
                ", openingHours=" + openingHours +
                ", categoryIds=" + categoryIds +
                '}';
    }
}
