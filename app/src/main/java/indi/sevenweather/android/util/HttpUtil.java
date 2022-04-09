package indi.sevenweather.android.util;

import android.app.DownloadManager;

import okhttp3.Request;


public class HttpUtil {

    public  static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        Request request = new Request //用这种方式建立request用来收到到服务器响应
                .Builder()
                .url(address)
                .build();
}
