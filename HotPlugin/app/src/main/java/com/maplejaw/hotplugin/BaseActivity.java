package com.maplejaw.hotplugin;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;

import java.lang.reflect.Method;

public class BaseActivity extends Activity {
  
    private static final String TAG = "Client-BaseActivity";  

    public static final String EXTRA_DEX_PATH = "extra_dex_path";
    public static final String EXTRA_ACTIVITY_NAME = "extra_activity_name";



    protected Activity that;



    /**
     * 将代理Activity传给插件Activity
     * @param proxyActivity
     */
    public void setProxy(Activity proxyActivity) {
        that = proxyActivity;
    }  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {
        String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/2.apk";
        loadResources(path);
        super.onCreate(savedInstanceState);

    }

    /**
     * 启动Activity
     * @param className
     */
    protected void startActivityByProxy(String className) {
     /*   Intent intent = new Intent(PROXY_VIEW_ACTION);
        intent.putExtra(EXTRA_DEX_PATH, DEX_PATH);
        intent.putExtra(EXTRA_CLASS, className);
        mProxyActivity.startActivity(intent);*/
    }  
  


   /* @Override
    public void setContentView(int layoutResID) {
        that.setContentView(layoutResID);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }*/




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

}