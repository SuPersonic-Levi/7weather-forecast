package indi.sevenweather.android.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import indi.sevenweather.android.db.City;
import indi.sevenweather.android.db.County;
import indi.sevenweather.android.db.Province;
import indi.sevenweather.android.gson.Weather;

public class Utility {
   /**  用来解析json格式的数据（服务器返回的省市县数据在里面）*/
    public static boolean handleProvinceResponse(String response){//注意这里的handle是处理的意思
        if( !TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces = new JSONArray(response);

                for(int i = 0; i < allProvinces.length();i++){ //遍历传回的所有省份
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name")); //从json里提取需要信息并set
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();

                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }

        }

return false;
    }

    /**开始解析和处理服务器返回的市级数据
     * */
    public static boolean handleCityResponse(String response, int provinceId){
        if (!TextUtils.isEmpty(response)) {//方法同province
            try{
                JSONArray allCities = new JSONArray(response);

                for(int i = 0; i < allCities.length(); i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();

                }
                return true;

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**开始解析和处理服务器返回的县级数据
     * */

    public static boolean handleCountyResponse(String response, int cityId){
        if (!TextUtils.isEmpty(response)) {//方法同province
            try{
                JSONArray allCounties = new JSONArray(response);

                for(int i = 0; i < allCounties.length(); i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();

                }
                return true;

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成weather实体类*/

    public static Weather handleWeatherResponse(String response){

        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);


        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
