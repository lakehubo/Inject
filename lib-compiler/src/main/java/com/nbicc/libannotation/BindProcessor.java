package com.nbicc.libannotation;

import com.google.auto.service.AutoService;
import com.nbicc.libbindview.BindView;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
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
import javax.lang.model.type.TypeMirror;

/**
 * 注解处理器
 */
@AutoService(Processor.class)
public class BindProcessor extends AbstractProcessor {
    static final String ACTIVITY_TYPE = "android.app.Activity";

    // 存放同一个Class下的所有注解信息
    private Map<TypeElement, BindingObject> bindingMap = new HashMap<>();
    /**
     * 生成代码用的
     */
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
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
    void collectBindViewInfo(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            // 备注解元素所在的Class
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            // 收集Class中所有被注解的元素
            BindingObject bindingObject = bindingMap.get(enclosingElement);
            if (bindingObject == null) {
                bindingObject = new BindingObject(enclosingElement);
                bindingMap.put(enclosingElement, bindingObject);
            }
            int rid = element.getAnnotation(BindView.class).value();
            String name = element.getSimpleName().toString();
            TypeMirror elementType = element.asType();
            TypeName typeName = TypeName.get(elementType);
            BindViewFiled bindViewFiled = new BindViewFiled(element, rid, name, typeName);
            bindingObject.addField(bindViewFiled);
        }
    }

    /**
     * 生成注解方法
     */
    void writeToFile() {
        try {
            for (Map.Entry<TypeElement, BindingObject> entry : bindingMap.entrySet()) {
                Element enclosingElement = entry.getKey();
                BindingObject bindingObject = entry.getValue();

                JavaFile javaFile = bindingObject.brewJava();
                // 生成class文件
                javaFile.writeTo(filer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
