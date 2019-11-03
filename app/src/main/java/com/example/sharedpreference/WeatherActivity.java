package com.example.sharedpreference;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedpreference.Util.HttpUtil;
import com.example.sharedpreference.Weather.ForeCast;
import com.example.sharedpreference.Weather.Weather;
import com.example.sharedpreference.WeatherUtil.City;
import com.example.sharedpreference.WeatherUtil.County;
import com.example.sharedpreference.WeatherUtil.Province;
import com.example.sharedpreference.WeatherUtil.WeatherUtil;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final int QUERY_PROVINCE = 0;
    private static final int QUERY_CITY = 1;
    private static final int QUERY_COUNTY = 2;

    private static final int CITY_ONLY = 0;
    private static final int CITY_AND_COUNTY = 1;

    private static final int PROVINCE_READY = 0;
    private static final int CITY_READY = 1;
    private static final int COUNTY_READY = 2;
    private static final int PROVINCE_FAILED = 3;
    private static final int CITY_FAILED = 4;
    private static final int COUNTY_FAILED = 5;

    TextView queryWeatherButton;
    EditText provinceEditText;
    EditText cityEditText;
    EditText countyEditText;
    TextView weatherTitle;
    TextView weatherResultTextView;
    TextView weatherBackButton;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private int queryMode;//当前选中的级别
    private boolean hasCity = false;
    private boolean hasCounty = false;
    private boolean hasProvince = false;
    private String weatherId;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROVINCE_READY:
                    if(hasProvince)
                        queryCities();
                    break;
                case CITY_READY:
                    if(hasProvince && hasCity)
                        queryCounties();
                    break;
                case COUNTY_READY:
                    if((queryMode == CITY_ONLY && hasCity) || (queryMode == CITY_AND_COUNTY && hasCounty))
                        queryWeather();
                    break;
                case PROVINCE_FAILED:
                    Toast.makeText(WeatherActivity.this, "该省份不存在", Toast.LENGTH_SHORT).show();
                    break;
                case CITY_FAILED:
                    Toast.makeText(WeatherActivity.this, "该城市不存在", Toast.LENGTH_SHORT).show();
                    break;
                case COUNTY_FAILED:
                    Toast.makeText(WeatherActivity.this, "该地区不存在", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initView();
    }

    private void initView(){
        weatherBackButton = findViewById(R.id.weather_back_button);
        weatherBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeatherActivity.this.finish();
            }
        });
        weatherResultTextView = findViewById(R.id.weather_result_text_view);
        weatherTitle = findViewById(R.id.weather_title);
        provinceEditText = findViewById(R.id.province_edit_text);
        cityEditText = findViewById(R.id.city_edit_text);
        countyEditText = findViewById(R.id.county_edit_text);
        queryWeatherButton = (TextView)findViewById(R.id.query_weather_button);
        queryWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String province = provinceEditText.getText().toString();
                String city = cityEditText.getText().toString();
                String county = countyEditText.getText().toString();
                if(city.trim().length() == 0 || province.trim().length() == 0) {
                    Toast.makeText(WeatherActivity.this,"省份和城市皆不能为空",Toast.LENGTH_SHORT).show();
                } else if(city.trim().length() != 0 && county.trim().length() == 0) {
                    queryMode = CITY_ONLY;
                    hasProvince = false;
                    hasCity = false;
                    weatherId = "";
                    query();
                } else if(city.trim().length() != 0 && county.trim().length() != 0) {
                    queryMode = CITY_AND_COUNTY;
                    hasProvince = false;
                    hasCity = false;
                    weatherId = "";
                    query();
                }
            }
        });

    }

    private void query(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                queryProvinces();
            }
        }).start();
    }

    private void queryWeather(){
        if(weatherId.length() != 0){

            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=4470f45f7b27423294d546ef951460bc";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseText = response.body().string();
                    final Weather weather = WeatherUtil.handleWeatherResponse(responseText);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(weather != null && "ok".equals(weather.status)){
                                showWeatherText(weather);
                            }
                        }
                    });
                }
            });

        }
    }

    private void queryProvinces() {
        provinceList = LitePal.findAll(Province.class);
        if(provinceList.size() > 0) {
            provinceList = LitePal.where("provincename = ?", provinceEditText.getText().toString()).find(Province.class);
            if(provinceList.size() > 0) {
                hasProvince = true;
                selectedProvince = provinceList.get(0);
                Message message = handler.obtainMessage();
                message.what = PROVINCE_READY;
                handler.sendMessage(message);
            } else {
                hasProvince = false;
                Message message = handler.obtainMessage();
                message.what = PROVINCE_FAILED;
                handler.sendMessage(message);
            }

        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, QUERY_PROVINCE);
        }
    }

    private void queryCities() {
        if(hasProvince){
            cityList = LitePal.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
            if(cityList.size() > 0) {
                cityList = LitePal.where("cityname = ? and provinceid = ?", cityEditText.getText().toString(), String.valueOf(selectedProvince.getId())).find(City.class);
                if(cityList.size() > 0) {
                    selectedCity = cityList.get(0);
                    hasCity = true;
                    Message message = handler.obtainMessage();
                    message.what = CITY_READY;
                    handler.sendMessage(message);
                } else {
                    hasCity = false;
                    Message message = handler.obtainMessage();
                    message.what = CITY_FAILED;
                    handler.sendMessage(message);
                }
            } else {//如果本地没有数据则从网上获取
                int provinceCode = selectedProvince.getProvinceCode();
                String address = "http://guolin.tech/api/china/" + provinceCode;
                queryFromServer(address, QUERY_CITY);
            }
        }
    }

    private void queryCounties() {
        if(hasProvince && hasCity) {
            countyList = LitePal.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
            if(countyList.size() > 0) {
                if(queryMode == CITY_AND_COUNTY) {
                    countyList = LitePal.where("countyname = ? and cityid = ?", countyEditText.getText().toString(), String.valueOf(selectedCity.getId())).find(County.class);
                } else if(queryMode == CITY_ONLY) {
                    countyList = LitePal.where("countyname = ? and cityid = ?", cityEditText.getText().toString(), String.valueOf(selectedCity.getId())).find(County.class);
                }
                if(countyList.size() > 0) {
                    selectedCounty = countyList.get(0);
                    if(queryMode == CITY_AND_COUNTY) {
                        hasCounty = true;
                    }
                    weatherId = selectedCounty.getWeatherId();
                    Message message = handler.obtainMessage();
                    message.what = COUNTY_READY;
                    handler.sendMessage(message);
                } else {
                    hasCounty = false;
                    Message message = handler.obtainMessage();
                    message.what = COUNTY_FAILED;
                    handler.sendMessage(message);
                }
            } else {
                int provinceCode = selectedProvince.getProvinceCode();
                int cityCode = selectedCity.getCityCode();
                String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
                queryFromServer(address, QUERY_COUNTY);
            }
        }
    }

    private void queryFromServer(String address, final int type){
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                switch (type){
                    case QUERY_PROVINCE:
                        result = WeatherUtil.handleProvinceResponse(responseText);
                        break;
                    case QUERY_CITY:
                        result = WeatherUtil.handleCityResponse(responseText, selectedProvince.getId());
                        break;
                    case QUERY_COUNTY:
                        result = WeatherUtil.handleCountyResponse(responseText, selectedCity.getId());
                        break;
                    default:
                }

                if(result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (type){
                                case QUERY_PROVINCE:
                                    queryProvinces();
                                    break;
                                case QUERY_CITY:
                                    queryCities();
                                    break;
                                case QUERY_COUNTY:
                                    queryCounties();
                                    break;
                                default:
                            }
                        }
                    });
                }
            }
        });
    }

    private void showWeatherText(Weather weather) {
        String text = "城市名：" + weather.basic.cityName + "\n更新时间：" + weather.basic.update.updateTime.split(" ")[1]
                + "\n温度：" + weather.now.temperature + "\n天气：" + weather.now.more.info;

        if(weather.aqi != null) {
            text += ("\n空气质量指数：" + weather.aqi.city.aqi + "\nPM2.5指数：" + weather.aqi.city.pm25);
        }

        text += "\n天气预报：";

        for(ForeCast foreCast : weather.foreCastList){
            text += ("\n日期：" + foreCast.date + "    最高温：" + foreCast.temperature.max + "    最低温：" + foreCast.temperature.min + "\n天气：" + foreCast.more.info);
        }

        text += ("\n\n舒适度：" + weather.suggestion.comfort.info + "\n\n洗车指数：" + weather.suggestion.carWash.info + "\n\n运动建议：" + weather.suggestion.sport.info + "\n\n\n");

        weatherResultTextView.setText(text);


    }

}
