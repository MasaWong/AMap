package mw.ankara.map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.model.LatLng;

/**
 * 地图位置的信息，方便与Intent交互
 *
 * @author masawong
 * @since 8/9/15
 */
public class MapLocation {

    public static final String PREF_NAME = "pref_map";

    public static final String ADDRESS = "address";

    public static final String CITY = "city";

    public static final String LATITUDE = "latitude";

    public static final String LONGITUDE = "longitude";

    public String address;

    public String city = "0571";

    public LatLng position;

    private long mLastLocatedTime;

    public MapLocation() {
    }

    public MapLocation(Intent intent) {
        readFromIntent(intent);
    }

    public boolean needRelocate() {
        // 定位时间如果超过1分钟了，重新定位一下
        return System.currentTimeMillis() - mLastLocatedTime > 60000l;
    }

    public void recordLocation(AMapLocation aLocation) {
        address = aLocation.getPoiName();
        city = aLocation.getCity();
        position = new LatLng(aLocation.getLatitude(), aLocation.getLongitude());
        mLastLocatedTime = System.currentTimeMillis();
    }

    public void readFromIntent(Intent intent) {
        address = intent.getStringExtra(ADDRESS);
        city = intent.getStringExtra(CITY);

        double latitude = intent.getDoubleExtra(LATITUDE, 0d);
        double longitude = intent.getDoubleExtra(LONGITUDE, 0d);
        position = new LatLng(latitude, longitude);
    }

    public void writeToIntent(Intent intent) {
        intent.putExtra(ADDRESS, address);
        intent.putExtra(CITY, city);
        intent.putExtra(LATITUDE, position.latitude);
        intent.putExtra(LONGITUDE, position.longitude);
    }

    protected SharedPreferences getDefaultSharedPreference(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean readFromPreference(Context context) {
        SharedPreferences preferences = getDefaultSharedPreference(context);
        if (preferences.contains(LATITUDE) && preferences.contains(LONGITUDE)) {
            address = preferences.getString(ADDRESS, "");

            city = preferences.getString(CITY, "");

            double latitude = preferences.getFloat(LATITUDE, -1);
            double longitude = preferences.getFloat(LONGITUDE, -1);
            position = new LatLng(latitude, longitude);

            return true;
        }

        return false;
    }

    public void writeToPreference(Context context) {
        SharedPreferences preferences = getDefaultSharedPreference(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(ADDRESS, address);
        editor.putString(CITY, city);
        editor.putFloat(LATITUDE, (float) position.latitude);
        editor.putFloat(LONGITUDE, (float) position.longitude);

        editor.apply();
    }
}
