/*
 * Copyright (c) 2019 by Floating Market BV. Alle rechten voorbehouden.
 */

package com.plotprojects.facebookplaceswrapperlib.model;

public class PlotFacebookPlacesFilterProperties {
    private String name;
    private String category;
    private String minConfidence;

    public PlotFacebookPlacesFilterProperties(String name, String category, String minConfidence) {
        this.name = name.toLowerCase();
        this.category = category;
        this.minConfidence = minConfidence;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getMinConfidence() {
        return minConfidence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlotFacebookPlacesFilterProperties that = (PlotFacebookPlacesFilterProperties) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (category != null ? !category.equals(that.category) : that.category != null)
            return false;
        return minConfidence != null ? minConfidence.equals(that.minConfidence) : that.minConfidence == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (minConfidence != null ? minConfidence.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlotFacebookPlacesFilterProperties{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", minConfidence='" + minConfidence + '\'' +
                '}';
    }

}
