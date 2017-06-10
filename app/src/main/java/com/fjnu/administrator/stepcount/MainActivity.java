package com.fjnu.administrator.stepcount;

import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.edu.fjnu.stepcount.view.CycleView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Handler;

import step.Constant;
import step.utils.SharedPreferencesUtils;
import step.utils.StepCountModeDispatcher;

/*
计步器主页
 */
public class MainActivity extends AppCompatActivity implements android.os.Handler.Callback{

    private CycleView cv;
    private SharedPreferencesUtils sp;
    private TextView tv_support;

    private boolean isBind = false;
    private Messenger mGetReplyMessenger = new Messenger(new android.os.Handler(this));
    private Messenger messenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        initData();
    }
    /*
     绑定控件
     */
    private void assignViews()
    {
            cv=(CycleView) findViewById(R.id.cv);
        tv_support=(TextView) findViewById(R.id.tv_isSupport);
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
            new android.os.Handler(this);
            //setupService();
        } else {
            /*tartService(new Intent(this, StepsDetectService.class));
            StepsDetectService.setOnStepDetectListener(new StepsDetectService.OnStepDetectListener() {
                @Override
                public void onStepDetect(int steps) {
                    JSONObject ret = new JSONObject();
                    try {
                        ret.put("steps", steps);
                        tv_isSupport.setText(steps);
                        Logger.d("steps=" + steps);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            */
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
                break;
        }
        return false;
    }

}
