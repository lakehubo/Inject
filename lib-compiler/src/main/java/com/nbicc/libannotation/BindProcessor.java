package com.nbicc.libannotation;

import com.google.auto.service.AutoService;
import com.nbicc.libbindview.BindView;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

/**
 * 注解处理器
 */
@AutoService(Processor.class)
public class BindProcessor extends AbstractProcessor {
    static final String ACTIVITY_TYPE = "android.app.Activity";
    public static final String SUFFIX = "$$ViewBinder";
    // 存放同一个Class下的所有注解信息
    private Map<TypeElement, BindingObject> bindingMap = new HashMap<>();
    /**
     * 生成代码用的
     */
    private Filer filer;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        elementUtils = env.getElementUtils();
    }

    /**
     * 添加需要支持的注解
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        //添加需要支持的注解
        annotationTypes.add(BindView.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        bindingMap.clear();
        // 1、
        collectBindViewInfo(roundEnvironment);
        // 2、
        writeToFile();
        return true;
    }

    /**
     * 收集所有BindView注解信息
     *
     * @param roundEnvironment
     */
    private void collectBindViewInfo(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        Set<String> erasedTargetNames = new LinkedHashSet<String>();

        for (Element element : elements) {
            // 备注解元素所在的Class
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            // 收集Class中所有被注解的元素
            BindingObject bindingObject = bindingMap.get(enclosingElement);
            if (bindingObject == null) {
                String packageName = getPackageName(enclosingElement);
                String className = getClassName(enclosingElement, packageName);
                bindingObject = new BindingObject(enclosingElement, packageName, className);
                bindingMap.put(enclosingElement, bindingObject);
            }
            int rid = element.getAnnotation(BindView.class).value();
            String name = element.getSimpleName().toString();
            TypeMirror elementType = element.asType();
            TypeName typeName = TypeName.get(elementType);
            BindViewFiled bindViewFiled = new BindViewFiled(element, rid, name, typeName);
            bindingObject.addField(bindViewFiled);
            erasedTargetNames.add(enclosingElement.toString());
        }
        //检查是否有继承
        for (Map.Entry<TypeElement, BindingObject> entry : bindingMap.entrySet()) {
            String parentTypeElement = findParentFqcn(entry.getKey(), erasedTargetNames);
            if (parentTypeElement != null) {
                entry.getValue().setParentObject(parentTypeElement + SUFFIX);
            }
        }
    }

    /**
     * 生成注解方法
     */
    private void writeToFile() {
        for (Map.Entry<TypeElement, BindingObject> entry : bindingMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingObject bindingObject = entry.getValue();

            try {
                JavaFileObject jfo = filer.createSourceFile(bindingObject.getFqcn(), typeElement);
                Writer writer = jfo.openWriter();
                writer.write(bindingObject.brewJava());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Finds the parent binder type in the supplied set, if any.
     */
    private String findParentFqcn(TypeElement typeElement, Set<String> parents) {
        TypeMirror type;
        while (true) {
            type = typeElement.getSuperclass();
            if (type.getKind() == TypeKind.NONE) {
                return null;
            }
            typeElement = (TypeElement) ((DeclaredType) type).asElement();
            if (parents.contains(typeElement.toString())) {
                String packageName = getPackageName(typeElement);
                return packageName + "." + getClassName(typeElement, packageName);
            }
        }
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }
}
