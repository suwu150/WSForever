package com.jkwu.wsforever;

import android.Manifest;
import android.content.Intent;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;

import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;
import com.jkwu.wsforever.biz.ChangePlanType;
import com.jkwu.wsforever.biz.RequestLocation;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnGetGeoCoderResultListener {

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

        // 传感器管理
        private SensorManager mSensorManager;

        // 地理编码搜索
        GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用

        // UI相关
        RadioGroup.OnCheckedChangeListener radioButtonListener;
        private boolean isFirstLocate = true;

        // 语音识别
        EventManager asr = null;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

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
        checkSelfPermissions();
        initActity();

        }

        private void initActity() {
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
            setAudioListener();
            setARScanListener();

            // 初始化地理编码功能
            // 初始化搜索模块，注册事件监听
            initGeoCoder();
            unFocusSearchListener();

            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//            mCurrentMarker = BitmapDescriptorFactory
//                    .fromResource(R.drawable.arrow);
//            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
//            baiduMap.setMyLocationConfiguration(config);

            //
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
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
            if (isGrantedPermission(Manifest.permission.RECORD_AUDIO)) {
                permissionList.add(Manifest.permission.RECORD_AUDIO);
            }
            if (isGrantedPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
                permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
            }
            if (isGrantedPermission(Manifest.permission.INTERNET)) {
                permissionList.add(Manifest.permission.INTERNET);
            }

            Log.d("WSForver", permissionList.isEmpty() + "");
            if (!permissionList.isEmpty()) {
                String [] permissions = permissionList.toArray(new String[permissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, 1);
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

    private void setAudioListener() {
        asr = EventManagerFactory.create(this, "asr");
        final Button audioButton = (Button) findViewById(R.id.audio_button);

        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("audio", "开始语音识别");
                EventListener audioListener = new EventListener() {
                    @Override
                    public void onEvent(String name, String params, byte [] data, int offset, int length) {
                        if (data != null) {
                            Log.d("输出结果：", data[offset + length] + "");
                        };
                        if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)){
                            // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
                            Log.d("audio", name);

                        }
                        if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)){
                            // 识别结束
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        }
                        if (SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS.equals(name)) {
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        }
                        String currentJson = params;
                        String logMessage = "name:" + name + "; params:" + params;

                        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_LOADED)) {
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_UNLOADED)) {
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                            // 引擎准备就绪，可以开始说话
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN)) {
                            // 检测到用户的已经开始说话
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };

                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_END)) {
                            // 检测到用户的已经停止说话
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };

                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                            // 临时识别结果, 长语音模式需要从此消息中取出结果
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };

                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                            // 识别结束， 最终识别结果或可能的错误
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_LONG_SPEECH)) { // 长语音
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_EXIT)) {
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_VOLUME)) {
                            // Logger.info(TAG, "asr volume event:" + params);
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_AUDIO)) {
                            Log.d("audio", name);
                            if (data != null) {
                                Log.d("输出结果：", data[offset + length] + "");
                            };
                        }
                        // ... 支持的输出事件和事件支持的事件参数见“输入和输出参数”一节
                    }
                };
                asr.registerListener(audioListener);
                String json ="{\"accept-audio-data\":false,\"disable-punctuation\":false,\"accept-audio-volume\":true,\"pid\":1736}";
                asr.send(SpeechConstant.ASR_START, json, null, 0, 0);
            }
        };
        audioButton.setOnClickListener(btnClickListener);
    }

    private void setARScanListener() {
        final Button arscanButton = (Button) findViewById(R.id.ar_scan);
        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("arscanButton", "开始AR识别");
            }
        };
        arscanButton.setOnClickListener(btnClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        running = false;
//        Log.i(TAG, "requestCode" + requestCode);
        if (requestCode == 2) {
            String message = "对话框的识别结果：";
            if (resultCode == RESULT_OK) {
                ArrayList results = data.getStringArrayListExtra("results");
                if (results != null && results.size() > 0) {
                    message += results.get(0);
                }
            } else {
                message += "没有结果";
            }
//            Log.i(TAG, "message" + message);
        }

    }

    private void unFocusSearchListener() {
        final EditText SearchContent = (EditText) findViewById(R.id.search_content);
        SearchContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //当actionId == XX_SEND 或者 XX_DONE时都触发
                //或者event.getKeyCode == ENTER 且 event.getAction == ACTION_DOWN时也触发
                //注意，这是一定要判断event != null。因为在某些输入法上会返回null。
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                    //处理事件
                    mSearch.geocode(new GeoCodeOption()
                            .city(SearchContent.getText().toString())
                            .address(SearchContent.getText().toString()));
                }
                return false;
            }
        });

        View.OnFocusChangeListener SearchContentChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mSearch.geocode(new GeoCodeOption()
                            .city(SearchContent.getText().toString())
                            .address(SearchContent.getText().toString()));
                }
                mSearch.geocode(new GeoCodeOption()
                        .city(SearchContent.getText().toString())
                        .address(SearchContent.getText().toString()));

