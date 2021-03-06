package mw.ankara.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
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
public class MapActivity extends MapBaseActivity implements
    GeocodeSearch.OnGeocodeSearchListener, AMap.OnCameraChangeListener {

    /**
     * part of search view
     */
    private AppCompatAutoCompleteTextView mAcactvSearch;

    private Inputtips mInputTips;

    private ArrayAdapter<String> mPoiTipAdapter;

    private Marker mCenterMarker;
    private MapLocation mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSearchView();
        moveCameraToCurrentLocation();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_map;
    }

    /**
     * 设置查询的操作
     */
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
            mCurrentLocation.address = mCenterMarker.getTitle();
            mCurrentLocation.position = mCenterMarker.getPosition();
            mCurrentLocation.writeToIntent(intent);

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
                    if (!TextUtils.isEmpty(newText)) {
                        mInputTips.requestInputtips(newText, "");
                    }
                } catch (AMapException ignored) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void setupMapView(Bundle savedInstanceState) {
        super.setupMapView(savedInstanceState);

        mAMap.setOnCameraChangeListener(this);
    }

    private void moveCameraToCurrentLocation() {
        // 显示上一次的位置，缩放到17级，缩放级别4~20
        mCurrentLocation = new MapLocation();
        if (mCurrentLocation.readFromPreference(this)) {
            moveCamera(false, mCurrentLocation);
        } else {
            moveCamera(false, CameraUpdateFactory.zoomTo(DEFAULT_SCALE_LEVEL));
        }
    }

    /**
     * 开始进行poi搜索
     */
    protected void doPoiSearch(String keyword, String city) {
        hideSoftwareInput();

        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", city);
        query.setPageSize(1);// TODO: 设置每页最多返回多少条poiitem
        query.setPageNum(0);// TODO: 设置查第一页

        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int rCode) {
                if (rCode == 0 && poiResult != null) {
                    // 取得第一页的poi item数据，页数从数字0开始
                    List<PoiItem> poiItems = poiResult.getPois();
                    if (poiItems != null && !poiItems.isEmpty()) {
                        // TODO: 11/8/15 做成多选
                        PoiItem item = poiItems.get(0);
                        LatLonPoint point = item.getLatLonPoint();
                        moveCamera(false, new LatLng(point.getLatitude(), point.getLongitude()));
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
     * 对地图拖动的处理
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mCenterMarker != null && mCenterMarker.isInfoWindowShown()) {
            mCenterMarker.hideInfoWindow();
        }
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        if (mCenterMarker != null) {
            parseLatLngToAddress(mCenterMarker.getPosition());
        }
    }

    /**
     * 逆地理编码部分
     */
    private GeocodeSearch mGeocodeSearch;
    private RegeocodeQuery mRegeocodeQuery;

    private void parseLatLngToAddress(LatLng location) {
        if (mGeocodeSearch == null) {
            mGeocodeSearch = new GeocodeSearch(this);
            mGeocodeSearch.setOnGeocodeSearchListener(this);
            // latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
            mRegeocodeQuery = new RegeocodeQuery(null, 50, GeocodeSearch.AMAP);
        }
        mRegeocodeQuery.setPoint(new LatLonPoint(location.latitude, location.longitude));
        mGeocodeSearch.getFromLocationAsyn(mRegeocodeQuery);
    }

    private void removeGeocodeSearch() {
        if (mGeocodeSearch != null) {
            mGeocodeSearch.setOnGeocodeSearchListener(null);
        }
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (regeocodeResult != null) {
            RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
            if (address != null) {
                mCurrentLocation.city = address.getCity();

                mAcactvSearch.setHint(address.getFormatAddress());
                mCenterMarker.setTitle(address.getFormatAddress());
                mCenterMarker.setSnippet("坐标:" + mCenterMarker.getPosition().latitude
                    + "/" + mCenterMarker.getPosition().longitude);
                mCenterMarker.showInfoWindow();
            }
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        // Auto Generate
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    @Override
    protected void moveCamera(boolean animated, CameraUpdate update) {
        super.moveCamera(animated, update);

        // 添加中心位置标记
        mAMap.clear();
        mCenterMarker = mAMap.addMarker(new MarkerOptions()
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        mCenterMarker.setPositionByPixels(mMvMap.getWidth() / 2, mMvMap.getHeight() / 2);
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aLocation) {
        // 记录当前位置
        mCurrentLocation.recordLocation(aLocation);
        mCurrentLocation.writeToPreference(this);

        // 移动Map到当前位置
        moveCamera(true, mCurrentLocation);

        // 取消定位
        deactivateLocating();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCurrentLocation.needRelocate()) {
            activateLocating();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();

        deactivateLocating();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        removeGeocodeSearch();
    }

    /**
     * 强制隐藏软键盘
     */
    private void hideSoftwareInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAcactvSearch.getWindowToken(), 0);
    }
}