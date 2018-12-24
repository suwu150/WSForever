package com.jkwu.wsforever.biz;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;

public class ChangePlanType {
    private BaiduMap typeBaiduMap;
    public ChangePlanType(BaiduMap baiduMap){
        //获取地图控件引用
        typeBaiduMap = baiduMap;
    }

    /*
    卫星图
     */
    public void setStarsPlan () {
        typeBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
    }

    /*
    2D平面图
     */
    public void setTwoDPlan () {
        /**
         * 处理俯视 俯角范围： -45 ~ 0 , 单位： 度
         */

        typeBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        int overlookAngle = Integer.parseInt("-10".toString());
        MapStatus ms = new MapStatus.Builder(typeBaiduMap.getMapStatus()).overlook(overlookAngle).build();
        MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
        typeBaiduMap.animateMapStatus(u);
    }

    /*
    3D平面图
     */
    public void setThreeDPlan () {
        /**
         * 处理俯视 俯角范围： -45 ~ 0 , 单位： 度
         */
        typeBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        int overlookAngle = Integer.parseInt("-45".toString());
        MapStatus ms = new MapStatus.Builder(typeBaiduMap.getMapStatus()).overlook(overlookAngle).build();
        MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
        typeBaiduMap.animateMapStatus(u);
    }
}
