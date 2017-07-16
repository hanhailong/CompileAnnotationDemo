package com.hhl.compileannotationdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.hhl.ci_annotation.BindView;
import com.hhl.ci_api.ViewBind;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_content)
    TextView mContentTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewBind.bind(this);

        mContentTv.setText("Hello Class Annotations");
    }
}
