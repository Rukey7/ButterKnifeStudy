package com.dl7.butterknife.test;

import android.app.Activity;

import com.dl7.butterknife.R;
import com.dl7.butterknifelib.ButterKnife;

import butterknife.annotation.BindString;

/**
 * Created by long on 2016/8/10.
 */
public class A {

    @BindString(R.string.a_string)
    String mAString;

    public A(Activity activity) {
        ButterKnife.bind(this, activity);
    }

    public String getAString() {
        return mAString;
    }
}
