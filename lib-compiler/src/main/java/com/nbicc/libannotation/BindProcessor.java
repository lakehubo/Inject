package com.nbicc.libannotation;

import com.google.auto.service.AutoService;
import com.nbicc.libbindview.BindBroadcastReceiver;
import com.nbicc.libbindview.BindClick;
import com.nbicc.libbindview.BindView;
import com.squareup.javapoet.TypeName;

import java.io.Writer;
import java.util.Arrays;
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
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

/**
 * 注解处理器
 */
@AutoService(Processor.class)
public class BindProcessor extends AbstractProcessor {
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
        collectBindViewInfo(roundEnvironment);
        collectBindClickInfo(roundEnvironment);
        collectBroadCastInfo(roundEnvironment);
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
        int[] ridss = null;
        int i = 0;
        for (Element element : elements) {
            // 备注解元素所在的Class
            // 收集Class中所有被注解的元素
            BindingObject bindingObject = getOrCreateBindingObject((TypeElement) element.getEnclosingElement());
            int[] rid = element.getAnnotation(BindView.class).value();
            if (ridss == null) {
                ridss = rid;
            }
            if (!Arrays.equals(ridss, rid)) {
                i = 0;
                ridss = rid;
            }
            Name simpleName = element.getSimpleName();
            String name = simpleName.toString();
            TypeMirror elementType = element.asType();
            TypeName typeName = TypeName.get(elementType);
            if (Arrays.equals(ridss, rid)) {
                if (i < rid.length) {
                    BindViewFiled bindViewFiled = new BindViewFiled(element, rid[i], name, typeName);
                    bindingObject.addField(bindViewFiled);
                    i++;
                } else {
                    continue;
                }
            }
        }
    }

    /**
     * 收集所有BindClick注解信息
     *
     * @param roundEnvironment
     */
    private void collectBindClickInfo(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindClick.class);
        for (Element element : elements) {
            // 备注解元素所在的Class
            // 收集Class中所有被注解的元素
            BindingObject bindingObject = getOrCreateBindingObject((TypeElement) element.getEnclosingElement());
            int[] rids = element.getAnnotation(BindClick.class).value();
            String name = element.getSimpleName().toString();

            BindViewMethod bindViewMethod = new BindViewMethod(element, rids, name);
            bindingObject.addField(bindViewMethod);
        }
    }

    /**
     * 收集所有BindBoradcastReceiver注解信息
     *
     * @param roundEnvironment
     */
    private void collectBroadCastInfo(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindBroadcastReceiver.class);
        for (Element element : elements) {
            // 备注解元素所在的Class
            // 收集Class中所有被注解的元素
            BindingObject bindingObject = getOrCreateBindingObject((TypeElement) element.getEnclosingElement());
            String[] filter = element.getAnnotation(BindBroadcastReceiver.class).value();
            String name = element.getSimpleName().toString();
            BindBroadCast bindBroadCast = new BindBroadCast(element, filter, name);
            bindingObject.addField(bindBroadCast);
        }
    }

    /**
     * 获取类或者创建类
     *
     * @param typeElement
     * @return
     */
    private BindingObject getOrCreateBindingObject(TypeElement typeElement) {
        BindingObject bindingObject = bindingMap.get(typeElement);
        if (bindingObject == null) {
            String packageName = getPackageName(typeElement);
            String className = getClassName(typeElement, packageName);
            bindingObject = new BindingObject(typeElement, packageName, className);
            bindingMap.put(typeElement, bindingObject);
        }
        return bindingObject;
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
            } catch (Exception e) {
                e.printStackTrace();
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
