package com.maplejaw.hotfix;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

/**
 * @author maplejaw
 * @version 1.0, 2016/6/3
 */
public class HookInstrumentation extends Instrumentation {
    private ClassLoader mClassLoader;
    public HookInstrumentation(ClassLoader classLoader){
        this.mClassLoader=classLoader;
    }
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        boolean isPlugin=false;
        if (intent != null) {
            isPlugin = intent.getBooleanExtra(HookUtil.FLAG_ACTIVITY_FROM_PLUGIN, false);
        }
        if (isPlugin && intent != null) {
            cl=mClassLoader;
            className = intent.getStringExtra(HookUtil.FLAG_ACTIVITY_CLASS_NAME);
        }
        return super.newActivity(cl, className, intent);
    }

    /**
     * 覆盖掉原始Instrumentation类的对应方法,用于插件内部跳转Activity时适配
     *
     *
     */
/*
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        replaceIntentTargetIfNeed(who, intent);
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
    }*/
}
