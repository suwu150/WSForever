package com.jkwu.wsforever;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;

import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.jkwu.wsforever.biz.ChangePlanType;
import com.jkwu.wsforever.biz.RequestLocation;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

        public LocationClient mLocationClient;
        private MapView mMapView = null;
        private BaiduMap baiduMap;
        BitmapDescriptor mCurrentMarker;
        private UiSettings mUiSettings;;
        private static final int accuracyCircleFillColor = 0xAAFFFF88;
        private static final int accuracyCircleStrokeColor = 0xAA00FF00;
        private Double lastX = 0.0;
        private int mCurrentDirection = 0;
        private double mCurrentLat = 0.0;
        private double mCurrentLon = 0.0;
        private float mCurrentAccracy;
        private  LocationMode mCurrentMode;
        private MyLocationData locationData;

        private SensorManager mSensorManager;


        // UI相关
        RadioGroup.OnCheckedChangeListener radioButtonListener;
        private boolean isFirstLocate = true;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

//            Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
//            setSupportActionBar(toolbar);
            if (Build.VERSION.SDK_INT >= 21) {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
            mLocationClient = new LocationClient(getApplicationContext());
            mLocationClient.registerLocationListener(new MyLocationListener());


            initLocation();

            setContentView(R.layout.activity_main);

            //获取地图控件引用
            mMapView = (MapView) findViewById(R.id.bmapView);
            baiduMap = mMapView.getMap();
            // 开启定位图层
            baiduMap.setMyLocationEnabled(true);
            baiduMap.setIndoorEnable(true);//打开室内图，默认为关闭状态
//            mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;//定位跟随态
            mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;   //默认为 LocationMode.NORMAL 普通态
//            mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;  //定位罗盘态

            mUiSettings = baiduMap.getUiSettings(); // 设置界面
            baiduMap.setCompassPosition(new android.graphics.Point(70, 200));
            baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            //开启交通图
            baiduMap.setTrafficEnabled(true);
            changePlanTypeRadioListener();
            requestLocListener();

            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//            mCurrentMarker = BitmapDescriptorFactory
//                    .fromResource(R.drawable.arrow);
//            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
//            baiduMap.setMyLocationConfiguration(config);

            //
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
            checkSelfPermissions();
        }

        private void checkSelfPermissions() {
            List<String> permissionList = new ArrayList<>();
            if (isGrantedPermission(Manifest.permission.READ_PHONE_STATE)) {
                permissionList.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (isGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (isGrantedPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            Log.d("WSForver", permissionList.isEmpty() + "");
            if (!permissionList.isEmpty()) {
                String [] permissions = permissionList.toArray(new String[permissionList.size()]);
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
            } else {
                requestLocation();
            }
        }

        private boolean isGrantedPermission(String permission) {
            return ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED;
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0) {
                    for(int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能够使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
            default:
        }
    }

    private void initLocation() {
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            //可选，设置定位模式，默认高精度
            //LocationMode.Hight_Accuracy：高精度；
            //LocationMode. Battery_Saving：低功耗；
            //LocationMode. Device_Sensors：仅使用设备；

            option.setCoorType("bd09ll");
            //可选，设置返回经纬度坐标类型，默认GCJ02
            //GCJ02：国测局坐标；
            //BD09ll：百度经纬度坐标；
            //BD09：百度墨卡托坐标；
            //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
            option.setScanSpan(5000);
            //可选，设置发起定位请求的间隔，int类型，单位ms
            //如果设置为0，则代表单次定位，即仅定位一次，默认为0
            //如果设置非0，需设置1000ms以上才有效
            option.setIsNeedAddress(true);

//            option.setEnableSimulateGps(true);
            //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

            mLocationClient.setLocOption(option);
        }

        private void requestLocation() {
            mLocationClient.start();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            mLocationClient.stop();
            // 当不需要定位图层时关闭定位图层
            baiduMap.setMyLocationEnabled(false);
            //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
            mMapView.onDestroy();
        }


        @Override
        protected void onStop() {
            //取消注册传感器监听
            mSensorManager.unregisterListener(this);
            super.onStop();
        }

        @Override
        protected void onResume() {
            super.onResume();
            //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
            mMapView.onResume();
            //为系统的方向传感器注册监听器
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_UI);
        }
        @Override
        protected void onPause() {
            super.onPause();
            //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
            mMapView.onPause();
        }

        private void navigateTo(BDLocation location) {
            if (isFirstLocate) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                baiduMap.animateMapStatus(update);
                update = MapStatusUpdateFactory.zoomTo(19f);
                baiduMap.animateMapStatus(update);
                isFirstLocate = false;
            }


            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();

            // 构造定位数据
            locationData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locationData);


        }

        private void changePlanTypeRadioListener() {
            RadioGroup groupPlan = (RadioGroup) this.findViewById(R.id.radioGroupPlan);
            radioButtonListener = new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    ChangePlanType changePlanType = new ChangePlanType(baiduMap);
                    switch (checkedId) {
                        case R.id.starsPlan: {
                            changePlanType.setStarsPlan();
                        }
                        break;
                        case R.id.twoDPlan: {
                            changePlanType.setTwoDPlan();
                        }
                        break;
                        case R.id.threeDPlan: {
                            changePlanType.setThreeDPlan();
                        }
                        break;
                        default: {
                        }
                    }
                }
            };
            groupPlan.setOnCheckedChangeListener(radioButtonListener);

        }

        private void requestLocListener() {
            final Button requestLocButton = (Button) findViewById(R.id.request_location_button);
            final TextView requestLocationButtonText = (TextView) findViewById(R.id.request_location_button_text);
            requestLocationButtonText.setText("普通");
            mCurrentMode = LocationMode.NORMAL;
            View.OnClickListener btnClickListener = new View.OnClickListener() {
                RequestLocation requestLocation = new RequestLocation(baiduMap);
                public void onClick(View v) {
                    switch (mCurrentMode) {
                        case NORMAL:
                            requestLocationButtonText.setText("跟随");
                            mCurrentMode = requestLocation.setFollowing();
                            break;
                        case COMPASS:
                            requestLocationButtonText.setText("普通");
                            mCurrentMode = requestLocation.setNormal();
                            break;
                        case FOLLOWING:
                            requestLocationButtonText.setText("罗盘");
                            mCurrentMode = requestLocation.setCompass();
                            break;
                        default:
                            break;
                    }
                }
            };
            requestLocButton.setOnClickListener(btnClickListener);
        }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locationData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            baiduMap.setMyLocationData(locationData);
        }
        lastX = x;

    }

    /**
     * 设置是否显示交通图
     *
     * @param view
     */
    public void setTraffic(View view) {
        baiduMap.setTrafficEnabled(((CheckBox) view).isChecked());
    }

    /**
     * 设置是否显示百度热力图
     *
     * @param view
     */
    public void setBaiduHeatMap(View view) {
        baiduMap.setBaiduHeatMapEnabled(((CheckBox) view).isChecked());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    public class MyLocationListener extends BDAbstractLocationListener {

            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                Log.d("百度定位", bdLocation.getAddrStr());
             if (bdLocation.getLocType() == BDLocation.TypeGpsLocation ||
                     bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                 navigateTo(bdLocation);
             }
            }
        }
    }
