package com.dl7.butterknife;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dl7.butterknife.test.B;
import com.dl7.butterknifelib.ButterKnife;

import java.util.List;

import butterknife.annotation.Bind;
import butterknife.annotation.BindColor;
import butterknife.annotation.BindString;

public class MainActivity extends AppCompatActivity {

    @BindString(R.string.activity_string)
    String mBindString;
    @BindColor(R.color.colorAccent)
    int mBindColor;
    @BindColor(R.color.sel_btn_text)
    ColorStateList mBtnTextColor;
//    @BindString(R.string.bind_string)
//    private int mBindInt;

    @Bind(R.id.tv_desc)
    TextView textView;
    @Bind(R.id.fl_view)
    FrameLayout view;
    @Bind({R.id.btn_one, R.id.btn_two, R.id.btn_three})
    List<Button> mButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        TextView textView = (TextView) findViewById(R.id.tv_desc);
        textView.setText(mBindString);

//        FrameLayout view = (FrameLayout) findViewById(R.id.fl_view);
        view.setBackgroundColor(mBindColor);

        mButtons.get(0).setTextColor(mBtnTextColor);
        mButtons.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Button Two", Toast.LENGTH_SHORT).show();
            }
        });
//        Button btnDialog = (Button) findViewById(R.id.btn_dialog);
//        btnDialog.setTextColor(mBtnTextColor);
//        btnDialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Dialog", Toast.LENGTH_SHORT).show();
//            }
//        });

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.fl_fragment, new SampleFragment());
        transaction.commit();

        _testExtendClass();
    }

    private void _testExtendClass() {
        B b = new B(this);
        Log.e("MainActivity", b.getBString());
        Log.w("MainActivity", b.getAString());
    }


    /**
     * 初始化 Toolbar
     *
     * @param toolbar
     * @param homeAsUpEnabled
     * @param title
     */
    public void initToolBar(Toolbar toolbar, boolean homeAsUpEnabled, String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(homeAsUpEnabled);
    }
}
