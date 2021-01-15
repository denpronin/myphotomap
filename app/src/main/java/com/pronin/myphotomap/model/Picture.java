package com.pronin.myphotomap.model;

public class Picture {
    private final String path;
    private final LatLong latLong;
    private final String date;

    public Picture(String pathArg, float latArg, float longArg, String dateArg) {
        path = pathArg;
        latLong = new LatLong(latArg, longArg, false);
        date = dateArg;
    }

    public String getDate() {
        return date;
    }

    public LatLong getLatLong() {
        return latLong;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Picture{" +
                "path='" + path + '\'' +
                ", latitude=" + latLong.getLatitude() +
                ", longitude=" + latLong.getLongitude() +
                ", date='" + date + '\'' +
                '}';
    }
}

