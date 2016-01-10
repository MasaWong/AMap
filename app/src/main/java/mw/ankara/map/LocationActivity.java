package mw.ankara.map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;

import java.util.ArrayList;

/**
 * @author masawong
 * @since 11/14/15
 */
public class LocationActivity extends MapBaseActivity
    implements AMapNaviListener, AMapNaviViewListener {

    // 终点
    private ArrayList<NaviLatLng> mStarting = new ArrayList<>();
    private ArrayList<NaviLatLng> mDestination = new ArrayList<>();

    private ProgressDialog mProgressDialog;

    private AMapNavi mAMapNavi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri != null) {
            String title = uri.getQueryParameter("title");
            setTitle(title);

            double lat = Double.parseDouble(uri.getQueryParameter("lat"));
            double lng = Double.parseDouble(uri.getQueryParameter("lng"));
            mDestination.add(new NaviLatLng(lat, lng));

            LatLng location = new LatLng(lat, lng);
            Marker marker = mAMap.addMarker(new MarkerOptions().position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

            String position = uri.getQueryParameter("position");
            marker.setTitle(position);
            marker.setSnippet("坐标:" + lat + "/" + lng);
            marker.showInfoWindow();

            moveCamera(false, location);
        }
    }

    /**
     * 设置查询的操作
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_navigate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.navigate) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this, 3);
                mProgressDialog.setCancelable(true);
            }
            mProgressDialog.setMessage("正在定位您的位置");
            mProgressDialog.show();

            activateLocating();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(AMapLocation aLocation) {
        mStarting.clear();
        mStarting.add(new NaviLatLng(aLocation.getLatitude(), aLocation.getLongitude()));

        deactivateLocating();
        mProgressDialog.setMessage("正在规划路径");

        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.setAMapNaviListener(this);
        if (!mAMapNavi.calculateDriveRoute(mStarting, mDestination, null, AMapNavi.DrivingDefault)) {
            mProgressDialog.dismiss();
            Toast.makeText(this, "查询路径失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCalculateRouteSuccess() {
        mProgressDialog.dismiss();
        // 删除导航监听
        if (mAMapNavi != null) {
            mAMapNavi.removeAMapNaviListener(this);
        }

        startActivity(new Intent(this, NavigationActivity.class));
        finish();
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        mProgressDialog.dismiss();

        Toast.makeText(this, "查询路径失败", Toast.LENGTH_SHORT).show();
    }

    // useless

    @Override
    public void onInitNaviFailure() {
    }

    @Override
    public void onInitNaviSuccess() {
    }

    @Override
    public void onStartNavi(int i) {
    }

    @Override
    public void onTrafficStatusUpdate() {
    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
    }

    @Override
    public void onGetNavigationText(int i, String s) {
    }

    @Override
    public void onEndEmulatorNavi() {
    }

    @Override
    public void onArriveDestination() {
    }

    @Override
    public void onReCalculateRouteForYaw() {
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
    }

    @Override
    public void onArrivedWayPoint(int i) {
    }

    @Override
    public void onGpsOpenStatus(boolean b) {
    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
    }

    @Override
    public void onNaviSetting() {
    }

    @Override
    public void onNaviCancel() {
    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {
    }

    @Override
    public void onNaviTurnClick() {
    }

    @Override
    public void onNextRoadClick() {
    }

    @Override
    public void onScanViewButtonClick() {
    }

    @Override
    public void onLockMap(boolean b) {
    }
}
