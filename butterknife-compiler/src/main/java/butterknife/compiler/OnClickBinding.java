package butterknife.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by long on 2016/8/12.
 * 方法信息
 */
final class OnClickBinding implements ViewBinding {

    private final String name;
    private final List<Parameter> parameters;

    OnClickBinding(String name, List<Parameter> parameters) {
        this.name = name;
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public String getDescription() {
        return "method '" + name + "'";
    }
}
