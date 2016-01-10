package mw.ankara.map;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;

/**
 * @author masawong
 * @since 11/10/15
 */
public class NavigationActivity extends AppCompatActivity implements
    AMapNaviViewListener {

    //导航View
    private AMapNaviView mAMapNaviView;

    private TTSController mTTSController;

    private AMapNavi mAMapNavi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mTTSController = TTSController.getInstance(this);
        mTTSController.init();
        //语音播报开始
        mTTSController.startSpeaking();

        mAMapNavi = AMapNavi.getInstance(this);
        mAMapNavi.setAMapNaviListener(mTTSController);// 设置语音模块播报

        mAMapNaviView = (AMapNaviView) findViewById(R.id.navigation_mv_map);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);

        // 开启实时导航
        mAMapNavi.startNavi(AMapNavi.GPSNaviMode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog mAlertDialog;

    @Override
    public void onBackPressed() {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog
                .Builder(this, R.style.Base_Theme_AppCompat_Light_Dialog_Alert)
                .setMessage("是否退出导航")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavigationActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("取消", null).create();
        }
        mAlertDialog.show();
    }

    /**
     * 导航页面返回监听
     */
    @Override
    public boolean onNaviBackClick() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mAMapNaviView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        mAMapNaviView.onResume();
        mAMapNavi.resumeNavi();
    }

    @Override
    public void onPause() {
        super.onPause();

        mAMapNaviView.onPause();
        mAMapNavi.stopNavi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAMapNaviView.onDestroy();
        mAMapNavi.destroy();// 销毁导航
        mTTSController.stopSpeaking();
        mTTSController.destroy();
    }

    @Override
    public void onNaviSetting() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onNaviMapMode(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onNaviTurnClick() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onNextRoadClick() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onScanViewButtonClick() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLockMap(boolean arg0) {
        // TODO Auto-generated method stub
    }

    /**
     * 导航界面返回按钮监听
     */
    @Override
    public void onNaviCancel() {
        // TODO Auto-generated method stub
    }
}
