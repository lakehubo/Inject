package com.nbicc.libannotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

// 被添加注解的类
final class BindingObject {

    private static final ClassName VIEW = ClassName.get("android.view", "View");
    private static final ClassName CLICK = ClassName.get("android.view", "View.OnClickListener");
    private static final ClassName UI_THREAD = ClassName.get("android.support.annotation", "UiThread");
    private static final ClassName BROADCAST = ClassName.get("android.content", "BroadcastReceiver");
    private static final ClassName CONTEXT = ClassName.get("android.content", "Context");
    private static final ClassName INTENT = ClassName.get("android.content", "Intent");
    private static final ClassName FILTER = ClassName.get("android.content", "IntentFilter");

    private final TypeName targetTypeName;
    private final Set<BaseBinding> baseBindings;//待绑定集合
    private final String packageName;//当前类包名
    private final String className;//当前类名

    BindingObject(TypeElement typeElement, String packageName, String className) {
        this.baseBindings = new HashSet<>();
        TypeMirror typeMirror = typeElement.asType();
        this.targetTypeName = TypeName.get(typeMirror);
        this.packageName = packageName;
        this.className = className + "_ViewBinding";
    }

    String getFqcn() {
        return packageName + "." + className;
    }

    String brewJava() {
        TypeSpec bindingTypeSpec = createTypeSpec();
        return JavaFile.builder(packageName, bindingTypeSpec).build().toString();
    }

