package butterknife.compiler;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import butterknife.annotation.BindString;

/**
 * Created by long on 2016/8/9.
 * ע������󶨰�����
 */
public final class ParseHelper {

    private static final String BINDING_CLASS_SUFFIX = "$$ViewBinder";


    private ParseHelper() {
        throw new AssertionError("No instances.");
    }


    public static void parseResString(Element element, Map<TypeElement, BindingClass> targetClassMap,
                                      Elements elementUtils) {
        // ��ȡ�ֶ�����ע�����ԴID
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
        // ������ com.butterknife.MainActivity �����Ϊ��
        if (bindingClass == null) {
            // ��ȡԪ�ص���ȫ�޶����ƣ�com.butterknife.MainActivity
            String targetType = enclosingElement.getQualifiedName().toString();
            // ��ȡԪ�����ڰ�����com.butterknife
            String classPackage = elementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();
            // ��ȡҪ���ɵ�Class�����ƣ�MainActivity$$ViewBinder
            int packageLen = classPackage.length() + 1;
            String className = targetType.substring(packageLen).replace('.', '$') + BINDING_CLASS_SUFFIX;
            // ����Class����ȫ�޶����ƣ�com.butterknife.MainActivity$$ViewBinder
            String classFqcn = classPackage + "." + className;

            /* ��Ҫ���������������Class���ƣ��ڲ������� */
//            String className = enclosingElement.getSimpleName() + BINDING_CLASS_SUFFIX;

            bindingClass = new BindingClass(classPackage, className, targetType, classFqcn);
            targetClassMap.put(enclosingElement, bindingClass);
        }
        return bindingClass;
    }

}
