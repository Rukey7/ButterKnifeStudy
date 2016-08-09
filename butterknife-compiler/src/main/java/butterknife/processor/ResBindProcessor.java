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

        for (Element element : roundEnv.getElementsAnnotatedWith(BindString.class)) {
            System.out.println("-------------------------");
            if (VerifyHelper.verifyResString(element, messager)) {
                ParseHelper.parseResString(element, targetClassMap, elementUtils);
            }
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
