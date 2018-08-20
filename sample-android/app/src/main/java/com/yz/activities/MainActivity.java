package com.yz.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import com.yz.pluginstest.R;
import com.zhy.annotation.InjectView;
import com.zhy.processor.MyViewInjector;

public class MainActivity extends Activity {

    @InjectView(R.id.textView)
    TextView textView;
    @InjectView(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        MyViewInjector.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        textView.setText("123");
    }
}
