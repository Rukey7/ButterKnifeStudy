package com.dl7.butterknifelib;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by long on 2016/8/9.
 * 提供资源绑定接口的类
 */
public class ButterKnife {

    static final Map<Class<?>, ViewBinder<Object>> BINDERS = new LinkedHashMap<>();


    private ButterKnife() {
        throw new AssertionError("No instances.");
    }


    /**
     * 绑定 Activity
     * @param target 目标为 Activity
     */
    public static void bind(@NonNull Activity target) {
        _bind(target, target, Finder.ACTIVITY);
    }
    /**
     * 绑定目标对象
     * @param target 目标为 Object
     * @param source 依赖 Activity
     */
    public static void bind(@NonNull Object target, @NonNull Activity source) {
        _bind(target, source, Finder.ACTIVITY);
    }

    /**
     * 绑定 View
     * @param target 目标为 View
     */
    public static void bind(@NonNull View target) {
        _bind(target, target, Finder.VIEW);
    }
    /**
     * 绑定目标对象
     * @param target 目标为 Object
     * @param source 依赖 View
     */
    public static void bind(@NonNull Object target, @NonNull View source) {
        _bind(target, source, Finder.VIEW);
    }

    /**
     * 绑定 Dialog
     * @param target 目标为 Dialog
     */
    public static void bind(@NonNull Dialog target) {
        _bind(target, target, Finder.DIALOG);
    }
    /**
     * 绑定目标对象
     * @param target 目标为 Object
     * @param source 依赖 Dialog
     */
    public static void bind(@NonNull Object target, @NonNull Dialog source) {
        _bind(target, source, Finder.DIALOG);
    }

    /**
     * 资源绑定
     * @param target 目标
     * @param source 来源：activity、dialog 或 view
     * @param finder 辅助查找的工具，配合source使用
     */
    private static void _bind(@NonNull Object target, @NonNull Object source, @NonNull Finder finder) {
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
            // 利用反射来生成对应 ViewBinder
            Class<?> viewBindingClass = Class.forName(clsName + "$$ViewBinder");
            viewBinder = (ViewBinder<Object>) viewBindingClass.newInstance();
        } catch (ClassNotFoundException e) {
            // 查找父类是否存在
            viewBinder = findViewBinderForClass(cls.getSuperclass());
        }
        BINDERS.put(cls, viewBinder);
        return viewBinder;
    }
}
