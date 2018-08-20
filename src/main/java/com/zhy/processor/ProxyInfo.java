package com.zhy.processor;

import android.view.View;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

public class ProxyInfo {
    private String packageName;
    private String targetClassName;
    private String proxyClassName;
    private TypeElement typeElement;

    private int layoutId;

    private Map<Integer, ViewInfo> idViewMap = new HashMap<>();

    ProxyInfo(String packageName, String className) {
        this.packageName = packageName;
        this.targetClassName = className;
        this.proxyClassName = className + Constant.PROXY_SUFF;
    }

    void putViewInfo(int id, ViewInfo viewInfo) {
        idViewMap.put(id, viewInfo);
    }

    // fixme: I do not know how to generate code "class A<T extends B> implements AbstractInjector<T>" <br> .
    //  fixme: So I can only generate ""class A<T> implements AbstractInjector<T>".
    //  fixme: And then cast type T to type B
    TypeSpec generateJavaCodeWithPoet() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(proxyClassName)
                .addTypeVariable(TypeVariableName.get("T"))
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AbstractInjector.class), TypeVariableName.get("T")))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(generateMethodSpec());
        return builder.build();
    }

    private MethodSpec generateMethodSpec() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("inject")
                .addAnnotation(ClassName.get(Override.class))
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get(Finder.class), "finder", Modifier.FINAL)
                .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
                .addParameter(ClassName.get(Object.class), "source");
        if (layoutId > 0) {
            builder.addStatement("finder.setContentView(sources, $L)", layoutId);
        }

        builder.addStatement("$T view", View.class)
                .beginControlFlow("if(target instanceof $L)", targetClassName)
                .addStatement("$L t = ($L) target", targetClassName, targetClassName);

        for (Integer key : idViewMap.keySet()) {
            ViewInfo viewInfo = idViewMap.get(key);
            builder.addStatement("view = finder.findViewById(source, $L)", viewInfo.getId())
                    .addStatement("t.$L = finder.castView(view, $L, $S)", viewInfo.getName(), viewInfo.getId(), viewInfo.getName());

        }
        builder.endControlFlow();
        return builder.build();
    }

    TypeElement getTypeElement() {
        return typeElement;
    }

    void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }
}
