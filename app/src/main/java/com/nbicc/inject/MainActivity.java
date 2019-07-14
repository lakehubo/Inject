package com.nbicc.inject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.nbicc.libinject.InjectView;
import com.nbicc.libinject.Unbinder;

public class MainActivity extends AppCompatActivity {
    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 为 tv 赋值
        unbinder = InjectView.bind(this);
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