    //新建类
    private TypeSpec createTypeSpec() {
        TypeSpec.Builder result = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC);
        if (hasTargetField())
            result.addField(targetTypeName, "target", PRIVATE);
        result.addMethod(createBindingConstructor());
        if (hasClickMethodBindings()) {
            result.addSuperinterface(CLICK);
            result.addMethod(createClickMethod());
        }
        if (hasBroadCastBindings()) {
            ClassName MYBROADCAST = ClassName.get(targetTypeName + "_ViewBinding", "MyBroadCastReceiver");
            result.addField(FILTER, "filter", PRIVATE);
            result.addField(MYBROADCAST, "myBroadCastReceiver", PRIVATE);
            result.addType(createBroadCastTypeSpec());
        }
        if (hasFieldBindings())
            result.addMethod(createBindingUnbindMethod());
        // 构建Class
        return result.build();
    }

    //创建广播内部类
    private TypeSpec createBroadCastTypeSpec() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("MyBroadCastReceiver")
                .superclass(BROADCAST)
                .addModifiers(PRIVATE);
        builder.addMethod(createOnReceiver());
        return builder.build();
    }

    //构建构造方法
    private MethodSpec createBindingConstructor() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addAnnotation(UI_THREAD)
                .addModifiers(PUBLIC);

        constructor.addParameter(targetTypeName, "target");

        if (hasFieldBindings()) {
            constructor.addStatement("this.target = target");
            constructor.addCode("\n");
        }
        if (hasBroadCastBindings()) {
            constructor.addStatement("filter = new IntentFilter()");
            addBroadCastFilter(constructor);
            constructor.addStatement("target.registerReceiver(myBroadCastReceiver,filter)");
        }
        addViewBinding(constructor);
        addClickBinding(constructor);
        return constructor.build();
    }

    //创建点击事件方法
    private MethodSpec createClickMethod() {
        MethodSpec.Builder result = MethodSpec.methodBuilder("onClick")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC);
        result.addParameter(VIEW, "view");
        result.addCode("switch(view.getId()){\n");
        for (BaseBinding baseBinding : baseBindings)
            addClickMethod(result, baseBinding);
        result.addStatement("default:\nbreak");
        result.addCode("}\n");
        return result.build();
    }

    //实现广播接收
    private MethodSpec createOnReceiver() {
        MethodSpec.Builder result = MethodSpec.methodBuilder("onReceive")
                .addAnnotation(Override.class)
                .addParameter(CONTEXT, "context")
                .addParameter(INTENT, "intent")
                .addModifiers(PUBLIC);
        addMethodByFilter(result);
        return result.build();
    }

    //广播截获
    private void addMethodByFilter(MethodSpec.Builder result) {
        Map<String, Set<String>> filters = new LinkedHashMap<>();
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindBroadCast) {
                BindBroadCast bindBroadCast = (BindBroadCast) baseBinding;
                String[] fs = bindBroadCast.getFilter();
                for (String s : fs) {
                    Set<String> method = filters.get(s);
                    if (method == null) {
                        method = new LinkedHashSet<>();
                        filters.put(s, method);
                    }
                    method.add(bindBroadCast.getMethodName());
                }
            }
        }
        if (filters.isEmpty())
            return;
        for (Map.Entry<String, Set<String>> map : filters.entrySet()) {
            String filter = map.getKey();
            Set<String> methods = map.getValue();
            if (filter == null || methods.isEmpty())
                continue;
            CodeBlock.Builder builder = CodeBlock.builder();
            builder.add("if($S.equals(intent.getAction())){\n", filter);
            for (String m : methods) {
                if (m != null)
                    builder.add("target.$N(context,intent);\n", m);
            }
            builder.add("}\n");
            result.addCode(builder.build());
        }
    }

    //解绑
    private MethodSpec createBindingUnbindMethod() {
        MethodSpec.Builder result = MethodSpec.methodBuilder("unbind")
                .addAnnotation(UI_THREAD)
                .addModifiers(PUBLIC);
        if (hasFieldBindings()) {
            result.addStatement("$T target = this.target", targetTypeName);
        }
        if (hasBroadCastBindings()) {
            result.addStatement("if (target != null)this.target.unregisterReceiver(myBroadCastReceiver)");
        }
        result.addStatement("if (target == null) throw new $T($S)", IllegalStateException.class,
                "Bindings already cleared.");
        result.addStatement("$N = null", hasFieldBindings() ? "this.target" : "target");
        result.addCode("\n");
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindViewFiled)
                result.addStatement("target.$L = null", ((BindViewFiled) baseBinding).getName());
        }
        return result.build();
    }

    protected void addField(BaseBinding baseBinding) {
        baseBindings.add(baseBinding);
    }

    private boolean hasTargetField() {
        return hasFieldBindings();
    }

    //是否有对象绑定
    private boolean hasFieldBindings() {
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindViewFiled)
                return true;
        }
        return false;
    }

    //是否有点击方法绑定
    private boolean hasClickMethodBindings() {
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindClickMethod)
                return true;
        }
        return false;
    }

    //是否有广播方法绑定
    private boolean hasBroadCastBindings() {
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindBroadCast)
                return true;
        }
        return false;
    }

    //新增view绑定 多view绑定
    private void addViewBinding(MethodSpec.Builder result) {
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindViewFiled) {
                BindViewFiled fieldBinding = (BindViewFiled) baseBinding;
                CodeBlock.Builder builder = CodeBlock.builder();
                int id = fieldBinding.getRid();
                String name = fieldBinding.getName();
                builder.add("target.$L = ", name);
                builder.add("($T) ", fieldBinding.getType());
                builder.add("target.findViewById($L)", id);
                result.addStatement("$L", builder.build());
            }
        }
    }

    //实现点击接口
    private void addClickBinding(MethodSpec.Builder result) {
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindClickMethod) {
                BindClickMethod clickBinding = (BindClickMethod) baseBinding;
                int[] rids = clickBinding.getRids();
                for (int id : rids) {
                    String name = findMethodFiled(id);
                    if (name == null) {
                        continue;
                    }
                    result.addStatement("target.$L.setFocusable(true)", name);
                    result.addStatement("target.$L.setClickable(true)", name);
                    result.addStatement("target.$L.setOnClickListener(this)", name);
                }
            }
        }
    }

    private String findMethodFiled(int id) {
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindViewFiled) {
                int rid = ((BindViewFiled) baseBinding).getRid();
                if (rid == id) {
                    return ((BindViewFiled) baseBinding).getName();
                }
            }
        }
        return null;
    }

    //新增方法
    private void addClickMethod(MethodSpec.Builder result, BaseBinding baseBinding) {
        if (baseBinding instanceof BindClickMethod) {
            BindClickMethod clickBinding = (BindClickMethod) baseBinding;
            CodeBlock.Builder builder = CodeBlock.builder();
            for (int id : clickBinding.getRids()) {
                builder.add("case $L:\n", id);
            }
            builder.add("{this.target.$L(view);", clickBinding.getMethodName());
            builder.add("break;}\n");
            result.addCode("$L", builder.build());
        }
    }

    /**
     * 新增广播过滤
     *
     * @param result
     */
    private void addBroadCastFilter(MethodSpec.Builder result) {
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindBroadCast) {
                BindBroadCast bindBroadCast = (BindBroadCast) baseBinding;
                for (String f : bindBroadCast.getFilter()) {
                    result.addStatement("filter.addAction($S)", f);
                }
            }
        }
    }

}
