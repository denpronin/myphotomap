package com.pronin.myphotomap.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.text.DateFormat;
import java.util.Date;

public class Picture implements Parcelable {
    private final String path;
    private final transient LatLong latLong;
    private final String date;

    public Picture(String pathArg, float latArg, float longArg, String dateArg) {
        path = pathArg;
        latLong = new LatLong(latArg, longArg, false);
        date = DateFormat.getDateInstance().format(new Date(Long.parseLong(dateArg)*1000));
    }

    protected Picture(Parcel in) {
        path = in.readString();
        latLong = null;
        date = in.readString();
    }

    public static final Creator<Picture> CREATOR = new Creator<Picture>() {
        @Override
        public Picture createFromParcel(Parcel in) {
            return new Picture(in);
        }

        @Override
        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(path);
        dest.writeString(date);
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

