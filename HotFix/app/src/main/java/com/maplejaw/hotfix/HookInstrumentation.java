package com.maplejaw.hotfix;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.lang.reflect.Method;

/**
 * @author maplejaw
 * @version 1.0, 2016/6/3
 */
public class HookInstrumentation extends Instrumentation {
    private ClassLoader mClassLoader;
    private Instrumentation mBase;
    public HookInstrumentation(Instrumentation instrumentation,ClassLoader classLoader){
        this.mClassLoader=classLoader;
        this.mBase=instrumentation;
    }
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        boolean isPlugin=false;
        if (intent != null) {
            isPlugin = intent.getBooleanExtra(HookUtil.EXTRA_ACTIVITY_FROM_PLUGIN, false);
        }
        if (isPlugin && intent != null) {
            cl=mClassLoader;
            className = intent.getStringExtra(HookUtil.EXTRA_ACTIVITY_NAME);
        }
        return super.newActivity(cl, className, intent);
    }

    /**
     * 覆盖掉原始Instrumentation类的对应方法,用于插件内部跳转Activity时适配
     *
     *
     */
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        wrapIntent(who, intent);//如果是启动插件，则替换intent
        try {
            // 由于这个方法是隐藏的,因此需要使用反射调用;首先找到这个方法
            Method execStartActivity = Instrumentation.class.getDeclaredMethod(
                    "execStartActivity", Context.class, IBinder.class, IBinder.class,
                    Activity.class, Intent.class, int.class, Bundle.class);
            execStartActivity.setAccessible(true);
            return (ActivityResult) execStartActivity.invoke(mBase, who,
                    contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("do not support!!!" + e.getMessage());
        }
    }


    private void wrapIntent(Context who,Intent intent){
        String className=intent.getComponent().getClassName();
        if(!className.startsWith("com.maplejaw.hotfix")){
            intent.setComponent(new ComponentName(who,MainActivity.class));
            intent.putExtra(HookUtil.EXTRA_ACTIVITY_FROM_PLUGIN, true);
            intent.putExtra(HookUtil.EXTRA_ACTIVITY_NAME, className);
        }


    }
}
