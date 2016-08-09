package butterknife.compiler;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import butterknife.annotation.BindString;

/**
 * Created by long on 2016/8/9.
 * 注解解析绑定帮助类
 */
public final class ParseHelper {

    private static final String BINDING_CLASS_SUFFIX = "$$ViewBinder";


    private ParseHelper() {

    }


    public static void parseResString(Element element, Map<TypeElement, BindingClass> targetClassMap,
                                      Elements elementUtils) {
        // 获取字段名和注解的资源ID
        String name = element.getSimpleName().toString();
        int resId = element.getAnnotation(BindString.class).value();

        BindingClass bindingClass = getOrCreateTargetClass(targetClassMap, element, elementUtils);
        FieldResourceBinding binding = new FieldResourceBinding(resId, name, "getString", false);
        bindingClass.addResource(binding);
    }


    private static BindingClass getOrCreateTargetClass(Map<TypeElement, BindingClass> targetClassMap, Element element,
                                                       Elements elementUtils) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        BindingClass bindingClass = targetClassMap.get(enclosingElement);
        // 以下以 com.butterknife.MainActivity 这个类为例
        if (bindingClass == null) {
            // 获取元素的完全限定名称：com.butterknife.MainActivity
            String targetType = enclosingElement.getQualifiedName().toString();
            // 获取元素所在包名：com.butterknife
            String classPackage = elementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();
            // 获取要生成的Class的名称：MainActivity$$ViewBinder
            int packageLen = classPackage.length() + 1;
            String className = targetType.substring(packageLen).replace('.', '$') + BINDING_CLASS_SUFFIX;
            // 生成Class的完全限定名称：com.butterknife.MainActivity$$ViewBinder
            String classFqcn = classPackage + "." + className;

            /* 不要用下面这个来生成Class名称，内部类会出错 */
//            String className = enclosingElement.getSimpleName() + BINDING_CLASS_SUFFIX;

            bindingClass = new BindingClass(classPackage, className, targetType, classFqcn);
            targetClassMap.put(enclosingElement, bindingClass);
        }
        return bindingClass;
    }

}
