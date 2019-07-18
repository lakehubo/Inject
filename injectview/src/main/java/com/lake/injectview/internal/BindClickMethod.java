package com.lake.injectview.internal;

import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static com.lake.injectview.internal.BindingObject.VIEW;

/**
 * 被注解的method方法
 */
public class BindClickMethod extends BaseBinding {

    private int[] rids;
    private String methodName;
    private boolean view;

    public BindClickMethod(Element element, int[] rids, String methodName) {
        super(element);
        this.rids = rids;
        this.methodName = methodName;
        if (element instanceof ExecutableElement) {
            ExecutableElement executableElement = (ExecutableElement) element;
            List<? extends VariableElement> params = executableElement.getParameters();
            if (params.size() != 0) {
                for(VariableElement v : params){
                    TypeMirror typeMirror = v.asType();
                    TypeName typeName = TypeName.get(typeMirror);
                    if(VIEW.equals(typeName)){
                        view = true;
                    }
                }
            }
        }
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

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }
}
