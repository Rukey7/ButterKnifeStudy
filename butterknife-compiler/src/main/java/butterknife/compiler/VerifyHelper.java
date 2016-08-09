package butterknife.compiler;

import com.google.auto.common.SuperficialValidation;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import butterknife.annotation.BindString;

/**
 * Created by long on 2016/8/9.
 * 检验元素有效性帮助类
 */
public final class VerifyHelper {

    private static final String STRING_TYPE = "java.lang.String";


    private VerifyHelper() {

    }


    public static boolean verifyResString(Element element, Messager messager) {
        return _verifyElement(element, BindString.class, STRING_TYPE, messager);
    }


    /*************************************************************************/

    /**
     * 验证元素的有效性
     * @param element   注解元素
     * @param annotationClass   注解类
     * @param elementType   元素类型的完全限定名称：java.lang.String
     * @param messager  提供注解处理器用来报告错误消息、警告和其他通知
     * @return  有效则返回true，否则false
     */
    private static boolean _verifyElement(Element element, Class<? extends Annotation> annotationClass,
                                        String elementType, Messager messager) {
        // 检测元素的有效性
        if (!SuperficialValidation.validateElement(element)) {
            return false;
        }
        // 获取最里层的外围元素
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        // 检测使用该注解的元素类型是否正确
        if (!elementType.equals(element.asType().toString())) {
            _error(messager, element, "@%s field type must be 'String'. (%s.%s)",
                    annotationClass.getSimpleName(), enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            return false;
        }
        // 使用该注解的字段访问权限不能为 private 和 static
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            _error(messager, element, "@%s %s must not be private or static. (%s.%s)",
                    annotationClass.getSimpleName(), "fields", enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            return false;
        }
        // 包含该注解的外围元素种类必须为 Class
        if (enclosingElement.getKind() != ElementKind.CLASS) {
            _error(messager, enclosingElement, "@%s %s may only be contained in classes. (%s.%s)",
                    annotationClass.getSimpleName(), "fields", enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            return false;
        }
        // 包含该注解的外围元素访问权限不能为 private
        if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
            _error(messager, enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
                    annotationClass.getSimpleName(), "fields", enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            return false;
        }
        // 判断是否处于错误的包中
        String qualifiedName = enclosingElement.getQualifiedName().toString();
        if (qualifiedName.startsWith("android.")) {
            _error(messager, element, "@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return false;
        }
        if (qualifiedName.startsWith("java.")) {
            _error(messager, element, "@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return false;
        }

        return true;
    }

    /**
     * 输出错误信息
     * @param element
     * @param message
     * @param args
     */
    private static void _error(Messager messager, Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
