package com.nbicc.inject;

import android.os.Bundle;
import android.widget.TextView;

import com.nbicc.libbindview.BindView;

public class Test2Activity extends BaseActivity {
    @BindView(R.id.tv3)
    TextView tv3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_test2);
        super.onCreate(savedInstanceState);
        tv3.setText("i am inject2");
    }
}
