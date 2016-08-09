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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import butterknife.annotation.BindString;
import butterknife.compiler.BindingClass;
import butterknife.compiler.FieldResourceBinding;
import butterknife.compiler.VerifyHelper;

/**
 * Created by long on 2016/8/9.
 * 资源注解处理器
 */
@AutoService(Processor.class)
public class ResBindProcessor extends AbstractProcessor {

    private static final String STRING_TYPE = "java.lang.String";

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

        for (Element element : roundEnv.getElementsAnnotatedWith(BindString.class)) {
            System.out.println("-------------------------");
            if (VerifyHelper.verifyResString(element, messager)) {
                // 获取字段名和注解的资源ID
                String name = element.getSimpleName().toString();
                int resId = element.getAnnotation(BindString.class).value();

                BindingClass bindingClass = getOrCreateTargetClass(targetClassMap, (TypeElement) element.getEnclosingElement());
                FieldResourceBinding binding = new FieldResourceBinding(resId, name, "getString", false);
                bindingClass.addResource(binding);
            }
//            // 检测元素的有效性
//            if (!SuperficialValidation.validateElement(element)) {
//                continue;
//            }
//            // 获取最里层的外围元素
//            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
//            // 检测使用该注解的元素类型是否为String
//            if (!STRING_TYPE.equals(element.asType().toString())) {
//                _error(element, "@%s field type must be 'String'. (%s.%s)",
//                        BindString.class.getSimpleName(), enclosingElement.getQualifiedName(),
//                        element.getSimpleName());
//                continue;
//            }
//            // 使用该注解的字段访问权限不能为 private 和 static
//            Set<Modifier> modifiers = element.getModifiers();
//            if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
//                _error(element, "@%s %s must not be private or static. (%s.%s)",
//                        BindString.class.getSimpleName(), "fields", enclosingElement.getQualifiedName(),
//                        element.getSimpleName());
//                continue;
//            }
//            // 包含该注解的外围元素种类必须为 Class
//            if (enclosingElement.getKind() != ElementKind.CLASS) {
//                _error(enclosingElement, "@%s %s may only be contained in classes. (%s.%s)",
//                        BindString.class.getSimpleName(), "fields", enclosingElement.getQualifiedName(),
//                        element.getSimpleName());
//                continue;
//            }
//            // 包含该注解的外围元素访问权限不能为 private
//            if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
//                _error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
//                        BindString.class.getSimpleName(), "fields", enclosingElement.getQualifiedName(),
//                        element.getSimpleName());
//                continue;
//            }
//            // 判断是否处于错误的包中
//            String qualifiedName = enclosingElement.getQualifiedName().toString();
//            if (qualifiedName.startsWith("android.")) {
//                _error(element, "@%s-annotated class incorrectly in Android framework package. (%s)",
//                        BindString.class.getSimpleName(), qualifiedName);
//                continue;
//            }
//            if (qualifiedName.startsWith("java.")) {
//                _error(element, "@%s-annotated class incorrectly in Java framework package. (%s)",
//                        BindString.class.getSimpleName(), qualifiedName);
//                continue;
//            }
            // 获取字段名和注解的资源ID
//            String name = element.getSimpleName().toString();
//            int resId = element.getAnnotation(BindString.class).value();
//
//            BindingClass bindingClass = getOrCreateTargetClass(targetClassMap, enclosingElement);
//            FieldResourceBinding binding = new FieldResourceBinding(resId, name, "getString", false);
//            bindingClass.addResource(binding);

//            System.out.println(element.getSimpleName());
//            System.out.println(resId);
//            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
//            System.out.println(typeElement.getQualifiedName());
            System.out.println("-------------------------");
        }

        for (Map.Entry<TypeElement, BindingClass> entry : targetClassMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingClass bindingClass = entry.getValue();

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
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /*************************************************************************/


    private BindingClass getOrCreateTargetClass(Map<TypeElement, BindingClass> targetClassMap,
                                                TypeElement enclosingElement) {
        BindingClass bindingClass = targetClassMap.get(enclosingElement);
        if (bindingClass == null) {

            String targetType = enclosingElement.getQualifiedName().toString();
            System.out.println(targetType);
            String classPackage = getPackageName(enclosingElement);
            System.out.println(classPackage);
            String className = getClassName(enclosingElement, classPackage) + BindingClass.BINDING_CLASS_SUFFIX;
            System.out.println(className);
            String classFqcn = getFqcn(enclosingElement) + BindingClass.BINDING_CLASS_SUFFIX;
            System.out.println(classFqcn);

            bindingClass = new BindingClass(classPackage, className, targetType, classFqcn);
            targetClassMap.put(enclosingElement, bindingClass);
        }
        return bindingClass;
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    private String getFqcn(TypeElement typeElement) {
        String packageName = getPackageName(typeElement);
        return packageName + "." + getClassName(typeElement, packageName);
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
