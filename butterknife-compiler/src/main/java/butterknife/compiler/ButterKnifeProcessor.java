package butterknife.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.LinkedHashMap;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import butterknife.annotation.Bind;
import butterknife.annotation.BindColor;
import butterknife.annotation.BindString;
import butterknife.annotation.OnClick;

/**
 * Created by long on 2016/8/11.
 * 注解处理器
 */
@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 保存包含注解元素的目标类，注意是使用注解的外围类，主要用来处理父类继承，例：MainActivity
        Set<TypeElement> erasedTargetNames = new LinkedHashSet<>();
        // TypeElement 使用注解的外围类，BindingClass 对应一个要生成的类
        Map<TypeElement, BindingClass> targetClassMap = new LinkedHashMap<>();

        // 处理BindString
        for (Element element : roundEnv.getElementsAnnotatedWith(BindString.class)) {
            if (VerifyHelper.verifyResString(element, messager)) {
                ParseHelper.parseResString(element, targetClassMap, erasedTargetNames, elementUtils);
            }
        }
        // 处理BindColor
        for (Element element : roundEnv.getElementsAnnotatedWith(BindColor.class)) {
            if (VerifyHelper.verifyResColor(element, messager)) {
                ParseHelper.parseResColor(element, targetClassMap, erasedTargetNames, elementUtils);
            }
        }
        // 处理Bind
        for (Element element : roundEnv.getElementsAnnotatedWith(Bind.class)) {
            if (VerifyHelper.verifyView(element, messager)) {
                ParseHelper.parseViewBind(element, targetClassMap, erasedTargetNames,
                        elementUtils, typeUtils, messager);
            }
        }
        // 处理OnClick
        for (Element element : roundEnv.getElementsAnnotatedWith(OnClick.class)) {
            if (VerifyHelper.verifyOnClick(element, messager)) {
                ParseHelper.parseOnClick(element, targetClassMap, erasedTargetNames,
                        elementUtils, typeUtils, messager);
            }
        }

        for (Map.Entry<TypeElement, BindingClass> entry : targetClassMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingClass bindingClass = entry.getValue();

            // 查看是否父类也进行注解绑定，有则添加到BindingClass
            TypeElement parentType = _findParentType(typeElement, erasedTargetNames);
            if (parentType != null) {
                BindingClass parentBinding = targetClassMap.get(parentType);
                bindingClass.setParentBinding(parentBinding);
            }

            try {
                // 生成Java文件
                bindingClass.brewJava().writeTo(filer);
            } catch (IOException e) {
                _error(typeElement, "Unable to write view binder for type %s: %s", typeElement,
                        e.getMessage());
            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(BindString.class.getCanonicalName());
        annotations.add(BindColor.class.getCanonicalName());
        annotations.add(Bind.class.getCanonicalName());
        annotations.add(OnClick.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /*************************************************************************/

    /**
     * 查找父类型
     * @param typeElement   类元素
     * @param erasedTargetNames 存在的类元素
     * @return
     */
    private TypeElement _findParentType(TypeElement typeElement, Set<TypeElement> erasedTargetNames) {
        TypeMirror typeMirror;
        while (true) {
            // 父类型要通过 TypeMirror 来获取
            typeMirror = typeElement.getSuperclass();
            if (typeMirror.getKind() == TypeKind.NONE) {
                return null;
            }
            // 获取父类元素
            typeElement = (TypeElement) ((DeclaredType)typeMirror).asElement();
            if (erasedTargetNames.contains(typeElement)) {
                // 如果父类元素存在则返回
                return typeElement;
            }
        }
    }

    /**
     * 输出错误信息
     * @param element
     * @param message
     * @param args
     */
    private void _error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
