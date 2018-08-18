package com.zhy.processor;

import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

public class ProxyInfo {
    private String packagename;
    private String targetClassName;
    private String proxyClassName;
    private TypeElement typeElement;

    private int layoutId;

    private Map<Integer, ViewInfo> idViewMap = new HashMap<>();
    public static final String PROXY = "PROXY";

    public ProxyInfo(String packagename, String className) {
        this.packagename = packagename;
        this.targetClassName = className;
        this.proxyClassName = className + "_" + PROXY;
    }

    public void putViewInfo(int id, ViewInfo viewInfo) {
        idViewMap.put(id, viewInfo);
    }

    public Map<Integer, ViewInfo> getIdViewMap() {
        return idViewMap;
    }

    public String getProxyClassFullName() {
        return packagename + "." + proxyClassName;
    }

    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packagename).append("; \n\n");

        builder.append("import android.view.View;\n");
        builder.append("import com.zhy.processor.Finder;\n");
        builder.append("import com.zhy.processor.AbstractInjector;\n");
        builder.append("\n");
//        builder.append("// Generated code from Injector . Do not modify ! \n");
//        builder.append("\n");
        builder.append("public class ").append(proxyClassName);
        builder.append("<T extends ").append(getTargetClassName()).append(">");
        builder.append(" implements AbstractInjector<T> { \n");

        generateInjectMethod(builder);
        builder.append("\n");

        builder.append("}\n");

        return builder.toString();
    }

    public String getTargetClassName() {
        return targetClassName.replace("$", ".");
    }

    public void generateInjectMethod(StringBuilder stringBuilder) {
        stringBuilder.append("    @Override \n");
        stringBuilder.append("    public void inject(final Finder finder, final T target, Object source) {\n");
        if (layoutId > 0) {
            stringBuilder.append("finder.setContentView(source, ").append(layoutId).append(");\n");
        }
        stringBuilder.append("        View view;\n");
        for (Integer key: idViewMap.keySet()) {
            ViewInfo viewInfo = idViewMap.get(key);
            stringBuilder.append("        view = finder.findViewById( source, ").append(viewInfo.getId()).append(");\n");
            stringBuilder.append("        target.").append(viewInfo.getName()).append(" = ")
                    .append("finder.castView(view").append(", ").append(viewInfo.getId()).append(", \"");
            stringBuilder.append(viewInfo.getName()).append("\" ); \n");
        }
        stringBuilder.append("    }\n");
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }
}
