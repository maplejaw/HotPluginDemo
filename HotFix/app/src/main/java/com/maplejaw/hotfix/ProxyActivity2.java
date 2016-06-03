package com.maplejaw.hotfix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

public class ProxyActivity2 extends Activity {
  
    private static final String TAG = "JG";
  
    public static final String FROM = "extra.from";  
    public static final int FROM_EXTERNAL = 0;  //未安装被宿主启动
    public static final int FROM_INTERNAL = 1;  //已安装内部启动
  
    public static final String EXTRA_DEX_PATH = "extra.dex.path";  
    public static final String EXTRA_CLASS = "extra.class";  
  
    private String mClass;  
    private String mDexPath;

    private Object mPluginActivity;
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        mDexPath = getIntent().getStringExtra(EXTRA_DEX_PATH);  
        mClass = getIntent().getStringExtra(EXTRA_CLASS);
        loadResources(mDexPath);//加载资源
        Log.d(TAG, "mClass=" + mClass + " mDexPath=" + mDexPath);
        if (mClass == null) {  
            launchTargetActivity();  
        } else {  
            launchTargetActivity(mClass);  
        }  
    }  

    protected void launchTargetActivity() {  
        PackageInfo packageInfo = getPackageManager().getPackageArchiveInfo(
                mDexPath, PackageManager.GET_ACTIVITIES);
        if ((packageInfo.activities != null)  
                && (packageInfo.activities.length > 0)) {  
            String activityName = packageInfo.activities[0].name;  
            mClass = activityName;  
            launchTargetActivity(mClass);  
        }  
    }


    protected void launchTargetActivity(final String className) {  

        File dexOutputDir = this.getDir("dex", Context.MODE_PRIVATE);
        final String dexOutputPath = dexOutputDir.getAbsolutePath();  
        ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();  
        DexClassLoader dexClassLoader = new DexClassLoader(mDexPath,
                dexOutputPath, null, localClassLoader);  
        try {  
            Class<?> localClass = dexClassLoader.loadClass(className);  
            Constructor<?> localConstructor = localClass
                    .getConstructor(new Class[] {});
            mPluginActivity = localConstructor.newInstance(new Object[] {});

            instantiateLifecircleMethods(localClass);
  
            Method setProxy = localClass.getMethod("setProxy",
                    new Class[] { Activity.class });  
            setProxy.setAccessible(true);  
            setProxy.invoke(mPluginActivity, new Object[] { this });
  
            Method onCreate = localClass.getDeclaredMethod("onCreate",  
                    new Class[] { Bundle.class });  
            onCreate.setAccessible(true);  
            Bundle bundle = new Bundle();  
            bundle.putInt(FROM, FROM_EXTERNAL);  
            onCreate.invoke(mPluginActivity, new Object[] { bundle });
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

    @Override
    protected void onStart() {
        Method method= mActivityLifecircleMethods.get("onStart");
        if(method!=null){
            try {
                method.invoke(mPluginActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        Method method= mActivityLifecircleMethods.get("onResume");
        if(method!=null){
            try {
                method.invoke(mPluginActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Method method= mActivityLifecircleMethods.get("onStart");
        if(method!=null){
            try {
                method.invoke(mPluginActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        Method method= mActivityLifecircleMethods.get("onStop");
        if(method!=null){
            try {
                method.invoke(mPluginActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Method method= mActivityLifecircleMethods.get("onRestart");
        if(method!=null){
            try {
                method.invoke(mPluginActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Method method= mActivityLifecircleMethods.get("onDestroy");
        if(method!=null){
            try {
                method.invoke(mPluginActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}