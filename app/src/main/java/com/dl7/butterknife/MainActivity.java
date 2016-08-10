package com.dl7.butterknife;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dl7.butterknife.test.B;
import com.dl7.butterknifelib.ButterKnife;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        TextView textView = (TextView) findViewById(R.id.tv_desc);
        textView.setText(mBindString);

        FrameLayout view = (FrameLayout) findViewById(R.id.fl_view);
        view.setBackgroundColor(mBindColor);

        Button btnDialog = (Button) findViewById(R.id.btn_dialog);
        btnDialog.setTextColor(mBtnTextColor);

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
