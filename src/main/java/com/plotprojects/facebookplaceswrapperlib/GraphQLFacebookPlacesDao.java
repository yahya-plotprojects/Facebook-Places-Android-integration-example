package com.plotprojects.facebookplaceswrapperlib;


import android.location.Location;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.places.PlaceManager;
import com.facebook.places.model.CurrentPlaceRequestParams;
import com.facebook.places.model.PlaceFields;
import com.plotprojects.facebookplaceswrapperlib.categories.CategoriesDao;
import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlace;
import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlacesFilterProperties;
import com.plotprojects.facebookplaceswrapperlib.util.BasicLockFactory;
import com.plotprojects.facebookplaceswrapperlib.util.LockFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphQLFacebookPlacesDao implements PlacesDao {
    private static final String DATA_KEY = "data";

    private final CategoriesDao categoriesDao;
    private final LockFactory lockFactory;


    public GraphQLFacebookPlacesDao(LockFactory lockFactory, CategoriesDao categoriesDao) {
        this.lockFactory = lockFactory;
        this.categoriesDao = categoriesDao;
    }

    @Override
    public void retrieveCandidatePlaces(Set<PlotFacebookPlacesFilterProperties> filterProperties,
                                        Location location,
                                        PlacesAvailableCallback callback,
                                        PowerManager.WakeLock lock) {
        CurrentPlaceRequestParams.Builder builder = new CurrentPlaceRequestParams.Builder();
        builder.addField(PlaceFields.NAME);
        builder.addField(PlaceFields.CONFIDENCE_LEVEL);
        builder.addField(PlaceFields.LOCATION);
        builder.addField(PlaceFields.CATEGORY_LIST);
        builder.addField(PlaceFields.ID);
        builder.addField(PlaceFields.HOURS);

        PlotCurrentPlaceRequestCallback placeRequestCallback = new PlotCurrentPlaceRequestCallback(
                new ArrayList<>(filterProperties),
                lockFactory,
                location,
                callback,
                lock);
        PlaceManager.newCurrentPlaceRequest(builder.build(), placeRequestCallback);
    }

    private class PlotCurrentPlaceRequestCallback implements PlaceManager.OnRequestReadyCallback, GraphRequest.Callback {

        private final LockFactory lockFactory;
        private PlacesAvailableCallback callback;
        private List<PlotFacebookPlacesFilterProperties> filterProperties;
        private Location location;
        private PowerManager.WakeLock lock;

        public PlotCurrentPlaceRequestCallback(List<PlotFacebookPlacesFilterProperties> filterProperties,
                                               LockFactory lockFactory,
                                               Location location,
                                               PlacesAvailableCallback callback,
                                               PowerManager.WakeLock lock) {
            this.callback = callback;
            this.filterProperties = filterProperties;
            this.lockFactory = lockFactory;
            this.location = location;
            this.lock = lock;

            lock.acquire(BasicLockFactory.LOCK_TIMEOUT);
        }

        @Override
        public void onCompleted(GraphResponse response) {
            if (callback == null || lock == null) {
                return;
            }

            try {
                FacebookRequestError error = response.getError();
                if (error != null) {
                    Log.e(Config.LOG_TAG, error.getErrorMessage());

                    callback.onAvailable(Collections.<PlotFacebookPlacesFilterProperties, PlotFacebookPlace>emptyMap());
                    callback = null;
                } else {
                    lock.acquire(BasicLockFactory.LOCK_TIMEOUT);
                    unmarshallAndFilterPlacesResponse(response, new UnmarshallAndFilterCompleteCallback() {
                        @Override
                        public void onComplete(Map<PlotFacebookPlacesFilterProperties, PlotFacebookPlace> places) {
                            callback.onAvailable(places);
                            callback = null;
                            lockFactory.releaseLock(lock);
                        }
                    }, lock);
                }
            } catch (Exception e) {
                Log.e(Config.LOG_TAG, "Failed to parse Facebook Places response", e);
            } finally {
                if (lock != null) {
                    lockFactory.releaseLock(lock); //closing lock opened in Constructor
                }
            }
        }

        @Override
        public void onLocationError(PlaceManager.LocationError error) {
            Log.e(Config.LOG_TAG, error.toString());
            if (callback == null || lock == null) {
                return;
            }

            lockFactory.releaseLock(lock); //closing lock opened in Constructor

            callback.onAvailable(Collections.<PlotFacebookPlacesFilterProperties, PlotFacebookPlace>emptyMap());
            callback = null;
        }

        @Override
        public void onRequestReady(GraphRequest graphRequest) {
            graphRequest.setVersion(Config.FB_API_VERSION);
            graphRequest.setCallback(this);

            graphRequest.executeAsync();
        }

        private List<PlotFacebookPlace> parseJsonArray(JSONArray array) {
            try {
                List<PlotFacebookPlace> result = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject placeJSON = array.getJSONObject(i);
                    PlotFacebookPlace place = PlotFacebookPlace.fromJsonObject(placeJSON);
                    result.add(place);
                }
                return result;
            } catch (JSONException e) {
                Log.e(Config.LOG_TAG, "Failed to parse places", e);
                return Collections.emptyList();
            }
        }

        private void unmarshallAndFilterPlacesResponse(GraphResponse response,
                                                       final UnmarshallAndFilterCompleteCallback callback,
                                                       PowerManager.WakeLock lock) {
            final JSONObject jsonObject = response.getJSONObject();
            if (jsonObject != null) {
                List<PlotFacebookPlace> places = parseJsonArray(jsonObject.optJSONArray(DATA_KEY));

                filterPlaces(
                        places,
                        filterProperties,
                        callback,
                        new HashMap<PlotFacebookPlacesFilterProperties, PlotFacebookPlace>(),
                        lock);

            } else {
                callback.onComplete(new HashMap<PlotFacebookPlacesFilterProperties, PlotFacebookPlace>());
            }
        }

        private void filterPlaces(final List<PlotFacebookPlace> places,
                                  final List<PlotFacebookPlacesFilterProperties> filterPropertiesLeft,
                                  final UnmarshallAndFilterCompleteCallback callback,
                                  final Map<PlotFacebookPlacesFilterProperties, PlotFacebookPlace> result,
                                  final PowerManager.WakeLock lock) {
            lock.acquire(BasicLockFactory.LOCK_TIMEOUT);
            if (filterPropertiesLeft.isEmpty()) {
                callback.onComplete(result);
            } else {
                final PlotFacebookPlacesFilterProperties head = filterPropertiesLeft.get(0);
                final List<PlotFacebookPlacesFilterProperties> tail = filterPropertiesLeft.subList(1, filterPropertiesLeft.size());
                Log.d(Config.LOG_TAG, "Items left: " + tail.size());

                List<PlotFacebookPlace> candidates = new ArrayList<>();
                for (PlotFacebookPlace place : places) {
                    if (isCandidatePlace(place, head)) {
                        candidates.add(place);
                    }
                }

                categoriesDao.isCategorySubCategoryOf(candidates, head.getCategory(), new CategoriesDao.CategoriesDaoCallback() {
                    @Override
                    public void isCategorySubCategoryOfResponse(@Nullable PlotFacebookPlace firstChild) {
                        try {
                            if (firstChild != null) {
                                result.put(head, firstChild);
                            }

                            filterPlaces(places, tail, callback, result, lock);
                        } finally {
                            lock.release();
                        }
                    }
                });
            }
        }

        private boolean containsName(String name, String filterName) {
            return filterName.isEmpty() || name.toLowerCase().contains(filterName);
        }

        private boolean withinDistance(Location placeLocation) {
            return placeLocation != null && insideRange(placeLocation) && validAccuracy();
        }

        private boolean insideRange(Location placeLocation) {
            return location.distanceTo(placeLocation) < Config.MAX_PLACE_DISTANCE_METERS;
        }

        private boolean validAccuracy() {
            return location.getAccuracy() <= Config.MIN_ACCURACY_METERS;
        }


        private boolean isCandidatePlace(PlotFacebookPlace place, PlotFacebookPlacesFilterProperties filter) {
            return place.getConfidenceLevel().equals(filter.getMinConfidence())
                    && containsName(place.getName(), filter.getName()) && withinDistance(place.getLocation());
        }


    }
}

