package butterknife.compiler;

import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import butterknife.annotation.Bind;
import butterknife.annotation.BindColor;
import butterknife.annotation.BindString;

/**
 * Created by long on 2016/8/9.
 * 注解解析绑定帮助类
 */
public final class ParseHelper {

    private static final String BINDING_CLASS_SUFFIX = "$$ViewBinder";
    private static final String COLOR_STATE_LIST_TYPE = "android.content.res.ColorStateList";
    private static final String LIST_TYPE = List.class.getCanonicalName();
    private static final String ITERABLE_TYPE = "java.lang.Iterable<?>";
    static final String VIEW_TYPE = "android.view.View";


    private ParseHelper() {
        throw new AssertionError("No instances.");
    }

    /**
     * 解析 String 资源
     *
     * @param element        使用注解的元素
     * @param targetClassMap 映射表
     * @param elementUtils   元素工具类
     */
    public static void parseResString(Element element, Map<TypeElement, BindingClass> targetClassMap,
                                      Set<TypeElement> erasedTargetNames, Elements elementUtils) {
        // 获取字段名和注解的资源ID
        String name = element.getSimpleName().toString();
        int resId = element.getAnnotation(BindString.class).value();

        BindingClass bindingClass = _getOrCreateTargetClass(element, targetClassMap, elementUtils);
        // 生成资源信息
        FieldResourceBinding binding = new FieldResourceBinding(resId, name, "getString", false);
        // 给BindingClass添加资源信息
        bindingClass.addResourceBinding(binding);

        erasedTargetNames.add((TypeElement) element.getEnclosingElement());
    }

    /**
     * 解析 String 资源
     *
     * @param element        使用注解的元素
     * @param targetClassMap 映射表
     * @param elementUtils   元素工具类
     */
    public static void parseResColor(Element element, Map<TypeElement, BindingClass> targetClassMap,
                                     Set<TypeElement> erasedTargetNames, Elements elementUtils) {
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

        erasedTargetNames.add((TypeElement) element.getEnclosingElement());
    }

    /**
     * 解析 View 资源
     *
     * @param element        使用注解的元素
     * @param targetClassMap 映射表
     * @param elementUtils   元素工具类
     */
    public static void parseView(Element element, Map<TypeElement, BindingClass> targetClassMap,
                                 Set<TypeElement> erasedTargetNames,
                                 Elements elementUtils, Types typeUtils, Messager messager) {
        TypeMirror elementType = element.asType();
        // 判断是一个 View 还是列表
        if (elementType.getKind() == TypeKind.ARRAY) {
            _parseBindMany(element, targetClassMap, erasedTargetNames, elementUtils, messager);
        } else if (LIST_TYPE.equals(_doubleErasure(elementType, typeUtils))) {
            _parseBindMany(element, targetClassMap, erasedTargetNames, elementUtils, messager);
        } else if (_isSubtypeOfType(elementType, ITERABLE_TYPE)) {
            _error(messager, element, "@%s must be a List or array. (%s.%s)", Bind.class.getSimpleName(),
                    ((TypeElement) element.getEnclosingElement()).getQualifiedName(),
                    element.getSimpleName());
        } else {
            _parseBindOne(element, targetClassMap, erasedTargetNames, elementUtils, messager);
        }
    }

    /*************************************************************************/

    /**
     * 获取存在的 BindingClass，没有则重新生成
     *
     * @param element        使用注解的元素
     * @param targetClassMap 映射表
     * @param elementUtils   元素工具类
     * @return BindingClass
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

    /**
     * 先通过 Types 工具对元素类型进行形式参数擦除，再通过字符比对进行二次擦除如果必要的话
     * 例：java.util.List<java.lang.String> -> java.util.List
     *
     * @param elementType 元素类型
     * @param typeUtils   类型工具
     * @return 类型完全限定名
     */
    private static String _doubleErasure(TypeMirror elementType, Types typeUtils) {
        String name = typeUtils.erasure(elementType).toString();
        int typeParamStart = name.indexOf('<');
        if (typeParamStart != -1) {
            name = name.substring(0, typeParamStart);
        }
        return name;
    }

