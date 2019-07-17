package com.nbicc.libannotation;

import javax.lang.model.element.Element;

/**
 * 被注解的method方法
 */
public class BindClickMethod extends BaseBinding {
    private int[] rids;
    private String methodName;

    public BindClickMethod(Element element, int[] rids, String methodName) {
        super(element);
        this.rids = rids;
        this.methodName = methodName;
    }

    public int[] getRids() {
        return rids;
    }

    public void setRids(int[] rids) {
        this.rids = rids;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String name) {
        this.methodName = name;
    }

}
