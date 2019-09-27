/*
 * Copyright (c) 2019 by Floating Market BV. Alle rechten voorbehouden.
 */

package com.plotprojects.facebookplaceswrapperlib.categories;

import android.support.annotation.Nullable;

import com.plotprojects.facebookplaceswrapperlib.model.PlotFacebookPlace;

import java.util.List;

public interface CategoriesDao {

    void isCategorySubCategoryOf(List<PlotFacebookPlace> isChildOf,
                                 String topCategory,
                                 CategoriesDaoCallback callback);

    interface CategoriesDaoCallback {
        void isCategorySubCategoryOfResponse(@Nullable PlotFacebookPlace firstChild);
    }
}
