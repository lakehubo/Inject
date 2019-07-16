package com.nbicc.inject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.nbicc.libinject.InjectView;

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
