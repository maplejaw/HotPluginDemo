package com.maplejaw.hotplugin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class PluginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin);
        Log.i("JG",  "包名："+getPackageName());
        Log.w("JG",  "代码路径："+getPackageCodePath());
        Log.e("JG",  "资源路径："+getPackageResourcePath());

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
}
