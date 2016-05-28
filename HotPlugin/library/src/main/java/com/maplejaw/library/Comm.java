package com.maplejaw.library;

import android.content.Context;

/**
 * @author maplejaw
 * @version 1.0, 2016/5/24
 */
public interface Comm {
    int function(int a, int b);
    void startPluginActivity(Context context, Class<?> cls);
}
