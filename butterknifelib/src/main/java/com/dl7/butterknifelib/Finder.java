package com.dl7.butterknifelib;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

/**
 * 资源查找的处理类
 */
@SuppressWarnings("UnusedDeclaration") // Used by generated code.
public enum Finder {
    VIEW {
        @Override
        protected View findView(Object source, int id) {
            return ((View) source).findViewById(id);
        }

        @Override
        public Context getContext(Object source) {
            return ((View) source).getContext();
        }

        @Override
        protected String getResourceEntryName(Object source, int id) {
            final View view = (View) source;
            // In edit mode, getResourceEntryName() is unsupported due to use of BridgeResources
            if (view.isInEditMode()) {
                return "<unavailable while editing>";
            }
            return super.getResourceEntryName(source, id);
        }
    },
    ACTIVITY {
        @Override
        protected View findView(Object source, int id) {
            return ((Activity) source).findViewById(id);
        }

        @Override
        public Context getContext(Object source) {
            return (Activity) source;
        }
    },
    DIALOG {
        @Override
        protected View findView(Object source, int id) {
            return ((Dialog) source).findViewById(id);
        }

        @Override
        public Context getContext(Object source) {
            return ((Dialog) source).getContext();
        }
    };

    /**
     * findViewById，会进行 null 判断
     * @param source
     * @param id    资源ID
     * @param who   描述信息
     * @param <T>   转换类型
     * @return
     */
    public <T> T findRequiredView(Object source, int id, String who) {
        T view = findOptionalView(source, id, who);
        if (view == null) {
            String name = getResourceEntryName(source, id);
            throw new IllegalStateException("Required view '"
                    + name
                    + "' with ID "
                    + id
                    + " for "
                    + who
                    + " was not found. If this view is optional add '@Nullable' (fields) or '@Optional'"
                    + " (methods) annotation.");
        }
        return view;
    }

    /**
     * findViewById，不进行 null 判断
     */
    public <T> T findOptionalView(Object source, int id, String who) {
        View view = findView(source, id);
        return castView(view, id, who);
    }

    /**
     * 类型转换
     */
    @SuppressWarnings("unchecked") // That's the point.
    public <T> T castView(View view, int id, String who) {
        try {
            return (T) view;
        } catch (ClassCastException e) {
            if (who == null) {
                throw new AssertionError();
            }
            String name = getResourceEntryName(view, id);
            throw new IllegalStateException("View '"
                    + name
                    + "' with ID "
                    + id
                    + " for "
                    + who
                    + " was of the wrong type. See cause for more info.", e);
        }
    }

    /**
     * 参数类型转换
     */
    @SuppressWarnings("unchecked") // That's the point.
    public <T> T castParam(Object value, String from, int fromPosition, String to, int toPosition) {
        try {
            return (T) value;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Parameter #"
                    + (fromPosition + 1)
                    + " of method '"
                    + from
                    + "' was of the wrong type for parameter #"
                    + (toPosition + 1)
                    + " of method '"
                    + to
                    + "'. See cause for more info.", e);
        }
    }

    /**
     * 获取资源ID对应的名称: id=R.layout.activity_main -> "activity_main"
     * @param source
     * @param id
     * @return
     */
    protected String getResourceEntryName(Object source, int id) {
        return getContext(source).getResources().getResourceEntryName(id);
    }

    /**
     * findViewById
     * @param source
     * @param id
     * @return
     */
    protected abstract View findView(Object source, int id);

    /**
     * 获取Context
     * @param source
     * @return
     */
    public abstract Context getContext(Object source);
}
