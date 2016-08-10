package com.dl7.butterknife.test;

import android.app.Activity;

import com.dl7.butterknife.R;
import com.dl7.butterknifelib.ButterKnife;

import butterknife.annotation.BindString;

/**
 * Created by long on 2016/8/10.
 *
 */
public class B extends A {

    @BindString(R.string.b_string)
    String mBString;

    public B(Activity activity) {
        super(activity);
        ButterKnife.bind(this, activity);
    }

    public String getBString() {
        return mBString;
    }
}
