package indi.sevenweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("city") //JSON中的有些字段不太适合作为java字段就用这个注解的方式来在json和java字段间建立映射关系
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
