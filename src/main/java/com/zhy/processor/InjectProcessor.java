package com.zhy.processor;

import com.squareup.javawriter.JavaWriter;
import com.zhy.annotation.InjectView;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("com.zhy.annotation.InjectView")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class InjectProcessor extends AbstractProcessor {
    private Map<String, ProxyInfo> mProxyMap = new HashMap<>();
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        String fqClassName, className, packageName;

        for (Element element : roundEnv.getElementsAnnotatedWith(InjectView.class)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ele = " + element);

            if (element.getKind() == ElementKind.CLASS) {
                TypeElement classElement = (TypeElement) element;
                PackageElement packageElement = (PackageElement) element.getEnclosingElement();
                fqClassName = classElement.getQualifiedName().toString();
                className = classElement.getSimpleName().toString();
                packageName = packageElement.getQualifiedName().toString();

                int layoutId = classElement.getAnnotation(InjectView.class).value();

                ProxyInfo proxyInfo= mProxyMap.get(fqClassName);
                if (proxyInfo != null) {
                    proxyInfo.setLayoutId(layoutId);
                } else {
                    proxyInfo = new ProxyInfo(packageName, className);
                    proxyInfo.setTypeElement(classElement);
                    proxyInfo.setLayoutId(layoutId);
                    mProxyMap.put(fqClassName, proxyInfo);
                }
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "annotated class : " + packageName + ", className = " + className + ", fqClassName = " + fqClassName);

            } else if (element.getKind() == ElementKind.FIELD){
                VariableElement variableElement = (VariableElement) element;

                TypeElement classElement = (TypeElement) element.getEnclosingElement();
                fqClassName = classElement.getQualifiedName().toString();
                PackageElement packageElement = elementUtils.getPackageOf(classElement);
                packageName = packageElement.getQualifiedName().toString();
                className = getClassName(classElement, packageName);

                int id = variableElement.getAnnotation(InjectView.class).value();
                String fieldName = variableElement.getSimpleName().toString();
                String fieldType = variableElement.asType().toString();

                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "annatated field : fieldName = "
                        + variableElement.getSimpleName().toString()
                        + " , id = " + id + " , fileType = "
                        + fieldType);
                ProxyInfo proxyInfo = mProxyMap.get(fqClassName);
                if (proxyInfo == null) {
                    proxyInfo = new ProxyInfo(packageName, className);
                    mProxyMap.put(fqClassName, proxyInfo);
                    proxyInfo.setTypeElement(classElement);
                }
                proxyInfo.putViewInfo(id, new ViewInfo(id, fieldName, fieldType));
            }
        }

        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            Writer writer = null;
            try {
                writer = processingEnv.getFiler().createSourceFile(proxyInfo.getProxyClassFullName(), proxyInfo.getTypeElement()).openWriter();
//                JavaWriter javaWriter = new JavaWriter(writer);
//                javaWriter.
                writer.write(proxyInfo.generateJavaCode());
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(), "%s: %s", proxyInfo.getTypeElement(), e.getMessage());
            } finally {
                if (null != writer) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    private void writeLog(String str) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File("/Users/yangzhang/IdeaProjects/generateFiles", "process.txt"), true);
            fileWriter.write(str + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fileWriter) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
        }
    }
}