//                if (v.getId() == R.id.reversegeocode) {
//                    int version  = 0;
////                    EditText lat = (EditText) findViewById(R.id.lat);
////                    EditText lon = (EditText) findViewById(R.id.lon);
////                    CheckBox cb = (CheckBox) findViewById(R.id.newVersion);
//                    LatLng ptCenter = new LatLng((Float.valueOf(lat.getText().toString())), (Float.valueOf(lon.getText().toString())));
//
//                    // 反Geo搜索
//                    if(cb.isChecked()){
//                        version=1;
//                    }
//                    mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter).newVersion(version).radius(500));
//                } else if (v.getId() == R.id.geocode) {
//                    EditText editCity = (EditText) findViewById(R.id.city);
//                    EditText editGeoCodeKey = (EditText) findViewById(R.id.geocodekey);
//
//                    // Geo搜索
//                    mSearch.geocode(new GeoCodeOption()
//                            .city(editCity.getText().toString())
//                            .address(editGeoCodeKey.getText().toString()));
//                }
            }
        };
        SearchContent.setOnFocusChangeListener(SearchContentChangeListener);
    }

    private void initGeoCoder() {
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }


    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        baiduMap.clear();
        baiduMap.addOverlay(new MarkerOptions()
                .position(result.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka)));
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
        String strInfo = String.format("纬度：%f 经度：%f",
                result.getLocation().latitude,
                result.getLocation().longitude);
        Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG).show();
        Log.e("GeoCodeDemo", "onGetGeoCodeResult = " + result.toString());
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        baiduMap.clear();
        baiduMap.addOverlay(new MarkerOptions()
                .position(result.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka)));
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
        Toast.makeText(MainActivity.this, result.getAddress() + " adcode: " + result.getAdcode(), Toast.LENGTH_LONG).show();
        Log.e("GeoCodeDemo", "ReverseGeoCodeResult = " + result.toString());
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
     * @param view
     */
    public void setTraffic(View view) {
        baiduMap.setTrafficEnabled(((CheckBox) view).isChecked());
    }

    /**
     * 设置是否显示百度热力图
     * @param view
     */
    public void setBaiduHeatMap(View view) {
        baiduMap.setBaiduHeatMapEnabled(((CheckBox) view).isChecked());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    /**
     * 获取点击事件
     */
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            View view = getCurrentFocus();
//            if (isHideInput(view, ev)) {
//                HideSoftInput(view.getWindowToken());
//                view.clearFocus();
//            }
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    /**
     * 判定是否需要隐藏
     */
//    private boolean isHideInput(View v, MotionEvent ev) {
//        if (v != null && (v instanceof EditText)) {
//            int[] l = {0, 0};
//            v.getLocationInWindow(l);
//            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
//            if (ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
//                return false;
//            } else {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * 隐藏软键盘
     */
//    private void HideSoftInput(IBinder token) {
//        if (token != null) {
//            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
//        }
//    }

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
