package com.nbicc.inject;

import android.os.Bundle;
import android.widget.TextView;

import com.nbicc.libbindview.BindView;

public class TestActivity extends MainActivity {
    @BindView(R.id.tv)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv.setText("I am injected");
    }
}
