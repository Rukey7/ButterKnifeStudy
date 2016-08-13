package com.dl7.butterknifelib;

/**
 * Created by long on 2016/8/9.
 * 绑定接口
 */
public interface ViewBinder<T> {

    /**
     * 处理绑定操作
     * @param finder 这个用来统一处理Activity、View、Dialog等查找 View 和 Context 的方法
     * @param target 进行绑定的目标对象
     * @param source 所依附的对象，可能是 target 本身，如果它是 Activity、View、Dialog 的话
     */
    void bind(Finder finder, T target, Object source);
}
