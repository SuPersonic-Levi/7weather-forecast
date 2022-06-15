package indi.sevenweather.android;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.qweather.sdk.view.HeConfig;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "77levi"+this.toString());
        setContentView(R.layout.activity_main);
//        HeConfig.init("HE2204151421551553", "07fcdf72fb3d4816bf9690c8ea0bff1b");
//        HeConfig.switchToDevService(); //初始化并设置为开发者模式
        //先读取缓存，如果不为null那就是已经有缓存数据了，直接跳转到天气预报页面就行
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather",null) != null){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("MainActivity", "77levi"+"onStop");
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("MainActivity", "77levi"+"onDestroy");
    }
    @Override
    protected void onPause(){
        super.onPause();
        Log.d("MainActivity", "77levi"+"onPause");
    }


}