package butterknife.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Created by long on 2016/8/9.
 * 绑定处理类
 */
public final class BindingClass {

    private static final ClassName FINDER = ClassName.get("com.dl7.butterknifelib", "Finder");
    private static final ClassName VIEW_BINDER = ClassName.get("com.dl7.butterknifelib", "ViewBinder");
    private static final ClassName VIEW = ClassName.get("android.view", "View");
    private static final ClassName CONTEXT = ClassName.get("android.content", "Context");
    private static final ClassName RESOURCES = ClassName.get("android.content.res", "Resources");
    private static final ClassName CONTEXT_COMPAT = ClassName.get("android.support.v4.content", "ContextCompat");

    private final List<FieldResourceBinding> resourceBindings = new ArrayList<>();
    private final List<FieldColorBinding> colorBindings = new ArrayList<>();
    private BindingClass parentBinding;
    private final String classPackage;
    private final String className;
    private final String targetClass;
    private final String classFqcn;


    /**
     * 绑定处理类
     *
     * @param classPackage 包名：com.butterknife
     * @param className    生成的类：MainActivity$$ViewBinder
     * @param targetClass  目标类：com.butterknife.MainActivity
     * @param classFqcn    生成Class的完全限定名称：com.butterknife.MainActivity$$ViewBinder
     */
    public BindingClass(String classPackage, String className, String targetClass, String classFqcn) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
        this.classFqcn = classFqcn;
    }

    /**
     * 生成Java类
     *
     * @return JavaFile
     */
    public JavaFile brewJava() {
        TypeSpec.Builder result = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T", ClassName.bestGuess(targetClass)));

        if (_hasParentBinding()) {
            result.superclass(ParameterizedTypeName.get(ClassName.bestGuess(parentBinding.classFqcn),
                    TypeVariableName.get("T")));
        } else {
            result.addSuperinterface(ParameterizedTypeName.get(VIEW_BINDER, TypeVariableName.get("T")));
        }

        result.addMethod(_createBindMethod());

        return JavaFile.builder(classPackage, result.build())
                .addFileComment("Generated code from Butter Knife. Do not modify!")
                .build();
    }

    /**
     * 创建方法
     *
     * @return MethodSpec
     */
    private MethodSpec _createBindMethod() {
        MethodSpec.Builder result = MethodSpec.methodBuilder("bind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(FINDER, "finder", Modifier.FINAL)
                .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
                .addParameter(Object.class, "source");

        if (_hasParentBinding()) {
            // 调用父类的bind()方法
            result.addStatement("super.bind(finder, target, source)");
        }

        if (_hasResourceBinding()) {
            // 过滤警告
            result.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                    .addMember("value", "$S", "ResourceType")
                    .build());

            result.addStatement("$T context = finder.getContext(source)", CONTEXT);
            result.addStatement("$T res = context.getResources()", RESOURCES);

            for (FieldResourceBinding binding : resourceBindings) {
                result.addStatement("target.$L = res.$L($L)", binding.getName(), binding.getMethod(),
                        binding.getId());
            }

            for (FieldColorBinding binding : colorBindings) {
                result.addStatement("target.$L = $T.$L(context, $L)", binding.getName(), CONTEXT_COMPAT,
                        binding.getMethod(), binding.getId());
            }
        }
        return result.build();
    }

    /**
     * 添加资源
     *
     * @param binding 资源信息
     */
    public void addResourceBinding(FieldResourceBinding binding) {
        resourceBindings.add(binding);
    }

    private boolean _hasResourceBinding() {
        return !(resourceBindings.isEmpty() && colorBindings.isEmpty());
    }

    /**
     * 添加资源
     *
     * @param binding 资源信息
     */
    public void addColorBinding(FieldColorBinding binding) {
        colorBindings.add(binding);
    }

    private boolean _hasColorBinding() {
        return !colorBindings.isEmpty();
    }

    /**
     * 设置父类
     *
     * @param parentBinding BindingClass
     */
    public void setParentBinding(BindingClass parentBinding) {
        this.parentBinding = parentBinding;
    }

    private boolean _hasParentBinding() {
        return parentBinding != null;
    }
}
