/*
 * Copyright (c) 2019 by Floating Market BV. Alle rechten voorbehouden.
 */

package com.plotprojects.facebookplaceswrapperlib.categories;


import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class CategorySearchTest {
    private CategorySearch categorySearch;
    private JSONObject tree;

    @Before
    public void before() throws IOException, JSONException {
        categorySearch = new CategorySearch();

        readTree();
    }

    @Test
    public void testNotChild() {
        String id = "188166377871384"; //Armed forces
        boolean result = categorySearch.isCategorySubCategoryOf(id,"ACTIVITY_GENERAL", tree);

        assertFalse(result);
    }

    @Test
    public void testSameNode() {
        String id = "123377808095874"; //MARKETING_AGENCY
        boolean result = categorySearch.isCategorySubCategoryOf(id,"MARKETING_AGENCY", tree);

        assertTrue(result);
    }

    @Test
    public void testChildNode() {
        String id = "1946678192225672"; //DAIRY_FARM
        boolean result = categorySearch.isCategorySubCategoryOf(id,"AGRICULTURE", tree);

        assertTrue(result);
    }

    private void readTree() throws IOException, JSONException {
        Context context = ApplicationProvider.getApplicationContext();

        StringBuilder sb = new StringBuilder();
        InputStream is = context.getAssets().open("categories.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8 ));
        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }
        br.close();
        tree = new JSONObject(sb.toString());
    }
}
