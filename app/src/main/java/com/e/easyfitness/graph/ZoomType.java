package com.e.easyfitness.graph;

public enum ZoomType {
    ZOOM_ALL,
    ZOOM_YEAR,
    ZOOM_MONTH,
    ZOOM_WEEK;

    public static ZoomType fromInteger(int x) {
        switch(x) {
            case 0:
                return ZOOM_ALL;
            case 1:
                return ZOOM_YEAR;
            case 2:
                return ZOOM_MONTH;
            case 3:
                return ZOOM_WEEK;
            default:
                return ZOOM_ALL;
        }
    }
}