    /**
     * 判断该类型是否为 otherType 的子类型
     *
     * @param typeMirror 元素类型
     * @param otherType  比对类型
     * @return
     */
    private static boolean _isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (otherType.equals(typeMirror.toString())) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        // 判断泛型列表
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        // 判断是否为类或接口类型
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        // 判断父类
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (_isSubtypeOfType(superType, otherType)) {
            return true;
        }
        // 判断接口
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (_isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 输出错误信息
     *
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

    /**
     * 解析单个View绑定
     *
     * @param element
     * @param targetClassMap
     * @param erasedTargetNames
     */
    private static void _parseBindOne(Element element, Map<TypeElement, BindingClass> targetClassMap,
                                      Set<TypeElement> erasedTargetNames, Elements elementUtils, Messager messager) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        TypeMirror elementType = element.asType();
        if (elementType.getKind() == TypeKind.TYPEVAR) {
            // 处理泛型，取它的上边界，例：<T extends TextView> -> TextView
            TypeVariable typeVariable = (TypeVariable) elementType;
            elementType = typeVariable.getUpperBound();
        }
        // 不是View的子类型，且不是接口类型则报错
        if (!_isSubtypeOfType(elementType, VIEW_TYPE) && !_isInterface(elementType)) {
            _error(messager, element, "@%s fields must extend from View or be an interface. (%s.%s)",
                    Bind.class.getSimpleName(), enclosingElement.getQualifiedName(), element.getSimpleName());
            return;
        }

        // 资源ID只能有一个
        int[] ids = element.getAnnotation(Bind.class).value();
        if (ids.length != 1) {
            _error(messager, element, "@%s for a view must only specify one ID. Found: %s. (%s.%s)",
                    Bind.class.getSimpleName(), Arrays.toString(ids), enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            return;
        }

        // 获取或创建绑定类
        int id = ids[0];
        BindingClass bindingClass = _getOrCreateTargetClass(element, targetClassMap, elementUtils);
        FieldViewBinding existViewBinding = bindingClass.isExistViewBinding(id);
        if (existViewBinding != null) {
            // 存在重复使用的ID
            _error(messager, element, "Attempt to use @%s for an already bound ID %d on '%s'. (%s.%s)",
                    Bind.class.getSimpleName(), id, existViewBinding.getName(),
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            return;
        }

        String name = element.getSimpleName().toString();
        TypeName type = TypeName.get(elementType);
        // 生成资源信息
        FieldViewBinding binding = new FieldViewBinding(name, type, true);
        // 给BindingClass添加资源信息
        bindingClass.addViewBinding(id, binding);

        erasedTargetNames.add(enclosingElement);
    }

    /**
     * 解析 View 列表
     * @param element
     * @param targetClassMap
     * @param erasedTargetNames
     * @param elementUtils
     * @param messager
     */
    private static void _parseBindMany(Element element, Map<TypeElement, BindingClass> targetClassMap,
                                       Set<TypeElement> erasedTargetNames,
                                       Elements elementUtils, Messager messager) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        TypeMirror elementType = element.asType();
        TypeMirror viewType = null;
        FieldCollectionViewBinding.Kind kind;

        if (elementType.getKind() == TypeKind.ARRAY) {
            ArrayType arrayType = (ArrayType) elementType;
            // 获取数组里面包含的View类型
            viewType = arrayType.getComponentType();
            kind = FieldCollectionViewBinding.Kind.ARRAY;
        } else {
            // 默认不是数组就只能是 List
            DeclaredType declaredType = (DeclaredType) elementType;
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (typeArguments.size() != 1) {
                // 列表的参数只能有一个
                _error(messager, element, "@%s List must have a generic component. (%s.%s)",
                        Bind.class.getSimpleName(), enclosingElement.getQualifiedName(),
                        element.getSimpleName());
                return;
            } else {
                // 获取 View 类型
                viewType = typeArguments.get(0);
            }
            kind = FieldCollectionViewBinding.Kind.LIST;
        }

        // 处理泛型
        if (viewType != null && viewType.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVariable = (TypeVariable) viewType;
            viewType = typeVariable.getUpperBound();
        }

        // 不是View的子类型，且不是接口类型则报错
        if (viewType != null && !_isSubtypeOfType(viewType, VIEW_TYPE) && !_isInterface(viewType)) {
            _error(messager, element, "@%s List or array type must extend from View or be an interface. (%s.%s)",
                    Bind.class.getSimpleName(), enclosingElement.getQualifiedName(), element.getSimpleName());
            return;
        }
        assert viewType != null; // Always false as hasError would have been true.

        int[] ids = element.getAnnotation(Bind.class).value();
        if (ids.length == 0) {
            _error(messager, element, "@%s must specify at least one ID. (%s.%s)", Bind.class.getSimpleName(),
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            return;
        }
        // 检测是否有重复 ID
        Integer duplicateId = _findDuplicate(ids);
        if (duplicateId != null) {
            _error(messager, element, "@%s annotation contains duplicate ID %d. (%s.%s)", Bind.class.getSimpleName(),
                    duplicateId, enclosingElement.getQualifiedName(), element.getSimpleName());
        }

        String name = element.getSimpleName().toString();
        TypeName typeName = TypeName.get(viewType); // 这边取得是View的类型不是列表类型
        BindingClass bindingClass = _getOrCreateTargetClass(element, targetClassMap, elementUtils);
        FieldCollectionViewBinding binding = new FieldCollectionViewBinding(name, typeName, kind);
        bindingClass.addFieldCollection(ids, binding);

        erasedTargetNames.add(enclosingElement);
    }

    /**
     * 判断是否为接口
     *
     * @param typeMirror
     * @return
     */
    private static boolean _isInterface(TypeMirror typeMirror) {
        return typeMirror instanceof DeclaredType
                && ((DeclaredType) typeMirror).asElement().getKind() == ElementKind.INTERFACE;
    }

    /**
     * 检测重复ID
     */
    private static Integer _findDuplicate(int[] array) {
        Set<Integer> seenElements = new LinkedHashSet<>();

        for (int element : array) {
            if (!seenElements.add(element)) {
                return element;
            }
        }

        return null;
    }
}
