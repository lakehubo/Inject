package com.nbicc.inject;

import android.os.Bundle;
import android.widget.TextView;

import com.nbicc.libbindview.BindView;


public class Test2Activity extends BaseActivity {
    @BindView(R.id.tv6)
    TextView tv6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_test2);
        super.onCreate(savedInstanceState);
        tv6.setText("i am inject2");
    }
}
