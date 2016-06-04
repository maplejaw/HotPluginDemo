package com.maplejaw.hotplugin;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;

public class PluginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/2.apk";
        loadResources(path);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin);
        Log.i("JG",  "包名："+getPackageName());
        Log.w("JG",  "代码路径："+getPackageCodePath());
        Log.e("JG",  "资源路径："+getPackageResourcePath());

    }

    public void btnClick(View view){
        startActivity(new Intent(this,PluginActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("JG","onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("JG","onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("JG","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("JG","onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("JG","onDestroy");
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

}
