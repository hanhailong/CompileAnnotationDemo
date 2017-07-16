package com.hhl.ci_api;

/**
 * Created by HanHailong on 2017/7/16.
 */

public interface ViewProxy<T> {
    void inject(T t, Object source);
}
