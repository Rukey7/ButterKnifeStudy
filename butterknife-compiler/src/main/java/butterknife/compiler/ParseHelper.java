package butterknife.compiler;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import butterknife.annotation.BindColor;
import butterknife.annotation.BindString;

/**
 * Created by long on 2016/8/9.
 * 注解解析绑定帮助类
 */
public final class ParseHelper {

    private static final String BINDING_CLASS_SUFFIX = "$$ViewBinder";
    private static final String COLOR_STATE_LIST_TYPE = "android.content.res.ColorStateList";


    private ParseHelper() {
        throw new AssertionError("No instances.");
    }

    /**
     * 解析 String 资源
     * @param element     使用注解的元素
     * @param targetClassMap  映射表
     * @param elementUtils  元素工具类
     */
    public static void parseResString(Element element, Map<TypeElement, BindingClass> targetClassMap,
                                      Elements elementUtils) {
        // 获取字段名和注解的资源ID
        String name = element.getSimpleName().toString();
        int resId = element.getAnnotation(BindString.class).value();

        BindingClass bindingClass = _getOrCreateTargetClass(element, targetClassMap, elementUtils);
        // 生成资源信息
        FieldResourceBinding binding = new FieldResourceBinding(resId, name, "getString", false);
        // 给BindingClass添加资源信息
        bindingClass.addResourceBinding(binding);
    }

    /**
     * 解析 String 资源
     * @param element     使用注解的元素
     * @param targetClassMap  映射表
     * @param elementUtils  元素工具类
     */
    public static void parseResColor(Element element, Map<TypeElement, BindingClass> targetClassMap,
                                      Elements elementUtils) {
        // 获取字段名和注解的资源ID
        String name = element.getSimpleName().toString();
        int resId = element.getAnnotation(BindColor.class).value();

        BindingClass bindingClass = _getOrCreateTargetClass(element, targetClassMap, elementUtils);
        // 生成资源信息
        FieldColorBinding binding;
        if (COLOR_STATE_LIST_TYPE.equals(element.asType().toString())) {
            binding = new FieldColorBinding(resId, name, "getColorStateList");
        } else {
            binding = new FieldColorBinding(resId, name, "getColor");
        }

        // 给BindingClass添加资源信息
        bindingClass.addColorBinding(binding);
    }


    /*************************************************************************/

    /**
     * 获取存在的 BindingClass，没有则重新生成
     * @param element     使用注解的元素
     * @param targetClassMap  映射表
     * @param elementUtils  元素工具类
     * @return  BindingClass
     */
    private static BindingClass _getOrCreateTargetClass(Element element, Map<TypeElement, BindingClass> targetClassMap,
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

            /* 不要用下面这个来生成Class名称，内部类会出错,比如ViewHolder */
//            String className = enclosingElement.getSimpleName() + BINDING_CLASS_SUFFIX;

            bindingClass = new BindingClass(classPackage, className, targetType, classFqcn);
            targetClassMap.put(enclosingElement, bindingClass);
        }
        return bindingClass;
    }

}
