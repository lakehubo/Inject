package com.nbicc.injectview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lake.injectview.InjectView;

public abstract class BaseActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InjectView.bind(this);
    }


    @Override
    protected void onDestroy() {
        InjectView.unbind(this);
        super.onDestroy();
    }
}
