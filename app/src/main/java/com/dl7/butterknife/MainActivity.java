package com.dl7.butterknife;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dl7.butterknifelib.ButterKnife;

import butterknife.annotation.BindString;

public class MainActivity extends AppCompatActivity {

    @BindString(R.string.bind_string)
    String mBindString;

//    @BindString(R.string.bind_string)
//    private int mBindInt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        TextView textView = (TextView) findViewById(R.id.tv_desc);
        textView.setText(mBindString);
    }
}
