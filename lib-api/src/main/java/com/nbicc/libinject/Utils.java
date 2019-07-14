package com.nbicc.libinject;

import android.app.Activity;
import android.view.View;

public class Utils {
    public static <T extends View> T findViewById(Activity activity, int viewId) {
        return (T) activity.findViewById(viewId);
    }
}
