package com.nbicc.libinject;
import android.view.View;

public class Utils {
    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewById(View view, int viewId) {
        return (T)view.findViewById(viewId);
    }
}
