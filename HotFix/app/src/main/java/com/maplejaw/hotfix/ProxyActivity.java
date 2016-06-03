package com.maplejaw.hotfix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

public class ProxyActivity extends Activity {

    public static final String EXTRA_DEX_PATH = "extra_dex_path";
    public static final String EXTRA_ACTIVITY_NAME = "extra_activity_name";

    private String mClass;
    private String mDexPath;

    private Object mRemoteActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取插件dex路径
        mDexPath = getIntent().getStringExtra(EXTRA_DEX_PATH);
        //要启动的Activity的完整类名
        mClass = getIntent().getStringExtra(EXTRA_ACTIVITY_NAME);
        //加载资源
        loadResources(mDexPath);
        //启动插件Activity
        performLaunchActivity(savedInstanceState);
    }

    protected void performLaunchActivity(Bundle savedInstanceState) {
        File dexOutputDir = this.getDir("dex", Context.MODE_PRIVATE);
        //初始化classloader
        DexClassLoader dexClassLoader = new DexClassLoader(mDexPath,
                dexOutputDir.getAbsolutePath(), null, ClassLoader.getSystemClassLoader());

        //注意：以下只是把插件中的Activity当作一个普通的类进行反射调用
        try {
            Class<?> localClass = dexClassLoader.loadClass(mClass);
            instantiateLifecircleMethods(localClass);
            Constructor<?> localConstructor = localClass
                    .getConstructor();
            Object instance = localConstructor.newInstance();//初始化插件Acitivity对象。

            mRemoteActivity=instance;
            //获取插件Activity的setProxy方法
            Method setProxy = localClass.getMethod("setProxy",
                    Activity.class);
            setProxy.setAccessible(true);
            //调用插件Activity的setProxy方法
            setProxy.invoke(instance, this);//将ProxyActivity对象传给插件Activity

            //获取插件Activity中的onCreate方法。
            Method onCreate = localClass.getDeclaredMethod("onCreate", Bundle.class);
            onCreate.setAccessible(true);
            //调用插件Activity中的onCreate方法。
            onCreate.invoke(instance, savedInstanceState);//将savedInstanceState传给插件
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    //替换资源。
    private AssetManager mAssetManager;
    private Resources.Theme mTheme;
    protected void loadResources(String dexPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexPath);
            mAssetManager = assetManager;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Resources superRes = super.getResources();

        mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(),superRes.getConfiguration());
        mTheme = mResources.newTheme();
        mTheme.setTo(super.getTheme());
    }


    private Resources mResources;
    @Override
    public AssetManager getAssets() {
        return mAssetManager == null ? super.getAssets() : mAssetManager;
    }
    @Override
    public Resources getResources() {
        return mResources == null ? super.getResources() : mResources;
    }


    @Override
    protected void onStart() {
        Method method= mActivityLifecircleMethods.get("onStart");
        try {
            method.invoke(mRemoteActivity);
        } catch (Exception e) {
            e.printStackTrace();
       }
        super.onStart();
    }

    @Override
    protected void onResume() {
        Method method= mActivityLifecircleMethods.get("onResume");
        try {
            method.invoke(mRemoteActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Method method= mActivityLifecircleMethods.get("onPause");
        try {
            method.invoke(mRemoteActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    private HashMap<String,Method> mActivityLifecircleMethods=new HashMap<>();
    protected void instantiateLifecircleMethods(Class<?> localClass) {

        String[] methodNames = new String[] {
                "onRestart",
                "onStart",
                "onResume",
                "onPause",
                "onStop",
                "onDestory"
        };
        for (String methodName : methodNames) {
            Method method = null;
            try {
                method = localClass.getDeclaredMethod(methodName);
                method.setAccessible(true);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            mActivityLifecircleMethods.put(methodName, method);
        }

        Method onCreate = null;
        try {
            onCreate = localClass.getDeclaredMethod("onCreate", Bundle.class);
            onCreate.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        mActivityLifecircleMethods.put("onCreate", onCreate);

        Method onActivityResult = null;
        try {
            onActivityResult = localClass.getDeclaredMethod("onActivityResult",int.class,int.class,Intent.class);
            onActivityResult.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        mActivityLifecircleMethods.put("onActivityResult", onActivityResult);
    }

}