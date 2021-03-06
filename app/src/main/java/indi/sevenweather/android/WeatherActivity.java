package indi.sevenweather.android;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.IOException;

import indi.sevenweather.android.gson.Forecast;
import indi.sevenweather.android.gson.Weather;
import indi.sevenweather.android.service.AutoUpdateService;
import indi.sevenweather.android.util.HttpUtil;
import indi.sevenweather.android.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private LinearLayout forecastLayout;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;//用于记录城市的天气id
    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)

    {
        super.onCreate(savedInstanceState);
        /**
         * 让顶栏变透明*/
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_weather);//这个必须在下面的初始化之前完成

        Log.d(TAG, "77levi2"+this.toString());
        //初始化各种控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button)findViewById(R.id.nav_button);


        /**
         * 手动切换城市*/
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);//打开滑动菜单且保证都是左滑，保证行为一致
            }
        });




        /**刷新天气 初始化*/
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        //刷新进度条的颜色设置
        swipeRefresh.setColorSchemeResources(android.R.color.holo_red_light);




        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //用的是键值对存储数据，数据持久化技术;获取了sharedpreferences对象(本地缓存)
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        } //加载必应背景图片

        String weatherString = prefs.getString("weather",null);//读取这里面的数据

        if(weatherString != null){
            //有读取到缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;//记录到mweatherid里
            showWeatherInfo(weather);
        }else {
            //没读取到缓存，去服务器查询天气数据
            String weatherId = getIntent().getStringExtra("weather_id");//获取用于启动WeatherActivity的intent并传入相应的key得到string（天气id）数据，因为传入的是字符串所以是getstringextra整型则是getIntExtra
            weatherLayout.setVisibility(View.INVISIBLE);//隐藏天气数据页面（因为此时是空的，不好看
            requestWeather(weatherId);//用天气id请求查询城市天气信息
        }

        //刷新逻辑
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);//保存id重新请求下响应
            }
        });



    }


    /**
     * 根据天气id请求查询城市天气信息*/
    public void requestWeather(final String weatherId){

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=07fcdf72fb3d4816bf9690c8ea0bff1b";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"天气信息获取失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);//隐藏进度条，刷新结束
                    }
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){//确认收到了数据返回

                            SharedPreferences.Editor editor = PreferenceManager//类似设置一个输入器来输入数据
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);//把服务器里读回的数据输入进editor
                            editor.apply();//提交以完成数据存储工作,相当于存了一个数据文件在本地了
                            mWeatherId = weather.basic.weatherId; // 不懂为什么要更新一下id
                            showWeatherInfo(weather);

                        }else{//没确认收到

                            Toast.makeText(WeatherActivity.this,"天气信息获取失败",Toast.LENGTH_SHORT).show();

                        }
                        swipeRefresh.setRefreshing(false);//请求结束后调用这个方法表示刷新时间结束，隐藏刷新进度条。
                    }
                });

            }
        });
        loadBingPic();
    }

    /**
     * 处理并展示weather实体类中的数据
     * */
    private void showWeatherInfo(Weather weather){
        //初始化需要的数据,从weather对象中获取数据
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];//先用“ ”隔开形成一个字符串组，通过索引取出第二个
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;

        //在响应控件上显示
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();//抹去之前的，展现新的？
        for(Forecast forecast :weather.forecastList){//显示未来几天的天气预报
            //动态加载布局
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);

            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText =(TextView) view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            forecastLayout.addView(view);
        }

        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度 :"+weather.suggestion.comfort.info;
        String carWash = "洗车指数 :"+weather.suggestion.carWash.info;
        String sport = "运动建议 :" + weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        /**
         * 启动自动更新*/
        Intent intent = new Intent(this, AutoUpdateService.class);//第一个作为上下文，第二个参数就是意图，要打开自动更新（目标活动）
        startService(intent);//传入参数启动目标活动

    }

    /**
     * 加载bing每日一图*/
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();//添加到weather本地缓存
                editor.putString("bing_pic",bingPic);
                editor.apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });


            }
        });
    }

}