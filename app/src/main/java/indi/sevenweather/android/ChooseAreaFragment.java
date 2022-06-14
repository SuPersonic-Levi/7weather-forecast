package indi.sevenweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import indi.sevenweather.android.db.City;
import indi.sevenweather.android.db.County;
import indi.sevenweather.android.db.Province;
import indi.sevenweather.android.gson.Weather;
import indi.sevenweather.android.util.HttpUtil;
import indi.sevenweather.android.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Button play;
    private Button pause;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    //省
    private List<Province> provinceList;

    //市
    private List<City> cityList;

    //县
    private List<County> countyList;

    //以下是选中的省、市
    private Province selectedProvince;
    private City selectedCity;

    //选中的级别
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.choose_area, container, false);//初始化layout下的xml
        titleText = (TextView) view.findViewById(R.id.title_text); //获取这些控件的实例，绑定
        backButton = (Button) view.findViewById(R.id.back_button);
        play = (Button)view.findViewById(R.id.play);
        pause = (Button)view.findViewById(R.id.pause);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList); //初始化arrayadapter，设置为Listview的适配器
        listView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        //给listview设置点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){//跳转到天气显示页面，有点不懂
                    String weatherId = countyList.get(position).getWeatherId(); //获取到天气id再传给intent里

                    /** 用instanceof来判断这个activity是哪一个类的实例里的然后来选择不同的逻辑*/
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);//第一个参数是key第二个是value，用前面的名字来给后面真正的值取值
                        startActivity(intent);//启动天气显示页面
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });

//给back的button设置点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }

            }
        });


        //初始化media

        initMediaPlayer();

        //播放音乐
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
            }
        });





        queryProvinces();//意味着先从省布局开始加载
    }

    private void initMediaPlayer(){
        try{
            mediaPlayer.setDataSource("http://m10.music.126.net/20220614161934/18da3f65aebccdd1932e4f816f513dea/ymusic/b255/21f2/d814/7cd8927b9ca639004ec27e723bd3961a.mp3");
            mediaPlayer.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * 用来查询全国所有的省，先从数据库查询，没查到再去服务器查*/
    private void queryProvinces(){
        titleText.setText("中国");  //把头布局标题设置成中国的同时把返回键隐藏（因为无法再返回上一层了
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class); //在数据库里搜索省级

        if(provinceList.size() > 0){

            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName()); //遍历省数据库
            }

            adapter.notifyDataSetChanged();//数据无法直接传递给控件，需要一个适配器通知数据刷新了
            listView.setSelection(0);// public static final int LEVEL_PROVINCE = 0;
            currentLevel = LEVEL_PROVINCE; /** 有点不懂这里为什么要重新把这个level再设置一次*/



        }else{
            String address = "http://guolin.tech/api/china"; //从提供的服务器中获取数据
            queryFromServer(address,"province");
        }
    }


    /**市的数据，与省同理*/

    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName()); //从已经选择了的省中获取名字
        backButton.setVisibility(View.VISIBLE);

        /**
        *
        ** LitePal Where 多条件查询

最近开发的一个APP本地数据存储使用的LitePal，用到了多条件查询，只用有个条件的查询比较好用。

DataSupport.where("name = ?", "张三").find( User.class);
多条件查询也很简单，把两个单条件的中简加一个 and 或 or 关键字 链接在一起就可以了

DataSupport.where("name = ? or pric = ?", "张三", "5000").find( User.class);**/

        cityList = LitePal.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);//服务器里遍历选择了的provinceid的所有城市
        if(cityList.size() > 0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0); //selection显示第0+1的item
            currentLevel = LEVEL_CITY;

        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }


//县
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;

        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }


    /**
     * 根据传入的address类型从服务器查询具体数据*/
    private void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() { //写匿名类来回调
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //通过runonUithread方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }


            @Override//响应的数据会回调到这里
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                //将解析传回来的数据存储到数据库里
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText); //解析传回来的省json数据并存储到数据库中
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }

                //需要再一次
                if(result){
                    getActivity().runOnUiThread(new Runnable() {//使用runonuithread从子线程切换到主线程
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();//由于queryprovinces牵扯到了ui操作，所以必须在主线程中调用；由于数据库中已经有了数据，直接调用query就会出现在界面上了
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }

    /**
     * 显示进度对话框*/
    private void showProgressDialog(){
        if(progressDialog == null){ //如果没有这个对话框就新建一个
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("加载中");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }


    /**
     * 关闭进度对话框*/
    private void closeProgressDialog(){
        if(progressDialog != null){ //如果对话框存在才能close
            progressDialog.dismiss();
        }
    }

}
