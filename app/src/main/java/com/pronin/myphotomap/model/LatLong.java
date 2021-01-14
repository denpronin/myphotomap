package com.pronin.myphotomap.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LatLong {
    private float latitude;
    private float longitude;

    public LatLong() {}

    public LatLong(float latArg, float longArg) {
        latitude = new BigDecimal(latArg).setScale(2, RoundingMode.HALF_UP).floatValue();
        longitude = new BigDecimal(longArg).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LatLong latLong = (LatLong) o;
        return latitude == latLong.getLatitude() && longitude == latLong.getLongitude();
    }

    @Override
    public int hashCode() {
        int result = 13;
        result = 37 * result + Float.floatToIntBits(latitude);
        result = 37 * result + Float.floatToIntBits(longitude);
        return result;
    }
}
