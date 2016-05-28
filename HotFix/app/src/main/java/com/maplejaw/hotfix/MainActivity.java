package com.maplejaw.hotfix;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.maplejaw.library.Comm;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class MainActivity extends AppCompatActivity {
    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView= (ImageView) this.findViewById(R.id.image);
    }



    private void useDexClassLoader(String path){

        File codeDir=getDir("dex", Context.MODE_PRIVATE);
        //创建类加载器，把dex加载到虚拟机中
        ClassLoader classLoader = new DexClassLoader(path,codeDir.getAbsolutePath() ,null,
                this.getClass().getClassLoader());

        replaceClassLoader(classLoader,path);
        //combinePathList(classLoader);
        //获得包管理器
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo=pm.getPackageArchiveInfo(path,PackageManager.GET_ACTIVITIES);
        String packageName=packageInfo.packageName;

        try {
            Class<?> clazz = classLoader.loadClass(packageName+".PluginClass");
            Comm obj = (Comm) clazz.newInstance();
            obj.startPluginActivity(this,classLoader.loadClass(packageName+".PluginActivity"));
          //  mImageView.setImageDrawable(obj.getImage(this));
          // obj.startMainActivity(this);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }




    public void btnClick(View view){

        switch (view.getId()){
            case R.id.btn1:
                //启动插件的页面
                String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/2.apk";
                loadResources(path);
                useDexClassLoader(path);
                break;
            case R.id.btn2:
                //启动自己的页面
                startActivity(new Intent(this,Main2Activity.class));
                break;

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
    public Resources.Theme getTheme() {
        return mTheme == null ? super.getTheme() : mTheme;


    }

    private void replaceClassLoader(ClassLoader dLoader,String resPath){
        try{
            String packageName = this.getPackageName();
            ClassLoader loader=ClassLoader.getSystemClassLoader();
            Class<?> loadApkCls =loader.loadClass("android.app.LoadedApk");
            Class<?> activityThreadCls =loader.loadClass("android.app.ActivityThread");

            //获取ActivityThread对象
            Method currentActivityThreadMethod=activityThreadCls.getMethod("currentActivityThread");
            Object currentActivityThread= currentActivityThreadMethod.invoke(null);
            //反射获取mPackages中的LoadedApk
            Field filed=activityThreadCls.getDeclaredField("mPackages");
            filed.setAccessible(true);
            Map mPackages= (Map) filed.get(currentActivityThread);
            WeakReference wr = (WeakReference) mPackages.get(packageName);
            //反射修改LoadedApk中的mClassLoader
            Field classLoaderFiled=loadApkCls.getDeclaredField("mClassLoader");
            classLoaderFiled.setAccessible(true);
            classLoaderFiled.set(wr.get(),dLoader);
            //反射修改LoadedApk中的资源目录
            Field filed2=loadApkCls.getDeclaredField("mResDir");
            filed2.setAccessible(true);
            filed2.set(wr.get(),resPath);

        }catch(Exception e){
          e.printStackTrace();
        }

    }


    /**
     * 以下是一种方式实现的
     * @param loader
     */
    private void combinePathList(ClassLoader loader){
        //获取系统的classloader
        PathClassLoader pathLoader = (PathClassLoader) getClassLoader();

        try {
            //反射dexpathlist
            Field pathListFiled = Class.forName("dalvik.system.BaseDexClassLoader").getDeclaredField("pathList");
            pathListFiled.setAccessible(true);
            //反射dexElements
            Field dexElementsFiled=Class.forName("dalvik.system.DexPathList").getDeclaredField("dexElements");
            dexElementsFiled.setAccessible(true);
            //获取系统的pathList
            Object pathList1= pathListFiled.get(pathLoader);
            //获取系统的dexElements
            Object dexElements1=dexElementsFiled.get(pathList1);

            //获取插件的pathlist
            Object pathList2= pathListFiled.get(loader);
            //获取插件的dexElements
            Object dexElements2=dexElementsFiled.get(pathList2);
            //合并dexElements
            Object combineDexElements=combineArray(dexElements1,dexElements2);
            //设置给系统的dexpathlist
            dexElementsFiled.set(pathList1,combineDexElements);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }
}
