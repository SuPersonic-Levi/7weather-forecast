package indi.sevenweather.android.util;

import android.app.DownloadManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**公共类，想要发起网络请求的时候调用该类的方法就行*/
public class HttpUtil {

    public  static void sendOkHttpRequest(String address, okhttp3.Callback callback){//callback参数是回调接口，类似之前写的httpcallbacklistner
        OkHttpClient client = new OkHttpClient();
        Request request = new Request //用这种方式建立request对象用来收到到服务器响应，此时是空的对象，没有什么作用，所以下面以build（）之前进行连缀来丰富request对象
                .Builder() //构造器
                .url(address)//设置目的网络地址
                .build();
                /** newCall()创建call对象*/
        client.newCall(request).enqueue(callback /** 没看懂 没懂*/);  //client.newcall()后直接调用enqueue方法把callback参数传入
        //enqueue方法内部帮我们开了子线程，在子线程中执行http请求，最终的请求结果回调到okhttp3.Callback里

        //本来是调用Call对象的execute方法发送请求并获取返回数据。用response.body().string()来得到返回的具体内容；
        // 如果是post请求会更复杂点，这部分程序写的是get请求
            }
}

