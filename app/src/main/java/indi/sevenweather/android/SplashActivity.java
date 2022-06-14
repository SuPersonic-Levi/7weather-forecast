package indi.sevenweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;
import com.qq.e.comm.managers.GDTAdSdk;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.PasswordCallback;

import dalvik.system.PathClassLoader;

public class SplashActivity extends AppCompatActivity {
    private RelativeLayout container;
    private SplashAD splashAD;
    /**
     * 用于判断是否可以跳过广告直接进入mainactivity*/
    private boolean canJump;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        GDTAdSdk.init(this, "1200517470");

        container = (RelativeLayout) findViewById(R.id.container);

        //进行运行时权限处理
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String []permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this,permissions,1);
        }else {
            requestAds();
        }


    }



    /**
     * 开屏广告*/
    private void requestAds(){

//        String posId = "6073732533050922";
        String posId = "9093517612222759";

        splashAD = new SplashAD(this, posId, new SplashADListener() {
            @Override
            public void onADDismissed() {
                //广告显示完毕
                forward();
            }

            @Override
            public void onNoAD(AdError adError) {
                //广告加载失败
                forward();

            }

            @Override
            public void onADPresent() {//广告加载成功

            }

            @Override
            public void onADClicked() {//广告被点击

            }

            @Override
            public void onADTick(long l) {

            }

            @Override
            public void onADExposure() {

            }

            @Override
            public void onADLoaded(long l) {

            }
        });
        splashAD.fetchAndShowIn(container);
        }
    @Override
    protected void onPause(){
        super.onPause();
        canJump = false;
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(canJump){
            forward();
        }
        canJump = true;
    }

    private void forward(){
        if(canJump){
            //跳转到MainActivity
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            canJump = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String [] permissions, int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestAds();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
}
