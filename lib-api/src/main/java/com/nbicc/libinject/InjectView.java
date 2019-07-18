package com.nbicc.libinject;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class InjectView {

    public static void bind(@NonNull Activity activity) {
        try {
            Class<?> bindClassName = Class.forName(activity.getClass().getName() + "_ViewBinding");
            Constructor<?> bindConstructor = bindClassName.getDeclaredConstructor(activity.getClass());
            bindConstructor.newInstance(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unbind(@NonNull Activity activity) {
        try {
            Class<?> bindClassName = Class.forName(activity.getClass().getName() + "_ViewBinding");
            Method unbind = bindClassName.getDeclaredMethod("unbind");
            unbind.invoke(bindClassName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
