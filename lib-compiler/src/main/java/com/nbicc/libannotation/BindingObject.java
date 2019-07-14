package com.nbicc.libannotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.google.auto.common.MoreElements.getPackage;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * 被添加注解的类
 */
public class BindingObject {

    static final ClassName UTILS = ClassName.get("com.nbicc.libinject", "Utils");
    private static final ClassName UNBINDER = ClassName.get("com.nbicc.libinject", "Unbinder");
    private static final ClassName VIEW = ClassName.get("android.view", "View");
    private static final ClassName UI_THREAD =
            ClassName.get("android.support.annotation", "UiThread");
    private static final ClassName CONTEXT = ClassName.get("android.content", "Context");


    protected TypeElement enclosingElement;
    private final TypeName targetTypeName;
    private final ClassName bindingClassName;
    private final boolean isActivity;
    private final Set<BaseBinding> baseBindings;

    public BindingObject(TypeElement typeElement) {
        this.enclosingElement = typeElement;
        this.baseBindings = new HashSet<>();
        TypeMirror typeMirror = enclosingElement.asType();
        this.targetTypeName = TypeName.get(typeMirror);
        this.isActivity = true;
        String packageName = getPackage(enclosingElement).getQualifiedName().toString();
        String className = enclosingElement.getQualifiedName().toString().substring(
                packageName.length() + 1).replace('.', '$');
        bindingClassName = ClassName.get(packageName, className + "_ViewBinding");
    }

    JavaFile brewJava() {
        TypeSpec bindingTypeSpec = createTypeSpec();
        return JavaFile.builder(bindingClassName.packageName(), bindingTypeSpec).build();
    }

    private TypeSpec createTypeSpec() {
        TypeSpec.Builder result = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addModifiers(PUBLIC);

        result.addSuperinterface(UNBINDER);
        if (hasTargetField())
            result.addField(targetTypeName, "target", PRIVATE);
        if (isActivity)
            result.addMethod(createBindingConstructorForActivity());
        result.addMethod(createBindingConstructor());
        if (hasFieldBindings())
            result.addMethod(createBindingUnbindMethod(result));
        // 构建Class
        return result.build();
    }

    /**
     * 构建构造方法
     *
     * @return
     */
    private MethodSpec createBindingConstructor() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addAnnotation(UI_THREAD)
                .addModifiers(PUBLIC);

        constructor.addParameter(targetTypeName, "target");
        if(constructorNeedsView()){
            constructor.addParameter(VIEW, "source");
        }else{
            constructor.addParameter(CONTEXT, "context");
        }
        if (hasFieldBindings()) {
            constructor.addStatement("this.target = target");
            constructor.addCode("\n");
        }
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindViewFiled) {
                addViewBinding(constructor, baseBinding);
            }
        }
        return constructor.build();
    }

    /**
     * 解绑
     *
     * @param builder
     * @return
     */
    private MethodSpec createBindingUnbindMethod(TypeSpec.Builder builder) {
        MethodSpec.Builder result = MethodSpec.methodBuilder("unbind")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC);
        if (hasFieldBindings()) {
            result.addStatement("$T target = this.target", targetTypeName);
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

    private MethodSpec createBindingConstructorForActivity() {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addAnnotation(UI_THREAD)
                .addModifiers(PUBLIC)
                .addParameter(targetTypeName, "target");
        if (hasFieldBindings()) {
            builder.addStatement("this(target, target.getWindow().getDecorView())");
        } else {
            builder.addStatement("this(target, target)");
        }
        return builder.build();
    }

    protected void addField(BaseBinding baseBinding) {
        baseBindings.add(baseBinding);
    }

    private boolean constructorNeedsView() {
        return !baseBindings.isEmpty();
    }

    private boolean hasTargetField() {
        return hasFieldBindings();
    }

    /**
     * 是否有对象绑定
     *
     * @return
     */
    private boolean hasFieldBindings() {
        for (BaseBinding baseBinding : baseBindings) {
            if (baseBinding instanceof BindViewFiled)
                return true;
        }
        return false;
    }

    /**
     * 新增view绑定
     *
     * @param result
     * @param baseBinding
     */
    private void addViewBinding(MethodSpec.Builder result, BaseBinding baseBinding) {
        if (baseBinding instanceof BindViewFiled) {
            BindViewFiled fieldBinding = (BindViewFiled) baseBinding;
            CodeBlock.Builder builder = CodeBlock.builder()
                    .add("target.$L = ", fieldBinding.getName());
            builder.add("($T) ", fieldBinding.getType());
            builder.add("source.findViewById($L)", fieldBinding.getRid());
            result.addStatement("$L", builder.build());
        }
    }

}
