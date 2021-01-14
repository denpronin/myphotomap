package com.pronin.myphotomap.model;

public class Picture {
    private final String path;
    private final float latitude;
    private final float longitude;
    private final String date;

    public Picture(String pathArg, float latArg, float longArg, String dateArg) {
        path = pathArg;
        latitude = latArg;
        longitude = longArg;
        date = dateArg;
    }

    public String getDate() {
        return date;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Picture{" +
                "path='" + path + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", date='" + date + '\'' +
                '}';
    }
}

