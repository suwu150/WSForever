package com.jkwu.wsforever.biz;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;

public class RequestLocation {
    private BaiduMap typeBaiduMap;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private BitmapDescriptor mCurrentMarker;
    public RequestLocation(BaiduMap baiduMap){
        //获取地图控件引用
        typeBaiduMap = baiduMap;
    }

    /*
    定位跟随态
       mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;//定位跟随态
       mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;   //默认为 LocationMode.NORMAL 普通态
       mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;  //定位罗盘态
   */
    public LocationMode setFollowing () {
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;//定位跟随态
        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
        typeBaiduMap.setMyLocationConfiguration(config);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(0);
        typeBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        return mCurrentMode;
    }

    /*
    普通态
     */
    public LocationMode setNormal () {
//        requestLocButton.setText("普通");
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;   //默认为 LocationMode.NORMAL 普通态
        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
        typeBaiduMap.setMyLocationConfiguration(config);
        MapStatus.Builder builder1 = new MapStatus.Builder();
        builder1.overlook(0);
        typeBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
        return mCurrentMode;
    }

    /*
    定位罗盘态
     */
    public LocationMode setCompass () {
        // requestLocButton.setText("罗盘");
        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;  //定位罗盘态
        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
        typeBaiduMap.setMyLocationConfiguration(config);
        return mCurrentMode;
    }
}
