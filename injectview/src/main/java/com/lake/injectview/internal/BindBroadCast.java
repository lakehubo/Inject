package com.lake.injectview.internal;

import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static com.lake.injectview.internal.BindingObject.CONTEXT;
import static com.lake.injectview.internal.BindingObject.INTENT;

/**
 * 被注解的广播
 */
public class BindBroadCast extends BaseBinding {

    private String[] filter;
    private String methodName;
    private boolean context;
    private boolean intent;

    public BindBroadCast(Element element, String[] filter, String methodName) {
        super(element);
        this.filter = filter;
        this.methodName = methodName;
        if (element instanceof ExecutableElement) {
            ExecutableElement executableElement = (ExecutableElement) element;
            List<? extends VariableElement> params = executableElement.getParameters();
            if (params.size() != 0) {
                for(VariableElement v : params){
                    TypeMirror typeMirror = v.asType();
                    TypeName typeName = TypeName.get(typeMirror);
                    if(CONTEXT.equals(typeName)){
                        context = true;
                    }
                    if(INTENT.equals(typeName)){
                        intent = true;
                    }
                }
            }
        }
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

    public boolean isContext() {
        return context;
    }

    public void setContext(boolean context) {
        this.context = context;
    }

    public boolean isIntent() {
        return intent;
    }

    public void setIntent(boolean intent) {
        this.intent = intent;
    }
}
