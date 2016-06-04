package com.maplejaw.hotplugin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class TestProxyActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        that.setContentView(R.layout.activity_test);
        that.findViewById(R.id.btn).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Intent intent=new Intent(that,that.getClass());
        intent.putExtra(EXTRA_DEX_PATH,that.getIntent().getStringExtra(EXTRA_DEX_PATH));
        intent.putExtra(EXTRA_ACTIVITY_NAME,"com.maplejaw.hotplugin.TestProxyActivity");
        that.startActivity(intent);
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
