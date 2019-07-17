package com.nbicc.libannotation;

import javax.lang.model.element.Element;

/**
 * 被注解的广播
 */
public class BindBroadCast extends BaseBinding{
    private String[] filter;
    private String methodName;

    public BindBroadCast(Element element, String[] filter, String methodName) {
        super(element);
        this.filter = filter;
        this.methodName = methodName;
    }

    public String[] getFilter() {
        return filter;
    }

    public void setFilter(String[] filter) {
        this.filter = filter;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
