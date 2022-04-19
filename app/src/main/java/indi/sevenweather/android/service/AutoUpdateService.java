package indi.sevenweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.io.IOException;

import indi.sevenweather.android.WeatherActivity;
import indi.sevenweather.android.gson.Weather;
import indi.sevenweather.android.util.HttpUtil;
import indi.sevenweather.android.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

   @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        updateWeather(); //这两个只是为了更新缓存而已，主要的实施逻辑还是在weatheractivity里
        updateBingPic();
       Log.d("AutoUpdateService", "onStartCommand: ");
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        int anHour = 10*1000;//8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour; //定时任务触发时间
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);

        return super.onStartCommand(intent,flags,startId);

    }
    /**
     * 更新天气*/
    public void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId ;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                    weatherId + "&key=07fcdf72fb3d4816bf9690c8ea0bff1b";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if(weather != null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }

                }
            });
        }
    }

    /**
     * 更新必应每日一图*/
    public void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();//添加到weather本地缓存
                editor.putString("bing_pic",bingPic);
                editor.apply();



            }
        });
    }

}