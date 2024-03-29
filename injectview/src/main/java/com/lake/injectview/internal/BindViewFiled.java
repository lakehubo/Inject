package com.lake.injectview.internal;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;

/**
 * 被注解的View对象
 */
public class BindViewFiled extends BaseBinding {
    private int rid;
    private String name;
    private TypeName type;

    public BindViewFiled(Element element, int rid, String name, TypeName type) {
        super(element);
        this.rid = rid;
        this.name = name;
        this.type = type;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rids) {
        this.rid = rids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeName getType() {
        return type;
    }

    public void setType(TypeName type) {
        this.type = type;
    }
}
