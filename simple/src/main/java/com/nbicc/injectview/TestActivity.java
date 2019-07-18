package com.nbicc.injectview;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.lake.injectview.BindBroadcastReceiver;
import com.lake.injectview.BindClick;
import com.lake.injectview.BindView;

public class TestActivity extends BaseActivity {
    @BindView({R.id.tv, R.id.tv2})
    TextView text1, tv2;
    @BindView(R.id.tv3)
    TextView tv3;
    @BindView({R.id.tv4, R.id.tv5})
    TextView tv4, tv5;
    @BindView(R.id.btn)
    Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        text1.setText("I am injected");
        tv2.setText("I am injected2");
        tv3.setText("I am injected3");
        tv4.setText("I am injected4");
        tv5.setText("I am injected5");
    }

    @BindClick(R.id.btn)
    public void setTvText() {
        text1.setText("i am a click");
    }

    @BindBroadcastReceiver({WifiManager.WIFI_STATE_CHANGED_ACTION, ConnectivityManager.CONNECTIVITY_ACTION})
    public void wifiStateReceiver(Intent intent) {
        Log.e("lake", "reciver: ");
        tv2.setText("收到广播！");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
