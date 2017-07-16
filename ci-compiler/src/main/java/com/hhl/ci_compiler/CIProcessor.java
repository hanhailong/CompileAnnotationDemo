package com.hhl.ci_compiler;

import com.google.auto.service.AutoService;
import com.hhl.ci_annotation.BindView;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class CIProcessor extends AbstractProcessor {

    private Filer fileUtils;
    private Elements elementUtils;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        fileUtils = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    /**
     * Fixed writing
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();

        for (Class<? extends Annotation> clz : getSupportedAnnotations()) {
            types.add(clz.getCanonicalName());
        }

        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();

        annotations.add(BindView.class);
        //add other annotations

        return annotations;
    }

    /**
     * Fixed writing
     *
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private Map<String, ProxyInfo> proxyInfoMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //一般分成两步：
        //1.收集信息
        proxyInfoMap.clear();

        //解析BindView，当然你也可以解析其他的
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);

        if (elements != null) {
            for (Element element : elements) {
                //检查element
                checkAnnotationValid(element, BindView.class);

                //成员变量
                VariableElement variableElement = (VariableElement) element;

                //类Type
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                //类的全名子，例如：com.hhl.MainActivity
                String fullName = typeElement.getQualifiedName().toString();

                //从缓存Map里面读取
                ProxyInfo proxyInfo = proxyInfoMap.get(fullName);

                if (proxyInfo == null) {
                    proxyInfo = new ProxyInfo(elementUtils, typeElement);
                    proxyInfoMap.put(fullName, proxyInfo);
                }

                BindView annotation = variableElement.getAnnotation(BindView.class);
                int id = annotation.value();
                proxyInfo.injectVariables.put(id, variableElement);
            }
        }

        //2.生成Java代码（代理类）
        for (String key : proxyInfoMap.keySet()) {
            ProxyInfo proxyInfo = proxyInfoMap.get(key);

            try {
                JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(
                        proxyInfo.getProxyClassFullName(), proxyInfo.getTypeElement());
                Writer writer = javaFileObject.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(), "Unable to write injector for type %s: %s\""
                        , proxyInfo.getTypeElement(), e.getMessage());
            }
        }

        return true;
    }

    private boolean checkAnnotationValid(Element element, Class clz) {
        if (element.getKind() != ElementKind.FIELD) {
            error(element, "%s must be declared on a field", clz.getSimpleName());
            return false;
        }

        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            error(element, "%s must not be declared private", element.getSimpleName());
            return false;
        }

        return true;
    }

    /**
     * 打印错误日志
     *
     * @param element
     * @param message
     * @param args
     */
    private void error(Element element, String message, Object... args) {
        if (args != null && args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

}
