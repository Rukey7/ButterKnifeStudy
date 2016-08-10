package butterknife.processor;

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

import butterknife.annotation.BindColor;
import butterknife.annotation.BindString;
import butterknife.compiler.BindingClass;
import butterknife.compiler.ParseHelper;
import butterknife.compiler.VerifyHelper;

/**
 * Created by long on 2016/8/9.
 * 资源注解处理器
 */
@AutoService(Processor.class)
public class ResBindProcessor extends AbstractProcessor {

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
        Map<TypeElement, BindingClass> targetClassMap = new LinkedHashMap<>();
        Set<TypeElement> erasedTargetNames = new LinkedHashSet<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(BindString.class)) {
            if (VerifyHelper.verifyResString(element, messager)) {
                ParseHelper.parseResString(element, targetClassMap, elementUtils);
                erasedTargetNames.add((TypeElement) element.getEnclosingElement());
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(BindColor.class)) {
            if (VerifyHelper.verifyResColor(element, messager)) {
                ParseHelper.parseResColor(element, targetClassMap, elementUtils);
                erasedTargetNames.add((TypeElement) element.getEnclosingElement());
            }
        }



        for (Map.Entry<TypeElement, BindingClass> entry : targetClassMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingClass bindingClass = entry.getValue();

            TypeElement parentType = _findParentType(typeElement, erasedTargetNames);
            if (parentType != null) {
                System.out.println("--------------------------");
                System.out.println(parentType.getQualifiedName());
                System.out.println("--------------------------");
                BindingClass parentBinding = targetClassMap.get(parentType);
                bindingClass.setParentBinding(parentBinding);
            }

            try {
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
