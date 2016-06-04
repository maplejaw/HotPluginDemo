package com.maplejaw.hotfix;

import android.app.Instrumentation;
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




    public void btnClick(View view){
       //获取插件路径
        String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/2.apk";

        File codeDir=getDir("dex", Context.MODE_PRIVATE);
        //创建类加载器，把dex加载到虚拟机中
        ClassLoader classLoader = new DexClassLoader(path,codeDir.getAbsolutePath() ,null,
                this.getClass().getClassLoader());

        //获得包管理器
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo=pm.getPackageArchiveInfo(path,PackageManager.GET_ACTIVITIES);
        String packageName=packageInfo.packageName;

        switch (view.getId()){
            case R.id.btn1://hook classloader,需要在清单中声明
                hookClassLoader(classLoader);
                try {
                    startActivity(new Intent(this,classLoader.loadClass(packageName+".PluginActivity")));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case R.id.btn2://hook pathlist,需要在清单中声明
                 hookPathList(classLoader);

                try {
                    startActivity(new Intent(this,classLoader.loadClass(packageName+".PluginActivity")));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case R.id.btn3: //ProxyActivity



                //启动Activity
                Intent intent=new Intent(this,ProxyActivity.class);
                intent.putExtra(HookUtil.EXTRA_DEX_PATH,path);
                intent.putExtra(HookUtil.EXTRA_ACTIVITY_NAME,packageName+".TestProxyActivity");

                startActivity(intent);
                break;
            case R.id.btn4://hook instrumentation

                hookInstrumentation(classLoader);

                //启动activity
                Intent intent1=new Intent(this,MainActivity.class);
                intent1.putExtra(HookUtil.EXTRA_ACTIVITY_FROM_PLUGIN,true);
                intent1.putExtra(HookUtil.EXTRA_ACTIVITY_NAME,packageName+".PluginActivity");
                startActivity(intent1);
                break;
            case R.id.btn5://loadResource
                loadResources(path);
                try {
                    Class<?> clazz = classLoader.loadClass(packageName+".PluginClass");
                    Comm obj = (Comm) clazz.newInstance();
                    mImageView.setImageDrawable(obj.getImage(this));

                } catch (Exception e) {
                    e.printStackTrace();
                }
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


    /**
     * hook Instrumentation
     * @param dLoader
     */
    private void hookInstrumentation(ClassLoader dLoader){
        try {

            //获取ActivityThread的Class
            Class<?> activityThreadCls = Class.forName("android.app.ActivityThread");
            //获取ActivityThread对象
            Method currentActivityThreadMethod=activityThreadCls.getMethod("currentActivityThread");
            Object currentActivityThread= currentActivityThreadMethod.invoke(null);

            // 反射获取Instrumentation
            Field mInstrumentationField = activityThreadCls.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
             Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);

            //反射修改Instrumentation
            Instrumentation pluginInstrumentation = new HookInstrumentation(mInstrumentation,dLoader);
            mInstrumentationField.set(currentActivityThread, pluginInstrumentation);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * hook ClassLoder
     * @param dLoader
     */
    private void hookClassLoader(ClassLoader dLoader){
        try{
            String packageName = this.getPackageName();
            //获取LoadedApk的Class
            Class<?> loadApkCls =Class.forName("android.app.LoadedApk");
            //获取ActivityThread的Class
            Class<?> activityThreadCls =Class.forName("android.app.ActivityThread");

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
//            Field filed2=loadApkCls.getDeclaredField("mResDir");
//            filed2.setAccessible(true);
//            filed2.set(wr.get(),resPath);





        }catch(Exception e){
          e.printStackTrace();
        }


    }


    /**
     * hook PathList
     * 合并dexElement到宿主的PathList
     * @param loader
     */
    private void hookPathList(ClassLoader loader){
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 合并两个数组
     * @param arrayLhs 第一个数组
     * @param arrayRhs 第二个数组
     * @return
     */
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
