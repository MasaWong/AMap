package mw.ankara.map;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;

/**
 * @author masawong
 * @since 11/14/15
 */
public class MapBaseActivity extends AppCompatActivity implements AMapLocationListener {

    protected static final int DEFAULT_SCALE_LEVEL = 17;

    /**
     * part of map
     */
    protected MapView mMvMap;

    protected AMap mAMap;

    protected LocationManagerProxy mAMapLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupMapView(savedInstanceState);
    }

    protected int getLayoutResource() {
        return R.layout.activity_map_base;
    }

    /**
     * 设置地图的一些操作
     */
    protected void setupMapView(Bundle savedInstanceState) {
        mMvMap = (MapView) findViewById(R.id.map);
        mMvMap.onCreate(savedInstanceState);

        // 初始化AMap对象
        mAMap = mMvMap.getMap();

        // 设置默认定位按钮是否显示
        mAMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void moveCamera(boolean animated, CameraUpdate update) {
        if (animated) {
            mAMap.animateCamera(update, 1000, null);
        } else {
            mAMap.moveCamera(update);
        }
    }

    protected void moveCamera(boolean animated, LatLng location) {
        moveCamera(animated, CameraUpdateFactory.newCameraPosition(
                new CameraPosition(location, DEFAULT_SCALE_LEVEL, 0, 0)));
    }

    protected void moveCamera(boolean animated, MapLocation location) {
        moveCamera(animated, CameraUpdateFactory.newCameraPosition(
                new CameraPosition(location.position, DEFAULT_SCALE_LEVEL, 0, 0)));
    }

    /**
     * 激活定位
     */
    public void activateLocating() {
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this);
        }

        // API定位采用GPS和网络混合定位方式
        // 第一个参数是定位provider，第二个参数时间最短是2000毫秒，-1代表一次性定位
        // 第三个参数距离间隔单位是米，第四个参数是定位监听者
        mAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 10, this);
    }

    /**
     * 停止定位
     */
    public void deactivateLocating() {
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aLocation) {
        // TODO
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMvMap.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMvMap.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMvMap.onDestroy();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMvMap.onSaveInstanceState(outState);
    }

    // useless
    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
