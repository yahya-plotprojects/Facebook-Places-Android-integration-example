/*
 * Copyright (c) 2019 by Floating Market BV. Alle rechten voorbehouden.
 */

package com.plotprojects.facebookplaceswrapperlib.categories;

import android.os.AsyncTask;
import android.util.Log;

import com.plotprojects.facebookplaceswrapperlib.Config;
import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlace;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public final class FileCategoriesDao implements CategoriesDao {
    private static final String CATEGORIES_FILE = "https://files.plotprojects.com/facebook-places/facebook-categories-min.json";

    @Override
    public void isCategorySubCategoryOf(List<PlotFacebookPlace> isChildOf, String topCategory, CategoriesDaoCallback callback) {
        if (isChildOf.isEmpty()) {
            callback.isCategorySubCategoryOfResponse(null);
        } else if (topCategory == null || "".equals(topCategory)) {
            callback.isCategorySubCategoryOfResponse(isChildOf.get(0));
        } else {
            RequestTask requestTask = new RequestTask(isChildOf, topCategory, callback);
            requestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private static class RequestTask extends AsyncTask<Void, Void, PlotFacebookPlace> {
        private static JSONObject cachedCategories;

        private final List<PlotFacebookPlace> isChildOf;
        private final String topCategory;
        private final CategoriesDaoCallback callback;

        public RequestTask(List<PlotFacebookPlace> isChildOf, String topCategory, CategoriesDaoCallback callback) {
            this.isChildOf = isChildOf;
            this.topCategory = topCategory;
            this.callback = callback;
        }

        @Override
        protected PlotFacebookPlace doInBackground(Void... voids) {
            JSONObject categories = getCategories();
            if (categories != null) {
                CategorySearch categorySearch = new CategorySearch();
                for (PlotFacebookPlace place : isChildOf) {
                    for (String categoryId : place.getCategoryIds()) {
                        if (categorySearch.isCategorySubCategoryOf(categoryId, topCategory, categories)) {
                            return place;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(PlotFacebookPlace result) {
            try {
                callback.isCategorySubCategoryOfResponse(result);
            } catch (Exception e) {
                Log.e(Config.LOG_TAG, "Failed to handle categories", e);
            }
        }

        private static JSONObject getCategories() {
            if (cachedCategories == null) {
                String categoriesResult = downloadCategories();
                if (categoriesResult != null) {
                    try {
                        cachedCategories = new JSONObject(categoriesResult);
                    } catch (JSONException e) {
                        Log.e(Config.LOG_TAG, "Failed to parse categories", e);
                    }
                }
            }
            return cachedCategories;
        }

        private static String downloadCategories() {
            try {
                URL url = new URL(CATEGORIES_FILE);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");

                InputStream is = c.getInputStream();
                ByteArrayOutputStream resultStream = new ByteArrayOutputStream();

                try {
                    byte[] buffer = new byte[1024];
                    int readLength;

                    while (true) {
                        readLength = is.read(buffer);
                        if (readLength == -1) {
                            break;
                        }
                        resultStream.write(buffer, 0, readLength);
                    }
                } finally {
                    is.close();
                }

                return resultStream.toString("UTF-8");
            } catch (IOException e) {
                Log.e(Config.LOG_TAG, "Failed to download categories", e);
                return null;
            }
        }

    }

}
