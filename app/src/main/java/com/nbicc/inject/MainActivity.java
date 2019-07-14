package com.nbicc.inject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.nbicc.libbindview.BindView;
import com.nbicc.libinject.InjectView;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.tv)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 为 tv 赋值
        InjectView.bind(this);
        tv.setText("I am injected");
    }
}
