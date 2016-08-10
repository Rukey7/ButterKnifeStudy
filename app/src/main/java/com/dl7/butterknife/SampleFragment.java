package com.dl7.butterknife;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dl7.butterknifelib.ButterKnife;

import butterknife.annotation.BindString;

/**
 * Created by long on 2016/8/10.
 *
 */
public class SampleFragment extends Fragment {

    @BindString(R.string.fragment_string)
    String mBindString;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sample, null);
        ButterKnife.bind(this, view);

        TextView textView = (TextView) view.findViewById(R.id.tv_content);
        textView.setText(mBindString);
        return view;
    }
}
