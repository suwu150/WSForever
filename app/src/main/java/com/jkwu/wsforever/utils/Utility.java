package com.jkwu.wsforever.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.jkwu.wsforever.R;
import com.jkwu.wsforever.db.City;
import com.jkwu.wsforever.db.County;
import com.jkwu.wsforever.db.Province;
import com.jkwu.wsforever.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行dradlew
     */
    public static int handleWeatherIcon(String code) {
        int icon = R.drawable.icon_weather_100;
        switch (code) {
            case "100n": { icon = R.drawable.icon_weather_100n; break; }
            case "103n": { icon = R.drawable.icon_weather_103n; break; }
            case "104n": { icon = R.drawable.icon_weather_104n; break; }
            case "300n": { icon = R.drawable.icon_weather_300n; break; }
            case "301n": { icon = R.drawable.icon_weather_301n; break; }
            case "406n": { icon = R.drawable.icon_weather_406n; break; }
            case "407n": { icon = R.drawable.icon_weather_407n; break; }
            case "100": { icon = R.drawable.icon_weather_100; break; }
            case "101": { icon = R.drawable.icon_weather_101; break; }
            case "102": { icon = R.drawable.icon_weather_102; break; }
            case "103": { icon = R.drawable.icon_weather_103; break; }
            case "104": { icon = R.drawable.icon_weather_104; break; }
            case "200": { icon = R.drawable.icon_weather_200; break; }
            case "201": { icon = R.drawable.icon_weather_201; break; }
            case "202": { icon = R.drawable.icon_weather_202; break; }
            case "203": { icon = R.drawable.icon_weather_203; break; }
            case "204": { icon = R.drawable.icon_weather_204; break; }
            case "205": { icon = R.drawable.icon_weather_205; break; }
            case "206": { icon = R.drawable.icon_weather_206; break; }
            case "207": { icon = R.drawable.icon_weather_207; break; }
            case "208": { icon = R.drawable.icon_weather_208; break; }
            case "209": { icon = R.drawable.icon_weather_209; break; }
            case "210": { icon = R.drawable.icon_weather_210; break; }
            case "211": { icon = R.drawable.icon_weather_211; break; }
            case "212": { icon = R.drawable.icon_weather_212; break; }
            case "213": { icon = R.drawable.icon_weather_213; break; }
            case "300": { icon = R.drawable.icon_weather_300; break; }
            case "301": { icon = R.drawable.icon_weather_301; break; }
            case "302": { icon = R.drawable.icon_weather_302; break; }
            case "303": { icon = R.drawable.icon_weather_303; break; }
            case "304": { icon = R.drawable.icon_weather_304; break; }
            case "305": { icon = R.drawable.icon_weather_305; break; }
            case "306": { icon = R.drawable.icon_weather_306; break; }
            case "307": { icon = R.drawable.icon_weather_307; break; }
            case "308": { icon = R.drawable.icon_weather_308; break; }
            case "309": { icon = R.drawable.icon_weather_309; break; }
            case "310": { icon = R.drawable.icon_weather_310; break; }
            case "311": { icon = R.drawable.icon_weather_311; break; }
            case "312": { icon = R.drawable.icon_weather_312; break; }
            case "313": { icon = R.drawable.icon_weather_313; break; }
            case "314": { icon = R.drawable.icon_weather_314; break; }
            case "315": { icon = R.drawable.icon_weather_315; break; }
            case "316": { icon = R.drawable.icon_weather_316; break; }
            case "317": { icon = R.drawable.icon_weather_317; break; }
            case "318": { icon = R.drawable.icon_weather_318; break; }
            case "399": { icon = R.drawable.icon_weather_399; break; }
            case "400": { icon = R.drawable.icon_weather_400; break; }
            case "401": { icon = R.drawable.icon_weather_401; break; }
            case "402": { icon = R.drawable.icon_weather_402; break; }
            case "403": { icon = R.drawable.icon_weather_403; break; }
            case "404": { icon = R.drawable.icon_weather_404; break; }
            case "405": { icon = R.drawable.icon_weather_405; break; }
            case "406": { icon = R.drawable.icon_weather_406; break; }
            case "407": { icon = R.drawable.icon_weather_407; break; }
            case "408": { icon = R.drawable.icon_weather_408; break; }
            case "409": { icon = R.drawable.icon_weather_409; break; }
            case "410": { icon = R.drawable.icon_weather_410; break; }
            case "499": { icon = R.drawable.icon_weather_499; break; }
            case "500": { icon = R.drawable.icon_weather_500; break; }
            case "501": { icon = R.drawable.icon_weather_501; break; }
            case "502": { icon = R.drawable.icon_weather_502; break; }
            case "503": { icon = R.drawable.icon_weather_503; break; }
            case "504": { icon = R.drawable.icon_weather_504; break; }
            case "507": { icon = R.drawable.icon_weather_507; break; }
            case "508": { icon = R.drawable.icon_weather_508; break; }
            case "509": { icon = R.drawable.icon_weather_509; break; }
            case "510": { icon = R.drawable.icon_weather_510; break; }
            case "511": { icon = R.drawable.icon_weather_511; break; }
            case "512": { icon = R.drawable.icon_weather_512; break; }
            case "513": { icon = R.drawable.icon_weather_513; break; }
            case "514": { icon = R.drawable.icon_weather_514; break; }
            case "515": { icon = R.drawable.icon_weather_515; break; }
            case "900": { icon = R.drawable.icon_weather_900; break; }
            case "901": { icon = R.drawable.icon_weather_901; break; }
            case "999": { icon = R.drawable.icon_weather_999; break; }
        }
        return icon;
    }

}
