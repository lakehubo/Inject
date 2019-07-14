package com.nbicc.libinject;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.lang.reflect.Constructor;

public class InjectView {

    public static Unbinder bind(@NonNull Activity activity) {
        try {
            Class<? extends Unbinder> bindClassName = (Class<? extends Unbinder>)
                    Class.forName(activity.getClass().getName() + "_ViewBinding");
            // 构造函数
            Constructor<? extends Unbinder> bindConstructor = bindClassName.getDeclaredConstructor(activity.getClass());
            Unbinder unbinder = bindConstructor.newInstance(activity);
            // 返回 Unbinder
            return unbinder;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Unbinder.EMPTY;
    }
}
