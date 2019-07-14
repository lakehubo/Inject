package com.nbicc.libannotation;

import javax.lang.model.element.Element;

/**
 * 注解对象基类
 */
public abstract class BaseBinding {
    private Element element;

    public BaseBinding(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }
}
