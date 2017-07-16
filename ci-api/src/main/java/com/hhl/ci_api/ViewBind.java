package com.hhl.ci_api;

import android.app.Activity;

/**
 * Created by HanHailong on 2017/7/16.
 */

public class ViewBind {

    public static final String SUFFIX = "$$ViewProxy";

    public static void bind(Activity activity) {
        ViewProxy viewProxy = findActivityViewProxy(activity);
        if (viewProxy == null) return;
        viewProxy.inject(activity, activity);
    }

    private static ViewProxy findActivityViewProxy(Activity activity) {
        Class clz = activity.getClass();

        try {
            Class proxyClz = Class.forName(clz.getName() + SUFFIX);
            return (ViewProxy) proxyClz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }
}
