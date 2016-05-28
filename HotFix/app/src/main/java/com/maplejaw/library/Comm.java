package com.maplejaw.library;

import android.content.Context;
import android.graphics.drawable.Drawable;

public interface Comm {
    int function(int a, int b);
    void startPluginActivity(Context context, Class<?> cls);
    void startPluginActivity(Context context);
    void startMainActivity(Context context);
    Drawable getImage(Context context);
    int getImageId();
}