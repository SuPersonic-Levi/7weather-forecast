package indi.sevenweather.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import indi.sevenweather.android.db.City;
import indi.sevenweather.android.db.County;
import indi.sevenweather.android.db.Province;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
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

        

    }
}
