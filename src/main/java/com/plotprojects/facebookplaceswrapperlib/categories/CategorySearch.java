/*
 * Copyright (c) 2019 by Floating Market BV. Alle rechten voorbehouden.
 */

package com.plotprojects.facebookplaceswrapperlib.categories;

import android.support.annotation.Nullable;
import android.util.Log;

import com.plotprojects.facebookplaceswrapperlib.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class CategorySearch {
    private static final String DATA_KEY = "data";
    private static final String API_ENUM_KEY = "api_enum";
    private static final String ID_KEY = "id";
    private static final String CATEGORIES_KEY = "fb_page_categories";
    private static final JSONArray EMPTY_ARRAY = new JSONArray();

    /**
     *
     * @param idIsChildOf category to check
     * @param topCategoryApiEnum category it needs to search in
     * @param categoriesWrapped all categories
     * @return true when isChildOf equals or is a sub-category of topCategory
     */
    public boolean isCategorySubCategoryOf(String idIsChildOf,
                                           String topCategoryApiEnum,
                                           JSONObject categoriesWrapped) {
        if (topCategoryApiEnum == null) {
            return true;
        } else if (idIsChildOf == null || categoriesWrapped == null) {
            return false;
        }

        try {
            JSONArray categories = categoriesWrapped.getJSONArray(DATA_KEY);

            JSONObject category = findTopCategory(topCategoryApiEnum, categories);
            if (category == null) {
                Log.e(Config.LOG_TAG, "Cannot find category: " + topCategoryApiEnum);
                return false;
            }
            return categoryContains(category, idIsChildOf);
        } catch (JSONException e) {
            Log.e(Config.LOG_TAG, "Failed to parse categories", e);
            return false;
        }
    }

    @Nullable
    private JSONObject findTopCategory(String topCategory, JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject category = array.getJSONObject(i);
            if (category.has(API_ENUM_KEY)) {
                String apiEnum = category.getString(API_ENUM_KEY);

                if (topCategory.equals(apiEnum)) {
                    return category;
                }

                JSONArray subCategories = (category.has(CATEGORIES_KEY)) ? category.getJSONArray(CATEGORIES_KEY) : EMPTY_ARRAY;
                JSONObject foundInSub = findTopCategory(topCategory, subCategories);
                if (foundInSub != null) {
                    return foundInSub;
                }
            }
        }
        return null;
    }

    private boolean categoryContains(JSONObject category, String isChildOf) throws JSONException {
        String idKey = category.getString(ID_KEY);
        if (idKey.equals(isChildOf)) {
            return true;
        }
        JSONArray subCategories = (category.has(CATEGORIES_KEY)) ? category.getJSONArray(CATEGORIES_KEY) : EMPTY_ARRAY;
        for (int i = 0; i < subCategories.length(); i++) {
            JSONObject subCategory = subCategories.getJSONObject(i);
            if (categoryContains(subCategory, isChildOf)) {
                return true;
            }
        }
        return false;
    }
}
