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

    TypeSpec generateJavaCodeWithPoet() {
        TypeVariableName typeVariableName = TypeVariableName.get("T").withBounds(TypeVariableName.get(targetClassName));
        TypeSpec.Builder builder = TypeSpec.classBuilder(proxyClassName)
                .addTypeVariable(typeVariableName)
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

        builder.addStatement("$T view", View.class);

        for (Integer key : idViewMap.keySet()) {
            ViewInfo viewInfo = idViewMap.get(key);
            builder.addStatement("view = finder.findViewById(source, $L)", viewInfo.getId())
                    .addStatement("target.$L = finder.castView(view, $L, $S)", viewInfo.getName(), viewInfo.getId(), viewInfo.getName());

        }
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
