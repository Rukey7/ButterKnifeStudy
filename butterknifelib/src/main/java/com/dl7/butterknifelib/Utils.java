package com.dl7.butterknifelib;

import java.lang.reflect.Array;
import java.util.List;

@SuppressWarnings("deprecation") //
public final class Utils {

    private Utils() {
        throw new AssertionError("No instances.");
    }

    @SafeVarargs
    public static <T> T[] arrayOf(T... views) {
        return filterNull(views);
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... views) {
        return new ImmutableList<>(filterNull(views));
    }

    private static <T> T[] filterNull(T[] views) {
        int end = 0;
        int length = views.length;
        for (int i = 0; i < length; i++) {
            T view = views[i];
            if (view != null) {
                views[end++] = view;
            }
        }
        if (end == length) {
            return views;
        }
        //noinspection unchecked
        T[] newViews = (T[]) Array.newInstance(views.getClass().getComponentType(), end);
        System.arraycopy(views, 0, newViews, 0, end);
        return newViews;
    }
}