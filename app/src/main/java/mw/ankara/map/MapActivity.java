package mw.ankara.map;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.overlay.PoiOverlay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.List;

/**
 * 地图页面
 *
 * @author masawong
 * @since 8/6/15
 */
public class MapActivity extends AppCompatActivity implements AMapLocationListener {

    private static final int DEFAULT_SCALE_LEVEL = 17;

    private AppCompatAutoCompleteTextView mAcactvSearch;

    private Inputtips mInputTips;

    private ArrayAdapter<String> mPoiTipAdapter;

    private MapView mMvMap;

    private AMap mAMap;

    private LocationManagerProxy mAMapLocationManager;

    private MapLocation mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setupSearchView();

        mMvMap = (MapView) findViewById(R.id.map_mv_map);
        mMvMap.onCreate(savedInstanceState);
        if (mAMap == null) {
            setupMapView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.confirm) {
            Intent intent = new Intent();
            mLastLocation.address = mAcactvSearch.getText().toString();
            mLastLocation.writeToIntent(intent);

            setResult(RESULT_OK, intent);
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置查询的操作
     */
    private void setupSearchView() {
        mAcactvSearch = (AppCompatAutoCompleteTextView) findViewById(R.id.map_acactv_search);

        // 自动补全部分
        mPoiTipAdapter = new ArrayAdapter<>(this, R.layout.item_poi_tip);
        mAcactvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                doPoiSearch(mPoiTipAdapter.getItem(position), "");
            }
        });

        // 高德的提示选择
        mInputTips = new Inputtips(this, new Inputtips.InputtipsListener() {
            @Override
            public void onGetInputtips(List<Tip> tipList, int rCode) {
                if (rCode == 0) {// 正确返回
                    mPoiTipAdapter.clear();

                    for (int i = 0; i < tipList.size(); i++) {
                        mPoiTipAdapter.add(tipList.get(i).getName());
                    }

                    mAcactvSearch.setAdapter(mPoiTipAdapter);
                    mPoiTipAdapter.notifyDataSetChanged();
                }
            }
        });

        // 输入监听
        mAcactvSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString().trim();
                try {
                    // 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号
                    mInputTips.requestInputtips(newText, mLastLocation.city);
                } catch (AMapException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 设置地图的一些操作
     */
    private void setupMapView() {
        // 初始化AMap对象
        mAMap = mMvMap.getMap();
        mAMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                onMarkerClicked(marker);
                return false;
            }
        });

        // 设置默认定位按钮是否显示
        mAMap.getUiSettings().setMyLocationButtonEnabled(false);

        // 显示上一次的位置，缩放到17级，缩放级别4~20
        mLastLocation = new MapLocation();
        if (mLastLocation.readFromPreference(this)) {
            moveCamera(false, mLastLocation);
        } else {
            moveCamera(false, CameraUpdateFactory.zoomTo(DEFAULT_SCALE_LEVEL));
        }
    }

    private void onMarkerClicked(Marker marker) {
        mLastLocation.address = marker.getTitle();
        mLastLocation.position = marker.getPosition();

        mAcactvSearch.setText(marker.getTitle());
    }

    /**
     * 开始进行poi搜索
     */
    protected void doPoiSearch(String keyword, String city) {
        hideSoftwareInput();

        int currentPage = 0;
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", city);
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int rCode) {
                if (rCode == 0 && poiResult != null) {
                    // 取得第一页的poiitem数据，页数从数字0开始
                    List<PoiItem> poiItems = poiResult.getPois();
                    if (poiItems != null && !poiItems.isEmpty()) {
                        mAMap.clear();// 清理之前的图标

                        PoiOverlay poiOverlay = new PoiOverlay(mAMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    }
                } else {
                    Toast.makeText(MapActivity.this, R.string.network_error, Toast.LENGTH_SHORT)
                        .show();
                }
            }

            @Override
            public void onPoiItemDetailSearched(PoiItemDetail poiItemDetail, int i) {
            }
        });
        poiSearch.searchPOIAsyn();
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void moveCamera(boolean animated, CameraUpdate update) {
        if (animated) {
            mAMap.animateCamera(update, 1000, null);
        } else {
            mAMap.moveCamera(update);
        }
    }

    private void moveCamera(boolean animated, MapLocation location) {
        // 清理之前的图标
        mAMap.clear();

        // 添加当前位置标记
        MarkerOptions marker = new MarkerOptions();
        marker.position(mLastLocation.position).title(mLastLocation.address).snippet("当前位置");
        mAMap.addMarker(marker);

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
        // 记录当前位置
        mLastLocation.recordLocation(aLocation);
        mLastLocation.writeToPreference(this);

        // 移动Map到当前位置
        moveCamera(true, mLastLocation);

        // 取消定位
        deactivateLocating();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMvMap.onResume();

        if (mLastLocation.needRelocate()) {
            activateLocating();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMvMap.onPause();

        deactivateLocating();
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

    /**
     * 强制隐藏软键盘
     */
    private void hideSoftwareInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAcactvSearch.getWindowToken(), 0);
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
