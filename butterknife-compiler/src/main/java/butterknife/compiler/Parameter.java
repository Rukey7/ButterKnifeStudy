package butterknife.compiler;

import com.squareup.javapoet.TypeName;

/**
 * Created by long on 2016/8/12.
 * 方法的参数
 */
final class Parameter {

    static final Parameter[] NONE = new Parameter[0];

    // 参数的位置索引
    private final int listenerPosition;
    // 参数的类型
    private final TypeName type;

    Parameter(int listenerPosition, TypeName type) {
        this.listenerPosition = listenerPosition;
        this.type = type;
    }

    int getListenerPosition() {
        return listenerPosition;
    }

    TypeName getType() {
        return type;
    }

    public boolean requiresCast(String toType) {
        return !type.toString().equals(toType);
    }
}
