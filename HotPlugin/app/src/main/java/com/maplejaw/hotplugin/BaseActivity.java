package com.maplejaw.hotplugin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;

import java.lang.reflect.Method;

public class BaseActivity extends Activity {
  

    public static final String EXTRA_DEX_PATH = "extra_dex_path";
    public static final String EXTRA_ACTIVITY_NAME = "extra_activity_name";


    //指向代理activity
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
        //String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/2.apk";
        //loadResources(path);

    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onResume() {

    }

    @Override
    protected void onPause() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onRestart() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    protected void onDestroy() {

    }


    /* @Override
    public void setContentView(int layoutResID) {
        that.setContentView(layoutResID);
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