package com.pronin.myphotomap.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LatLong {
    private final float latitude;
    private final float longitude;

    public LatLong(float latArg, float longArg, boolean isRounded) {
        if (isRounded) {
            latitude = latArg;
            longitude = longArg;
        } else {
            latitude = new BigDecimal(latArg).setScale(2, RoundingMode.HALF_UP).floatValue();
            longitude = new BigDecimal(longArg).setScale(2, RoundingMode.HALF_UP).floatValue();
        }
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
        result = 31 * result + Float.floatToIntBits(latitude);
        result = 31 * result + Float.floatToIntBits(longitude);
        return result;
    }

    @Override
    public String toString() {
        return "LatLong{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
