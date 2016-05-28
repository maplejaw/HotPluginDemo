package com.maplejaw.hotplugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.maplejaw.library.Comm;


public class PluginClass implements Comm {
    public PluginClass() {
        Log.d("JG","初始化PluginClass");
    }


    @Override
    public int function(int a, int b) {
        return a+b;
    }

    @Override
    public void startPluginActivity(Context context, Class<?> cls) {
        Intent intent=new Intent(context,cls);
        context.startActivity(intent);
    }


    public void startMainActivity(Context context) {
        Intent intent=new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }

    public void startPluginActivity(Context context) {
        Intent intent=new Intent(context,PluginActivity.class);
        context.startActivity(intent);
    }

    public Drawable getImage(Context context){
       return context.getResources().getDrawable(R.drawable.a2);
    }

    public int getImageId(){
        return R.drawable.a;
    }

}