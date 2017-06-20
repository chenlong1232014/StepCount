package com.fjnu.administrator.stepcount;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import com.edu.fjnu.stepcount.view.CycleView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


import java.net.URL;

import step.Constant;
import step.service.StepService;
import step.utils.SharedPreferencesUtils;
import step.utils.StepCountModeDispatcher;
import weather.JSonUtils;
import weather.WeatherBean;

/*
计步器主页
 */
public class MainActivity extends AppCompatActivity implements Handler.Callback, View.OnClickListener{

    private static String url;
    private CycleView cv;
    private SharedPreferencesUtils sp;
    private TextView tv_support;
    private RatingBar ratingBar;
    private TextView evaluate;
    private TextView city;
    private TextView temp;
    private TextView weather;
    private ImageView weather_pic;
    private TextView date;

    private boolean isBind = false;
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));
    private Messenger messenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        initData();
        initUrl();
        GetWeatherInfo();

    }
    /*
     绑定控件
     */
    private void assignViews()
    {
        cv=(CycleView) findViewById(R.id.cv);
        tv_support=(TextView) findViewById(R.id.tv_isSupport);
        ratingBar=(RatingBar) findViewById(R.id.ratingBar);
        evaluate=(TextView) findViewById(R.id.evaluate);
        city=(TextView)findViewById(R.id.city);
        temp=(TextView)findViewById(R.id.temp);
        weather=(TextView)findViewById(R.id.weather);
        weather_pic=(ImageView)findViewById(R.id.weather_pic);
        date=(TextView)findViewById(R.id.date);
    }
    private void initData() {
        sp = new SharedPreferencesUtils(this);
        //获取用户设置的计划锻炼步数，没有设置过的话默认5000
        String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "5000");
        //设置当前步数为0
        cv.setCurrentCount(Integer.parseInt(planWalk_QTY), 0);
        //判断设置是否支持计步
        if (StepCountModeDispatcher.isSupportStepCountSensor(this)) {
            tv_support.setText("计步中...");
            new Handler(this);
            setupService();
        } else {
            tv_support.setText("该设备不支持计步");
        }
    }
    /**
     * 从service服务中拿到步数
     *
     * @param msg
     * @return
     */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.MSG_FROM_SERVER:
                String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "5000");
                cv.setCurrentCount(Integer.parseInt(planWalk_QTY), msg.getData().getInt("step"));
                TodayEvaluation(Integer.parseInt(planWalk_QTY), msg.getData().getInt("step"));
                break;
        }
        return false;
    }
    /**
     * 开启计步服务
     */
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }
    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    ServiceConnection conn = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                messenger = new Messenger(service);
                Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                msg.replyTo = mGetReplyMessenger;
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBind) {
            this.unbindService(conn);
        }
    }

    public void TodayEvaluation(Integer plan_step,Integer current_step)
    {
        float rate=((float)current_step/(float)plan_step)*5;
        ratingBar.setRating(rate);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_set:
                startActivity(new Intent(this, SetPlan.class));
                break;
            case R.id.tv_history:
                startActivity(new Intent(this, History.class));
                break;
        }
    }
    /*
    获取需要网址的url
     */
    public void initUrl()
    {
        try {
            String city = java.net.URLEncoder.encode("福州", "utf-8");
            //拼地址
            String apiUrl = String.format("http://www.sojson.com/open/api/weather/json.shtml?city=%s", city);
            url = apiUrl;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
    获取天气情况数据
     */
    public void GetWeatherInfo(){

        try {
            URL u=new URL(url);
            WeatherBean weatherBean = JSonUtils.getWeatherBean(u);
            String message="";
            message="当前所在城市： "+weatherBean.getCity()+"\n今天的天气情况是："+weatherBean.getWeather1()
                    +"\n今日平均气温："
                    +weatherBean.getTemp1()+"\n"+weatherBean.getDate();
            System.out.println(message);
            if(weatherBean.getCity()!=null) {
                city.setText(weatherBean.getCity());
                temp.setText(weatherBean.getTemp1() + "°");
                weather.setText(weatherBean.getWeather1());
                date.setText(weatherBean.getDate());
                setWeatherPic(weatherBean.getWeather1());
            }
            else
                Toast.makeText(MainActivity.this, "无法获取天气数据", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    设置天气图标
     */
    public void setWeatherPic(String weather)
    {
        switch(weather)
        {
            case "晴":
                weather_pic.setBackgroundResource(R.mipmap.sunny);
                break;
            case "多云":
                weather_pic.setBackgroundResource(R.mipmap.duoyun);
                break;
            case "阴":
                weather_pic.setBackgroundResource(R.mipmap.yintian);
                break;
            case "小雨":
                weather_pic.setBackgroundResource(R.mipmap.min_rain);
                break;
            case "中雨":
                weather_pic.setBackgroundResource(R.mipmap.mid_rain);
                break;
            case "大雨":
                weather_pic.setBackgroundResource(R.mipmap.heavy_rain);
                break;
            case "暴雨":
                weather_pic.setBackgroundResource(R.mipmap.rainstorm);
                break;
            case "雷阵雨":
                weather_pic.setBackgroundResource(R.mipmap.thunden_rain);
                break;
            default:
                weather_pic.setBackgroundResource(R.mipmap.sunny);
        }
    }
}
