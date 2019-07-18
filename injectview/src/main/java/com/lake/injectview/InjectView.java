package com.lake.injectview;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class InjectView {
    private static Map<Class<?>, ViewBinder> objectMap = new HashMap<>();


    public static void bind(Object activity) {
        try {
            Class<?> bindClassName = Class.forName(activity.getClass().getName() + "_ViewBinding");
            Constructor<?> bindConstructor = bindClassName.getDeclaredConstructor(activity.getClass());
            ViewBinder viewBinder = (ViewBinder) bindConstructor.newInstance(activity);
            viewBinder.bind();
            objectMap.put(bindClassName, viewBinder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unbind(Object activity) {
        try {
            Class<?> className = Class.forName(activity.getClass().getName() + "_ViewBinding");
            ViewBinder viewBinder = objectMap.get(className);
            if (viewBinder != null) {
                viewBinder.unbind();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface ViewBinder {
        void bind();
        void unbind();
    }

}
