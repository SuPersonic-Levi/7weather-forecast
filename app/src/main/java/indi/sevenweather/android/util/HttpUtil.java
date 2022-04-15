package indi.sevenweather.android.util;

import android.app.DownloadManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class HttpUtil {

    public  static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request //用这种方式建立request用来收到到服务器响应
                .Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback /** 没看懂 没懂*/);
            }
}

