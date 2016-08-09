package com.dl7.butterknifelib;

/**
 * Created by long on 2016/8/9.
 * 绑定接口
 */
public interface ViewBinder<T> {

    void bind(Finder finder, T target, Object source);
}
