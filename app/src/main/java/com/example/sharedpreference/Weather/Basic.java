package com.example.sharedpreference.Weather;

import com.google.gson.annotations.SerializedName;

/**
 * 基本信息
 */
public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }

}
