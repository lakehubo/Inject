package com.nbicc.inject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nbicc.libbindview.BindBroadcastReceiver;
import com.nbicc.libbindview.BindClick;
import com.nbicc.libbindview.BindView;

public class TestActivity extends BaseActivity {
    @BindView({R.id.tv, R.id.tv2})
    TextView tv, tv2;

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
        tv.setText("I am injected");
        tv2.setText("I am injected2");
        tv3.setText("I am injected3");
        tv4.setText("I am injected4");
        tv5.setText("I am injected5");
    }

    @BindClick(R.id.btn)
    public void setTvText(View view) {
        tv.setText("i am a click");
    }

    @BindBroadcastReceiver(Intent.ACTION_PACKAGE_REPLACED)
    public void reciver(Context context, Intent intent){
        Log.e("lake", "reciver: ");
    }
}
