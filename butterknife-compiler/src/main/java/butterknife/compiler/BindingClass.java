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
 * �󶨴�����
 */
public final class BindingClass {

    private static final ClassName FINDER = ClassName.get("com.dl7.butterknifelib", "Finder");
    private static final ClassName VIEW_BINDER = ClassName.get("com.dl7.butterknifelib", "ViewBinder");
    private static final ClassName VIEW = ClassName.get("android.view", "View");
    private static final ClassName CONTEXT = ClassName.get("android.content", "Context");
    private static final ClassName RESOURCES = ClassName.get("android.content.res", "Resources");


    private final List<FieldResourceBinding> resourceBindings = new ArrayList<>();
    private final String classPackage;
    private final String className;
    private final String targetClass;
    private final String classFqcn;


    /**
     * �󶨴�����
     * @param classPackage  ������com.butterknife
     * @param className     ���ɵ��ࣺMainActivity$$ViewBinder
     * @param targetClass   Ŀ���ࣺcom.butterknife.MainActivity
     * @param classFqcn     ����Class����ȫ�޶����ƣ�com.butterknife.MainActivity$$ViewBinder
     */
    public BindingClass(String classPackage, String className, String targetClass, String classFqcn) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
        this.classFqcn = classFqcn;
    }

    /**
     * �����Դ
     * @param binding ��Դ��Ϣ
     */
    public void addResource(FieldResourceBinding binding) {
        resourceBindings.add(binding);
    }

    /**
     * ����Java��
     * @return  JavaFile
     */
    public JavaFile brewJava() {
        TypeSpec.Builder result = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T", ClassName.bestGuess(targetClass)));

        result.addSuperinterface(ParameterizedTypeName.get(VIEW_BINDER, TypeVariableName.get("T")));
        result.addMethod(createBindMethod());

        return JavaFile.builder(classPackage, result.build())
                .addFileComment("Generated code from Butter Knife. Do not modify!")
                .build();
    }


    private MethodSpec createBindMethod() {
        MethodSpec.Builder result = MethodSpec.methodBuilder("bind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(FINDER, "finder", Modifier.FINAL)
                .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
                .addParameter(Object.class, "source");

        result.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "$S", "ResourceType")
                .build());
        result.addStatement("$T res = finder.getContext(source).getResources()", RESOURCES);

        for (FieldResourceBinding binding : resourceBindings) {
            if (binding.isThemeable()) {
//                result.addStatement("target.$L = $T.$L(res, theme, $L)", binding.getName(),
//                        UTILS, binding.getMethod(), binding.getId());
            } else {
                result.addStatement("target.$L = res.$L($L)", binding.getName(), binding.getMethod(),
                        binding.getId());
            }
        }
        return result.build();
    }
}
