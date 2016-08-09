package com.dl7.butterknifelib;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by long on 2016/8/9.
 */
public class ButterKnife {

    static final Map<Class<?>, ViewBinder<Object>> BINDERS = new LinkedHashMap<>();


    private ButterKnife() {
        throw new AssertionError("No instances.");
    }


    public static void bind(@NonNull Activity target) {
        bind(target, target, Finder.ACTIVITY);
    }

    /**
     * 资源绑定
     * @param target 目标
     * @param source 来源：activity、dialog 或 view
     * @param finder 辅助查找的工具，配合source使用
     */
    static void bind(@NonNull Object target, @NonNull Object source, @NonNull Finder finder) {
        Class<?> targetClass = target.getClass();
        try {
            ViewBinder<Object> viewBinder = findViewBinderForClass(targetClass);
            if (viewBinder != null) {
                // 执行bind方法进行资源绑定
                viewBinder.bind(finder, target, source);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to bind views for " + targetClass.getName(), e);
        }
    }

    /**
     * 通过目标Class找到对应的ViewBinder
     * @param cls Class
     * @return ViewBinder
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static ViewBinder<Object> findViewBinderForClass(Class<?> cls)
            throws IllegalAccessException, InstantiationException {
        ViewBinder<Object> viewBinder = BINDERS.get(cls);
        if (viewBinder != null) {
            return viewBinder;
        }
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            return null;
        }
        try {
            Class<?> viewBindingClass = Class.forName(clsName + "$$ViewBinder");
            //noinspection unchecked
            viewBinder = (ViewBinder<Object>) viewBindingClass.newInstance();
        } catch (ClassNotFoundException e) {
            viewBinder = findViewBinderForClass(cls.getSuperclass());
        }
        BINDERS.put(cls, viewBinder);
        return viewBinder;
    }
}
